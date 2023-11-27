package com.siryus.swisscon.api.taskworklog;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.siryus.swisscon.api.auth.permission.PermissionName;
import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.event.ContractStateChangeEvent;
import com.siryus.swisscon.api.event.EventPublisher;
import com.siryus.swisscon.api.event.TaskTimerStartedEvent;
import com.siryus.swisscon.api.event.TaskCompletedEvent;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileRepository;
import com.siryus.swisscon.api.file.file.FileService;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.mediawidget.MediaWidgetFileDTO;
import com.siryus.swisscon.api.mediawidget.MediaWidgetService;
import com.siryus.swisscon.api.tasks.dto.TaskStatus;
import com.siryus.swisscon.api.tasks.entity.MainTaskEntity;
import com.siryus.swisscon.api.tasks.entity.SubTaskEntity;
import com.siryus.swisscon.api.tasks.exceptions.TaskExceptions;
import com.siryus.swisscon.api.tasks.repos.MainTaskRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskRepository;
import com.siryus.swisscon.api.taskworklog.dto.CreateTimerWorkLogRequest;
import com.siryus.swisscon.api.taskworklog.dto.ModifyTimerWorkLogRequest;
import com.siryus.swisscon.api.taskworklog.dto.StartStopWorklogDTO;
import com.siryus.swisscon.api.taskworklog.dto.TaskStatusDTO;
import com.siryus.swisscon.api.taskworklog.dto.TaskWorklogDTO;
import com.siryus.swisscon.api.taskworklog.dto.TaskWorklogRequest;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventCounter;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventType;
import com.siryus.swisscon.api.taskworklog.entity.TaskWorklogAttachmentEntity;
import com.siryus.swisscon.api.taskworklog.entity.TaskWorklogEntity;
import com.siryus.swisscon.api.taskworklog.exceptions.TaskWorklogExceptions;
import com.siryus.swisscon.api.taskworklog.repos.TaskWorkLogRepository;
import com.siryus.swisscon.api.util.ValidationUtils;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.api.util.validator.DTOValidator;
import com.siryus.swisscon.soa.EventsEmitter;
import com.siryus.swisscon.soa.notification.contract.NotificationEvent;
import com.siryus.swisscon.soa.notification.contract.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service("taskWorkLogService")
public class TaskWorkLogWriterServiceImpl implements TaskWorkLogWriterService {
    private final MediaWidgetService mediaWidgetService;


    private final MainTaskRepository mainTaskRepository;
    private final TaskWorkLogRepository taskWorkLogRepository;
    private final SubTaskRepository subTaskRepository;
    private final FileRepository fileRepository;
    private final FileService fileService;
    private final UserRepository userRepository;

    private final Supplier<Clock> clock;
    private final SecurityHelper securityHelper;
    private final EventPublisher eventPublisher;
    private final EventsEmitter eventsEmitter;


    @Autowired
    public TaskWorkLogWriterServiceImpl(
            MediaWidgetService mediaWidgetService,
            MainTaskRepository mainTaskRepository,
            TaskWorkLogRepository taskWorkLogRepository,
            SubTaskRepository subTaskRepository,
            FileRepository fileRepository,
            FileService fileService, UserRepository userRepository,
            Supplier<Clock> clock,
            SecurityHelper securityHelper,
            EventPublisher eventPublisher, EventsEmitter eventsEmitter) {
        this.mediaWidgetService = mediaWidgetService;
        this.mainTaskRepository = mainTaskRepository;
        this.taskWorkLogRepository = taskWorkLogRepository;
        this.subTaskRepository = subTaskRepository;
        this.fileRepository = fileRepository;
        this.fileService = fileService;
        this.userRepository = userRepository;
        this.clock = clock;
        this.securityHelper = securityHelper;
        this.eventPublisher = eventPublisher;
        this.eventsEmitter = eventsEmitter;
    }

    @Transactional
    @Override
    public TaskStatusDTO createMainTaskWorkLog(WorkLogEventType event, Integer workerId, TaskWorklogRequest request) {
        TaskWorklogRequest validRequest = validateRequest(request, event);

        MainTaskEntity validMainTask = validateMainTaskWorkLog(event, workerId, validRequest);

        var savedWorkLog = saveMainTaskWorkLog(validMainTask, event, workerId, validRequest);

        materializeAttachments(event, request.getAttachmentIDs(), validMainTask.getId());

        return statusDTOFrom(savedWorkLog, validMainTask);
    }

    @Transactional
    @Override
    public TaskStatusDTO completeTask(Integer userId, TaskWorklogRequest request) {
        TaskWorklogRequest validRequest = validateRequest(request, WorkLogEventType.COMPLETE_TASK);
        MainTaskEntity mainTask = mainTaskRepository.findById(validRequest.getMainTaskId())
                .orElseThrow(() -> TaskExceptions.mainTaskNotFound(validRequest.getMainTaskId()));

        return createMainTaskWorkLog(
                userCanCompleteProjectTask(userId, mainTask.getProjectId()) ? WorkLogEventType.COMPLETE_TASK : WorkLogEventType.COMPLETE_CONTRACTOR_TASK,
                userId,
                request
        );
    }

    @Transactional
    @Override
    public TaskStatusDTO createSubTaskWorkLog(WorkLogEventType event, Integer workerId, TaskWorklogRequest request) {
        TaskWorklogRequest validRequest = validateRequest(request, event);

        SubTaskEntity validSubTask = validateSubTaskWorkLog(workerId, validRequest);

        var savedWorkLog = saveSubTaskWorkLog(validSubTask, event, workerId, validRequest);

        materializeAttachments(event, request.getAttachmentIDs(), validSubTask.getMainTask().getId());

        if (event.sameAs(WorkLogEventType.START_TIMER)) {
            eventPublisher.publishEvent(new TaskTimerStartedEvent(validSubTask.getMainTask().getId()));
        }
        return statusDTOFrom(savedWorkLog, validSubTask.getMainTask());
    }

    private void materializeAttachments(WorkLogEventType event, List<Integer> attachmentsIDs, Integer mainTaskId) {
        validateAttachments(event, attachmentsIDs);
        if (event.supportsAttachments() && attachmentsIDs != null) {
            var systemFolder = getSystemFolder(event.attachmentSystemFolder(), mainTaskId);
            attachmentsIDs.forEach(a ->
                    mediaWidgetService.convertTemporaryFileToSystemFile(
                            a,
                            ReferenceType.MAIN_TASK,
                            mainTaskId,
                            systemFolder.getId()));
        }
    }

    private void validateAttachments(WorkLogEventType event, List<Integer> attachmentIDs) {
        ValidationUtils.throwIf(!event.supportsAttachments() && attachmentIDs != null && !attachmentIDs.isEmpty(),
                () -> TaskWorklogExceptions.eventDoesNotSupportAttachments(event));
    }

    private MediaWidgetFileDTO getSystemFolder(String systemFolderName, Integer mainTaskId) {
        return mediaWidgetService.getSystemFolderByName(
                ReferenceType.MAIN_TASK,
                mainTaskId,
                systemFolderName)
                .orElseThrow(() -> TaskExceptions.systemFolderForTaskNotFound(systemFolderName, mainTaskId));
    }

    @Transactional
    @Override
    public TaskWorklogDTO cancelSubTaskTimerWorkLog(Integer subTaskId, Integer startTimerEventId) {
        TaskWorklogEntity oldStartTimer = validateStartTimerEventId(subTaskId, startTimerEventId);

        SubTaskEntity validSubTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> TaskExceptions.subTaskNotFound(subTaskId));
        SubTaskEntity defaultSubTask = subTaskRepository.getDefaultSubTask(validSubTask.getMainTask().getId())
                .orElseThrow(() -> TaskExceptions.defaultSubTaskNotFound(validSubTask.getMainTask().getId()));
        boolean isMainTask = defaultSubTask.getId().equals(subTaskId);

        final TaskWorklogDTO taskWorklogDTO = TaskWorklogDTO.from(
                taskWorkLogRepository.save(
                        TaskWorklogEntity.builder()
                                .event(WorkLogEventType.CANCEL_TIMER)
                                .mainTask(oldStartTimer.getMainTask())
                                .subTask(oldStartTimer.getSubTask())
                                .worker(oldStartTimer.getWorker())
                                .timestamp(LocalDateTime.now())
                                .modifyingWorklogId(oldStartTimer.getId())
                                .build()
                )
        );

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .notificationType(isMainTask ? NotificationType.MAIN_TASK_WORKLOG_REMOVED : NotificationType.SUB_TASK_WORKLOG_REMOVED)
                .senderId(securityHelper.currentUserId())
                .projectId(oldStartTimer.getMainTask().getProjectId())
                .referenceId(oldStartTimer.getMainTask().getId())
                .subjectId(isMainTask ? oldStartTimer.getMainTask().getId() : subTaskId)
                .build());

        return taskWorklogDTO;
    }

    @Transactional
    @Override
    public StartStopWorklogDTO createSubTaskTimerWorkLog(Integer subTaskId, CreateTimerWorkLogRequest request) {
        SubTaskEntity validSubTask = validateSubTaskId(subTaskId);

        validateCreateTimerWorkLogRequest(subTaskId, request);

        materializeAttachments(WorkLogEventType.START_TIMER, request.getAttachmentIDs(), validSubTask.getMainTask().getId());

        TaskWorklogEntity startTimer = saveWithAttachments(
                TaskWorklogEntity.builder()
                        .worker(userRepository.getOne(request.getWorkerId()))
                        .event(WorkLogEventType.START_TIMER)
                        .mainTask(validSubTask.getMainTask())
                        .subTask(validSubTask)
                        .timestamp(request.getStartTime())
                        .comment(request.getComment())
                        .latitude(request.getLatitude())
                        .longitude(request.getLongitude())
                        .build(),
                Optional.ofNullable(request.getAttachmentIDs()).orElse(Collections.emptyList()).stream().map(id ->
                        new TaskWorklogAttachmentEntity(null, null, fileRepository.getOne(id))
                ).collect(Collectors.toList())
        );

        TaskWorklogEntity stopTimer = taskWorkLogRepository.save(
                startTimer.toBuilder()
                        .id(null)
                        .event(WorkLogEventType.STOP_TIMER)
                        .timestamp(request.getEndTime())
                        .build()
        );

        SubTaskEntity defaultSubTask = subTaskRepository.getDefaultSubTask(validSubTask.getMainTask().getId())
                .orElseThrow(() -> TaskExceptions.defaultSubTaskNotFound(validSubTask.getMainTask().getId()));
        boolean isMainTask = defaultSubTask.getId().equals(subTaskId);

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .notificationType(isMainTask ? NotificationType.MAIN_TASK_WORKLOG_ADDED : NotificationType.SUB_TASK_WORKLOG_ADDED)
                .senderId(securityHelper.currentUserId())
                .projectId(validSubTask.getMainTask().getProjectId())
                .referenceId(validSubTask.getMainTask().getId())
                .subjectId(isMainTask ? validSubTask.getMainTask().getId() : subTaskId)
                .build());

        return StartStopWorklogDTO.from(startTimer, stopTimer);
    }

    @Transactional
    @Override
    public StartStopWorklogDTO modifySubTaskTimerWorkLog(Integer subTaskId, Integer startTimerEventId, ModifyTimerWorkLogRequest request) {
        TaskWorklogEntity oldStartTimer = validateStartTimerEventId(subTaskId, startTimerEventId);

        cancelSubTaskTimerWorkLog(subTaskId, startTimerEventId);

        TaskWorklogEntity startTimer = saveWithAttachments(
                oldStartTimer.toBuilder()
                        .id(null)
                        .timestamp(request.getStartTime())
                        .build(),
                oldStartTimer.getAttachmentIDs()
        );

        TaskWorklogEntity stopTimer = taskWorkLogRepository.save(
                startTimer.toBuilder()
                        .id(null)
                        .event(WorkLogEventType.STOP_TIMER)
                        .timestamp(request.getStopTime())
                        .attachmentIDs(null)
                        .build()
        );

        SubTaskEntity defaultSubTask = subTaskRepository.getDefaultSubTask(oldStartTimer.getMainTask().getId())
                .orElseThrow(() -> TaskExceptions.defaultSubTaskNotFound(oldStartTimer.getMainTask().getId()));
        boolean isMainTask = defaultSubTask.getId().equals(subTaskId);

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .notificationType(isMainTask ? NotificationType.MAIN_TASK_WORKLOG_EDITED : NotificationType.SUB_TASK_WORKLOG_EDITED)
                .senderId(securityHelper.currentUserId())
                .projectId(oldStartTimer.getMainTask().getProjectId())
                .referenceId(oldStartTimer.getMainTask().getId())
                .subjectId(isMainTask ? oldStartTimer.getMainTask().getId() : subTaskId)
                .build());

        return StartStopWorklogDTO.from(startTimer, stopTimer);
    }

    private TaskWorklogEntity saveWithAttachments(TaskWorklogEntity entity, List<TaskWorklogAttachmentEntity> attachments) {
        TaskWorklogEntity savedEntity = taskWorkLogRepository.save(entity.toBuilder().attachmentIDs(null).build());

        if (attachments != null && !attachments.isEmpty()) {
            final TaskWorklogEntity s = savedEntity;
            savedEntity.setAttachmentIDs(attachments.stream().map(
                    a -> a.toBuilder().id(null).taskWorklog(s).build()
            ).collect(Collectors.toList()));
            savedEntity = taskWorkLogRepository.save(savedEntity);
        }

        return savedEntity;
    }

    private SubTaskEntity validateSubTaskId(Integer subTaskId) {
        return subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> TaskExceptions.subTaskNotFound(subTaskId));
    }

    private void validateCreateTimerWorkLogRequest(Integer subTaskId, CreateTimerWorkLogRequest request) {
        // TODO
    }

    private TaskWorklogEntity validateStartTimerEventId(Integer subTaskId, Integer startTimerEventId) {
        TaskWorklogEntity workLogEntity = taskWorkLogRepository.findById(startTimerEventId)
                .orElseThrow(() -> TaskWorklogExceptions.eventNotFound(startTimerEventId));

        if (!workLogEntity.getEvent().equals(WorkLogEventType.START_TIMER)) {
            throw TaskWorklogExceptions.eventIsNotStartEvent(startTimerEventId);
        }

        if (!subTaskId.equals(workLogEntity.getSubTask().getId())) {
            throw TaskWorklogExceptions.eventDoesNotBelongToSubTask(startTimerEventId, subTaskId);
        }

        return workLogEntity;
    }

    private MainTaskEntity validateMainTaskWorkLog(WorkLogEventType event, Integer workerId, TaskWorklogRequest request) {
        MainTaskEntity validMainTask = mainTaskRepository.findById(request.getMainTaskId())
                .orElseThrow(() -> TaskExceptions.mainTaskNotFound(request.getMainTaskId()));

        validateMainTaskEvent(event, validMainTask.getStatus());

        validateWorkerCanPostMainTaskEvent(event, workerId, validMainTask);

        return validMainTask;
    }

    private TaskWorklogEntity saveMainTaskWorkLog(MainTaskEntity validMainTask, WorkLogEventType event, Integer workerId, TaskWorklogRequest validRequest) {
        createImpliedEventsMainTask(event.impliedEvents(validMainTask.getStatus()), workerId, validRequest, validMainTask);

        TaskWorklogEntity taskWorklogEntity = saveTaskWorkLog(event, workerId, validRequest, validMainTask, null);

        updateStatusIfNeeded(validMainTask, null, event);

        return taskWorklogEntity;
    }

    private SubTaskEntity validateSubTaskWorkLog(Integer workerId, TaskWorklogRequest validRequest) {
        SubTaskEntity validSubTask = subTaskRepository.findById(validRequest.getSubTaskId())
                .orElseThrow(() -> TaskExceptions.subTaskNotFound(validRequest.getSubTaskId()));

        SubTaskEntity defaultSubTask = subTaskRepository.getDefaultSubTask(validSubTask.getMainTask().getId())
                .orElseThrow(() -> TaskExceptions.defaultSubTaskNotFound(validSubTask.getMainTask().getId()));

        securityHelper.validateUserIdPartOfSubTasksTeam(workerId, validSubTask.getId(), defaultSubTask.getId());

        return validSubTask;
    }

    private TaskWorklogEntity saveSubTaskWorkLog(SubTaskEntity validSubTask, WorkLogEventType event, Integer workerId, TaskWorklogRequest validRequest) {
        MainTaskEntity mainTask = validSubTask.getMainTask();

        List<WorkLogEventType.BoundEventType> impliedEvents = new ArrayList<>(validateSubTaskEvent(event, workerId, validSubTask));

        impliedEvents.addAll(event.impliedEvents(mainTask.getStatus()));

        createImpliedEventsSubTask(impliedEvents, workerId, validRequest, mainTask);

        TaskWorklogEntity taskWorklogEntity = saveTaskWorkLog(event, workerId, validRequest, mainTask, validSubTask);

        updateStatusIfNeeded(mainTask, validSubTask, event);

        return taskWorklogEntity;
    }

    private void createImpliedEventsMainTask(List<WorkLogEventType.BoundEventType> impliedEvents, Integer workerId, TaskWorklogRequest validRequest, MainTaskEntity mainTask) {
        for (WorkLogEventType.BoundEventType impliedEvent : impliedEvents) {
            if (impliedEvent.getEventType().isMainTaskEvent()) {
                if (impliedEvent.getEventType().validCurrentStatus(mainTask.getStatus())) {
                    saveMainTaskWorkLog(mainTask, impliedEvent.getEventType(), impliedEvent.getWorkerId(workerId), validRequest);
                }
            } else {
                subTaskRepository.listSubTasksForMainTask(mainTask.getId()).forEach(st -> {
                    TaskWorklogRequest subTaskSpecificRequest = new TaskWorklogRequest(
                            mainTask.getId(),
                            st.getId(),
                            validRequest.getComment(),
                            validRequest.getLatitude(),
                            validRequest.getLongitude(),
                            validRequest.getAttachmentIDs()
                    );
                    saveSubTaskWorkLog(st, impliedEvent.getEventType(), impliedEvent.getWorkerId(workerId), subTaskSpecificRequest);
                });
            }
        }
    }

    private void validateWorkerCanPostMainTaskEvent(WorkLogEventType event, Integer workerId, MainTaskEntity mainTask) {
        if (event.requirePermission(mainTask.getStatus()) != null) {
            securityHelper.validateUserHasProjectPermission(workerId, mainTask.getProjectId(), event.requirePermission(mainTask.getStatus()));
        } else {
            securityHelper.validateUserIdPartOfSubTasksTeam(
                    workerId,
                    subTaskRepository.getDefaultSubTask(mainTask.getId())
                            .orElseThrow(() -> TaskExceptions.defaultSubTaskNotFound(mainTask.getId()))
                            .getId());
        }
    }

    private void createImpliedEventsSubTask(List<WorkLogEventType.BoundEventType> impliedEvents, Integer workerId, TaskWorklogRequest validRequest, MainTaskEntity mainTask) {
        for (WorkLogEventType.BoundEventType impliedEvent : impliedEvents) {
            if (impliedEvent.getEventType().isMainTaskEvent()) {
                if (impliedEvent.getEventType().validCurrentStatus(mainTask.getStatus())) {
                    saveMainTaskWorkLog(mainTask, impliedEvent.getEventType(), impliedEvent.getWorkerId(workerId), validRequest);
                }
            } else {
                TaskWorklogRequest impliedWorkLogRequest = validRequest.withSubTaskId(impliedEvent.getSubTaskId(validRequest.getSubTaskId()));
                saveSubTaskWorkLog(
                        subTaskRepository.findById(impliedWorkLogRequest.getSubTaskId())
                                .orElseThrow(() -> TaskExceptions.subTaskNotFound(impliedWorkLogRequest.getSubTaskId())),
                        impliedEvent.getEventType(), impliedEvent.getWorkerId(workerId), impliedWorkLogRequest
                );
            }
        }
    }

    private TaskWorklogEntity saveTaskWorkLog(
            WorkLogEventType event,
            Integer workerId,
            TaskWorklogRequest validRequest,
            MainTaskEntity validMainTask,
            SubTaskEntity subTaskEntity
    ) {
        TaskWorklogEntity taskWorklogEntity = taskWorkLogRepository.save(new TaskWorklogEntity(
                validMainTask,
                subTaskEntity,
                userRepository.getOne(workerId),
                LocalDateTime.now(clock.get()),
                event.getConcreteEvent(),
                validRequest.getComment(),
                validRequest.getLatitude(),
                validRequest.getLongitude(),
                null,
                Collections.emptyList()
        ));

        if (validRequest.getAttachmentIDs() != null && shouldStoreAttachments(event, validRequest.getMainTaskId())) {
            taskWorklogEntity.setAttachmentIDs(validRequest.getAttachmentIDs().stream().map(id ->
                    new TaskWorklogAttachmentEntity(null, taskWorklogEntity, fileRepository.getOne(id))
            ).collect(Collectors.toList()));
            fileService.updateFileReferences(validRequest.getAttachmentIDs(), validMainTask.getId(), ReferenceType.MAIN_TASK);
        }

        return taskWorkLogRepository.save(taskWorklogEntity);
    }

    /**
     * Checks whether the attachments associated to the event should be saved.
     * <p>
     * A START_TIMER event can only be saved if it's the first event of such type issued on the given task.
     * This method assumes the current state of the TaskWorkLog table is already updated.
     *
     * @param event      Work Log Event Type
     * @param mainTaskId Main Task Id
     * @return true or false
     */
    private boolean shouldStoreAttachments(WorkLogEventType event, Integer mainTaskId) {
        if (event.supportsAttachments()) {
            if (event.sameAs(WorkLogEventType.START_TIMER)) {
                return taskWorkLogRepository.findByMainTask(mainTaskId, ImmutableSet.of(WorkLogEventType.START_TIMER.name())).size() == 1;
            }

            return true;
        }

        return false;
    }

    private List<WorkLogEventType.BoundEventType> validateSubTaskEvent(WorkLogEventType event, Integer workerId, SubTaskEntity validSubTask) {
        if (event.isMainTaskEvent()) {
            throw TaskWorklogExceptions.canNotLogMainTaskEventForSubTask();
        }

        if (!event.validCurrentStatus(validSubTask.getMainTask().getStatus())) {
            throw TaskWorklogExceptions.canNotSaveEventInStatus(event, validSubTask.getMainTask().getStatus());
        }

        if (event.equals(WorkLogEventType.STOP_ALL_TIMERS)) {
            return stopAllTimersOnSubTask(validSubTask);
        } else if (workerIsIdle(workerId)) {
            if (event.equals(WorkLogEventType.STOP_TIMER)) {
                throw TaskWorklogExceptions.workerIsIdle(workerId);
            }
        } else {
            if (event.equals(WorkLogEventType.START_MULTI_TIMER)) {
                return Collections.emptyList();
            } else if (event.equals(WorkLogEventType.START_TIMER)) {
                Integer subTaskWorkerWorkingOn = subTaskWorkerWorkingOn(workerId);
                if (subTaskWorkerWorkingOn.equals(validSubTask.getId())) {
                    throw TaskWorklogExceptions.workerIsBusy(workerId);
                }
                return Collections.singletonList(WorkLogEventType.BoundEventType.bound(workerId, subTaskWorkerWorkingOn, WorkLogEventType.STOP_TIMER));
            }
        }

        return Collections.emptyList();
    }

    private List<WorkLogEventType.BoundEventType> stopAllTimersOnSubTask(SubTaskEntity validSubTask) {
        return !validSubTask.getStatus().equals(TaskStatus.IN_PROGRESS) ?
                Collections.emptyList() :
                taskWorkLogRepository.findBySubTask(validSubTask.getId(), TaskWorkLogRepository.TIMER_EVENTS).stream()
                        .map(e -> e.getWorker().getId())
                        .distinct()
                        .filter(id -> !workerIsIdle(id, validSubTask.getId()))
                        .map(id -> WorkLogEventType.BoundEventType.bound(id, validSubTask.getId(), WorkLogEventType.STOP_TIMER))
                        .collect(Collectors.toList())
                ;
    }

    private boolean workerIsIdle(Integer workerId) {
        var eventCountMap = asMap(taskWorkLogRepository.getEventsCountForWorker(workerId));

        return eventCountMap.get(WorkLogEventType.STOP_TIMER).equals(eventCountMap.get(WorkLogEventType.START_TIMER));
    }

    private boolean workerIsIdle(Integer workerId, Integer subTaskId) {
        Optional<TaskWorklogEntity> lastWorkerTimerEvent = taskWorkLogRepository.lastEventForWorkerAndSubTask(workerId, subTaskId, TaskWorkLogRepository.TIMER_EVENTS);

        return (lastWorkerTimerEvent.isEmpty()) || lastWorkerTimerEvent.get().getEvent().equals(WorkLogEventType.STOP_TIMER);
    }

    private Integer subTaskWorkerWorkingOn(Integer workerId) {
        Integer result = 0;
        Optional<TaskWorklogEntity> lastWorkerTimerEvent = taskWorkLogRepository.lastEventForWorker(workerId, TaskWorkLogRepository.TIMER_EVENTS);

        if (lastWorkerTimerEvent.isPresent() && lastWorkerTimerEvent.get().getEvent().equals(WorkLogEventType.START_TIMER)) {
            result = lastWorkerTimerEvent.get().getSubTask().getId();
        }

        return result;
    }

    private void validateAttachment(File f) {
        if (!(f.getCreatedBy().equals(Integer.valueOf(LecwUtils.currentUser().getId())) &&
                f.getReferenceType().equals(ReferenceType.TEMPORARY.name()))) {
            throw TaskWorklogExceptions.attachmentIsNotValid(f.getId());
        }
    }

    private TaskWorklogRequest validateRequest(TaskWorklogRequest request, WorkLogEventType event) {
        Integer mainTaskId = request.getMainTaskId();
        Integer subTaskId = request.getSubTaskId();

        if (mainTaskId == null && subTaskId == null) {
            throw TaskWorklogExceptions.eitherMainTaskIdOrSubTaskIdMustBePresent();
        }

        Integer effectiveSubTaskId = subTaskId == null ? subTaskRepository.getDefaultSubTask(mainTaskId)
                .orElseThrow(() -> TaskExceptions.defaultSubTaskNotFound(mainTaskId))
                .getId() : subTaskId;

        Integer effectiveMainTaskId = mainTaskId == null ? subTaskRepository.getOne(subTaskId).getMainTask().getId() : mainTaskId;

        // If attachments are present and the current message supports attachments (we do not check for first start_timer here, only on save)
        if (request.getAttachmentIDs() != null && event.supportsAttachments()) {
            fileRepository.findAllById(request.getAttachmentIDs()).forEach(this::validateAttachment);
        }

        TaskWorklogRequest validRequest = request.withTaskIds(effectiveMainTaskId, effectiveSubTaskId);

        DTOValidator.validateAndThrow(validRequest);

        return validRequest;
    }

    private boolean userCanCompleteProjectTask(Integer userId, Integer projectId) {
        return securityHelper.checkIfUserHasProjectPermission(userId, projectId, PermissionName.TASK_COMPLETE);
    }

    private void validateMainTaskEvent(WorkLogEventType event, TaskStatus mainTaskStatus) {
        if (!event.isMainTaskEvent()) {
            throw TaskWorklogExceptions.canNotLogSubTaskEventForMainTask();
        }

        if (!event.validCurrentStatus(mainTaskStatus)) {
            throw TaskWorklogExceptions.canNotSaveEventInStatus(event, mainTaskStatus);
        }
    }

    private Map<WorkLogEventType, Long> asMap(List<WorkLogEventCounter> counters) {
        return completeMap(
                counters.stream()
                        .collect(Collectors.toMap(
                                WorkLogEventCounter::getEventType,
                                WorkLogEventCounter::getCount
                        ))
        );
    }

    private Map<WorkLogEventType, Long> completeMap(Map<WorkLogEventType, Long> incompleteMap) {
        Map<WorkLogEventType, Long> completeMap = new EnumMap<>(WorkLogEventType.class);
        for (WorkLogEventType type : WorkLogEventType.values()) {
            completeMap.put(type, 0L);
        }
        completeMap.putAll(incompleteMap);
        return ImmutableMap.copyOf(completeMap);
    }

    private TaskStatusDTO statusDTOFrom(TaskWorklogEntity worklogEntity, MainTaskEntity mainTaskEntity) {
        return new TaskStatusDTO(
                mainTaskEntity.getId(),
                mainTaskEntity.getStatus(),
                mainTaskEntity.getDueDate().atZone(ZoneId.of(ZoneOffset.UTC.getId())),
                subTaskRepository.listSubTasksForMainTask(mainTaskEntity.getId()).stream()
                        .collect(Collectors.toMap(
                                SubTaskEntity::getSubTaskNumber,
                                SubTaskEntity::getStatus
                        )),
                worklogEntity.getId(),
                worklogEntity.getComment(),
                mapToListOfMediaWidgetFiles(worklogEntity.getAttachmentIDs(), mainTaskEntity)
        );
    }

    private List<MediaWidgetFileDTO> mapToListOfMediaWidgetFiles(List<TaskWorklogAttachmentEntity> attachmentIDs, MainTaskEntity mainTaskEntity) {
        return attachmentIDs.stream().map(a ->
                mediaWidgetService.findByFileId(ReferenceType.MAIN_TASK, mainTaskEntity.getId(), a.getFile().getId())
        ).collect(Collectors.toList());
    }

    private void updateStatusIfNeeded(MainTaskEntity mainTask, SubTaskEntity subTask, WorkLogEventType eventType) {
        Map<WorkLogEventType, Long> mainTaskEventCounts = asMap(taskWorkLogRepository.getEventsCountForMainTask(mainTask.getId()));
        Map<WorkLogEventType, Long> subTaskEventCounts = subTask == null ? null : asMap(taskWorkLogRepository.getEventsCountForSubTask(subTask.getId()));

        updateSubTaskStatusIfNeeded(eventType, subTask, subTaskEventCounts);

        updateMainTaskStatusIfNeeded(eventType, mainTask, mainTaskEventCounts);
    }

    private void updateSubTaskStatusIfNeeded(WorkLogEventType eventType, SubTaskEntity subTaskEntity, Map<WorkLogEventType, Long> subTaskEventCounts) {
        if (subTaskEntity == null) {
            return;
        }

        TaskStatus newSubTaskStatus = calculateNewSubTaskStatus(subTaskEntity.getStatus(), eventType, subTaskEventCounts);
        if (!newSubTaskStatus.equals(subTaskEntity.getStatus())) {
            subTaskEntity.setStatus(newSubTaskStatus);

            subTaskRepository.save(subTaskEntity);
            publishTaskStatusChangedEvent(eventType,
                    subTaskEntity.getMainTask().getProjectId(),
                    subTaskEntity.getMainTask().getId(),
                    subTaskEntity.getId(),
                    false);
        }
    }

    private void updateMainTaskStatusIfNeeded(WorkLogEventType eventType, MainTaskEntity mainTaskEntity, Map<WorkLogEventType, Long> mainTaskEventCounts) {
        TaskStatus newMainTaskStatus = calculateNewMainTaskStatus(mainTaskEntity.getStatus(), eventType, mainTaskEventCounts);

        if (!newMainTaskStatus.equals(mainTaskEntity.getStatus())) {
            mainTaskEntity.setStatus(newMainTaskStatus);
            mainTaskRepository.save(mainTaskEntity);

            publishTaskCompletedEventIfCompleted(mainTaskEntity.getId(), newMainTaskStatus);
            publishTaskStatusChangedEvent(eventType,
                    mainTaskEntity.getProjectId(),
                    mainTaskEntity.getId(),
                    mainTaskEntity.getId(),
                    true);
        }
    }

    private void publishTaskStatusChangedEvent(WorkLogEventType eventType, Integer projectId, Integer referenceId, Integer subjectId, boolean isMainTask) {
        List<NotificationType> notificationTypes = new ArrayList<>();
        notificationTypes.add(isMainTask ? NotificationType.MAIN_TASK_STATUS_CHANGED : NotificationType.SUB_TASK_STATUS_CHANGED);

        switch (eventType) {
            case START_TASK:
                notificationTypes.add(isMainTask ? NotificationType.MAIN_TASK_STARTED : NotificationType.SUB_TASK_STARTED);
                break;
            case COMPLETE_SUB_TASK:
            case COMPLETE_CONTRACTOR_TASK:
            case COMPLETE_TASK:
                notificationTypes.add(isMainTask ? NotificationType.MAIN_TASK_COMPLETED: NotificationType.SUB_TASK_COMPLETED);
                break;
            case REJECT_CONTRACTOR_TASK:
            case REJECT_TASK:
                notificationTypes.add(isMainTask ? NotificationType.MAIN_TASK_REJECTED : NotificationType.SUB_TASK_REJECTED);
                break;
            default:
                break;
        }
        notificationTypes.forEach(notificationType -> eventsEmitter
                .emitNotification(NotificationEvent.builder()
                        .notificationType(notificationType)
                        .senderId(securityHelper.currentUserId())
                        .projectId(projectId)
                        .referenceId(referenceId)
                        .subjectId(subjectId)
                        .build()));
    }

    private void publishTaskCompletedEventIfCompleted(Integer taskId, TaskStatus newMainTaskStatus) {
        if (newMainTaskStatus.equals(TaskStatus.COMPLETED)) {
            eventPublisher.publishEvent(new TaskCompletedEvent(taskId));
        }
    }

    private TaskStatus calculateNewSubTaskStatus(TaskStatus status, WorkLogEventType eventType, Map<WorkLogEventType, Long> subTaskEventCounters) {
        if (eventType.isMainTaskEvent()) {
            return status;
        }

        if (eventType.equals(WorkLogEventType.STOP_TIMER) && status.equals(TaskStatus.IN_PROGRESS)) {
            return isPaused(subTaskEventCounters) ? TaskStatus.PAUSED : TaskStatus.IN_PROGRESS;
        }

        if (
                eventType.sameAs(WorkLogEventType.START_TIMER) &&
                        (status.equals(TaskStatus.ASSIGNED) || status.equals(TaskStatus.ACCEPTED) ||
                                status.equals(TaskStatus.PAUSED) || (status.equals(TaskStatus.COMPLETED)))
        ) {
            return TaskStatus.IN_PROGRESS;
        }

        if (eventType.equals(WorkLogEventType.COMPLETE_SUB_TASK) && status.equals(TaskStatus.PAUSED)) {
            return TaskStatus.COMPLETED;
        }

        return status;
    }

    private TaskStatus calculateNewMainTaskStatus(TaskStatus status, WorkLogEventType eventType, Map<WorkLogEventType, Long> mainTaskEventCounters) {
        if (eventType.isMainTaskEvent()) {
            return eventType.validCurrentStatus(status) ? eventType.nextStatus(status) : status;
        }

        if (eventType.equals(WorkLogEventType.STOP_TIMER) && status.equals(TaskStatus.IN_PROGRESS)) {
            return isPaused(mainTaskEventCounters) ? TaskStatus.PAUSED : TaskStatus.IN_PROGRESS;
        }
        if (eventType.sameAs(WorkLogEventType.START_TIMER) && eventType.validCurrentStatus(status)) {
            return TaskStatus.IN_PROGRESS;
        }

        return status;
    }

    private boolean isPaused(Map<WorkLogEventType, Long> taskEventCounters) {
        return taskEventCounters.get(WorkLogEventType.STOP_TIMER).equals(taskEventCounters.get(WorkLogEventType.START_TIMER));
    }

    // Events

    @EventListener
    @Transactional
    public void onApplicationEvent(ContractStateChangeEvent event) {
        switch (event.getEvent()) {
            case OFFER_ACCEPTED:
                moveContactTasksToAcceptedState(
                        event.getEventInitiatorId(),
                        event.getContractTaskIds(),
                        WorkLogEventType.CONTRACT_ACCEPTED,
                        event.getNegatedTaskIds(),
                        WorkLogEventType.CONTRACT_TASK_NEGATED
                );
                break;
            case OFFER_SELF_ACCEPTED:
                moveContactTasksToAcceptedState(
                        event.getEventInitiatorId(),
                        event.getContractTaskIds(),
                        WorkLogEventType.CONTRACT_SELF_ACCEPTED,
                        event.getNegatedTaskIds(),
                        WorkLogEventType.CONTRACT_TASK_NEGATED
                );
                break;
            default:
                // do nothing
        }
    }

    private void moveContactTasksToAcceptedState(
            Integer eventInitiatorId,
            List<Integer> contractTaskIds,
            WorkLogEventType taskAddedEventType,
            List<Integer> negatedTaskIds,
            WorkLogEventType taskNegatedEventType
    ) {
        contractTaskIds.forEach(taskId -> createMainTaskWorkLog(
                taskAddedEventType,
                eventInitiatorId,
                TaskWorklogRequest
                        .builder()
                        .mainTaskId(taskId)
                        .build()));
        negatedTaskIds.forEach(taskId -> createMainTaskWorkLog(
                taskNegatedEventType,
                eventInitiatorId,
                TaskWorklogRequest
                        .builder()
                        .mainTaskId(taskId)
                        .build()));
    }
}
