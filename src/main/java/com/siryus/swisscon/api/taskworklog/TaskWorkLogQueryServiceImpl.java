package com.siryus.swisscon.api.taskworklog;

import com.siryus.swisscon.api.auth.user.AuthorDTO;
import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.mediawidget.MediaWidgetFileDTO;
import com.siryus.swisscon.api.mediawidget.MediaWidgetService;
import com.siryus.swisscon.api.tasks.exceptions.TaskExceptions;
import com.siryus.swisscon.api.tasks.repos.SubTaskRepository;
import com.siryus.swisscon.api.taskworklog.dto.EventHistoryDTO;
import com.siryus.swisscon.api.taskworklog.dto.MainTaskDurationDTO;
import com.siryus.swisscon.api.taskworklog.dto.SubTaskDurationDTO;
import com.siryus.swisscon.api.taskworklog.dto.TaskWorklogDTO;
import com.siryus.swisscon.api.taskworklog.dto.TimeWorklogDTO;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventPerSubTaskCounter;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventType;
import com.siryus.swisscon.api.taskworklog.dto.WorkerDurationDTO;
import com.siryus.swisscon.api.taskworklog.dto.WorkerStatusDTO;
import com.siryus.swisscon.api.taskworklog.entity.TaskWorklogEntity;
import com.siryus.swisscon.api.taskworklog.exceptions.TaskWorklogExceptions;
import com.siryus.swisscon.api.taskworklog.repos.TaskHistoryRepository;
import com.siryus.swisscon.api.taskworklog.repos.TaskWorkLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class TaskWorkLogQueryServiceImpl implements TaskWorkLogQueryService {
    private final UserRepository userRepository;
    private final TaskWorkLogRepository taskWorkLogRepository;
    private final SubTaskRepository subTaskRepository;
    private final TaskHistoryRepository taskHistoryRepository;
    private final MediaWidgetService mediaWidgetService;

    @Autowired
    public TaskWorkLogQueryServiceImpl(UserRepository userRepository, TaskWorkLogRepository taskWorkLogRepository, SubTaskRepository subTaskRepository, TaskHistoryRepository taskHistoryRepository, MediaWidgetService mediaWidgetService) {
        this.userRepository = userRepository;
        this.taskWorkLogRepository = taskWorkLogRepository;
        this.subTaskRepository = subTaskRepository;
        this.taskHistoryRepository = taskHistoryRepository;
        this.mediaWidgetService = mediaWidgetService;
    }

    @Transactional
    @Override
    public TaskWorklogDTO getWorkLogById(Integer worklogId) {
        return TaskWorklogDTO.from(taskWorkLogRepository.findById(worklogId).orElse(null));
    }

    @Override
    public SubTaskDurationDTO getSubTaskDurations(Integer subTaskId) {
        return calculateSubTaskDurationDTO(
                subTaskId,
                getOneSubTaskWorkLogs(subTaskId, calculateWorkerDurations(taskWorkLogRepository.findBySubTask(subTaskId, TaskWorkLogRepository.TIMER_EVENTS)))
        );
    }

    @Override
    public MainTaskDurationDTO getMainTaskDurations(Integer mainTaskId) {
        Map<Integer, Map<Integer, List<TimeWorklogDTO>>> thisMainTaskWorkLogsBySubTask =
                getOneMainTaskWorkLogs(mainTaskId, calculateWorkerDurations(taskWorkLogRepository.findByMainTask(mainTaskId, TaskWorkLogRepository.TIMER_EVENTS)));

        MainTaskDurationDTO result = new MainTaskDurationDTO(
                mainTaskId,
                thisMainTaskWorkLogsBySubTask.keySet().stream().map(k -> calculateSubTaskDurationDTO(k, thisMainTaskWorkLogsBySubTask.get(k))).collect(Collectors.toList()),
                0L
        );

        result.setTotalDurationInSeconds(calculateTotalDurations(result));

        return result;
    }

    @Override
    public SubTaskDurationDTO getWorkerSubTaskDurations(Integer workerId, Integer subTaskId) {
        Map<Integer, List<TimeWorklogDTO>> thisSubTaskPerWorkerLogs =
                getOneSubTaskWorkLogs(subTaskId, calculateWorkerDurations(taskWorkLogRepository.findByWorkerAndSubTask(workerId, subTaskId, TaskWorkLogRepository.TIMER_EVENTS)));

        return calculateSubTaskDurationDTO(subTaskId, thisSubTaskPerWorkerLogs);
    }

    @Override
    public SubTaskDurationDTO getMainTaskDefaultSubTaskDurations(Integer mainTaskId) {
        return getSubTaskDurations(
                subTaskRepository.getDefaultSubTask(mainTaskId)
                        .orElseThrow( () -> TaskExceptions.defaultSubTaskNotFound(mainTaskId))
                        .getId()
        );
    }

    @Override
    public WorkerStatusDTO getWorkerStatus(Integer workerId) {
        Map<Integer, Long> startCountPerSubTaskMap = new HashMap<>();

        taskWorkLogRepository.getEventsCountForWorkerBySubTask(workerId).stream()
                .filter(e -> e.getEventType().sameAs(WorkLogEventType.START_TIMER) || e.getEventType().sameAs(WorkLogEventType.STOP_TIMER))
                .sorted(
                        Comparator.comparing(WorkLogEventPerSubTaskCounter::getSubTaskId)
                                .thenComparing(e -> e.getEventType().ordinal())) // START defined before STOP :)
                .forEach(
                w -> {
                    if (w.getEventType().equals(WorkLogEventType.START_TIMER)) {
                        startCountPerSubTaskMap.put(w.getSubTaskId(), w.getCount());
                    }
                    else {
                        Long startCount = startCountPerSubTaskMap.get(w.getSubTaskId());
                        if (Objects.equals(startCount,w.getCount())) {
                            startCountPerSubTaskMap.remove(w.getSubTaskId());
                        }
                    }
                }
        );
        return new WorkerStatusDTO(
                workerId,
                List.copyOf(startCountPerSubTaskMap.keySet())
        );
    }

    private List<MediaWidgetFileDTO> mapToListOfMediaWidgetFiles(TaskWorklogEntity entity, Integer mainTaskId) {
        if (entity.getAttachmentIDs() == null) {
            return Collections.emptyList();
        }
        
        return entity.getAttachmentIDs()
            .stream().map( 
                a -> mediaWidgetService.findByFileId(ReferenceType.MAIN_TASK, mainTaskId, a.getFile().getId())
            ).collect(Collectors.toList());
    }    
    
    @Override
    public Page<EventHistoryDTO> getWorklogHistoryForMainTaskPaged(Integer mainTaskId, Pageable pageable) {
        if (mainTaskId == null || mainTaskId <= 0) {
            throw TaskWorklogExceptions.taskIdIsMissing();
        }
               
        Page<TaskWorklogEntity> results = taskHistoryRepository.findByMainTask(
                mainTaskId,
                TaskHistoryRepository.MAIN_TASK_HISTORY_EVENTS,
                pageable
        );
        
        Set<Integer> startEventsIds = results.get()
                .filter(x -> x.getEvent().equals(WorkLogEventType.START_TIMER))
                .map(TaskWorklogEntity::getId)
                .collect(Collectors.toSet());

        Set<Integer> cancelledTimers = results.get()
                .filter(x -> x.getEvent().equals(WorkLogEventType.CANCEL_TIMER))
                .map(TaskWorklogEntity::getModifyingWorklogId)
                .collect(Collectors.toSet());

        HashMap<Integer, Double> durations = new HashMap<>();
        
        if (!startEventsIds.isEmpty()) {            
            taskHistoryRepository.findDurationsOfStartEvents(startEventsIds)
                    .forEach(r -> durations.put((Integer) r.get(0), (Double) r.get(1)));
        }

        return results.map(entity -> EventHistoryDTO.from(entity, mapToListOfMediaWidgetFiles(entity, mainTaskId), entity.getWorker(), durations, cancelledTimers));
    }

    @Override
    public TaskWorklogDTO getWorkerLastTimerWorklog(Integer workerId) {
        return TaskWorklogDTO.from(taskWorkLogRepository.lastEventForWorker(workerId, TaskWorkLogRepository.TIMER_EVENTS)
                .orElseThrow(() -> TaskWorklogExceptions.workerHasNoWorklogs(workerId))
        );
    }

    private SubTaskDurationDTO calculateSubTaskDurationDTO(Integer subTaskId, Map<Integer, List<TimeWorklogDTO>> thisSubTaskPerWorkerLogs) {
        final Map<Integer, AuthorDTO> authors = new HashMap<>();

        SubTaskDurationDTO result = new SubTaskDurationDTO(
                subTaskId,
                thisSubTaskPerWorkerLogs.keySet().stream().map(k -> new WorkerDurationDTO(
                        authors.computeIfAbsent(k, this::calculateAuthor),
                        thisSubTaskPerWorkerLogs.get(k),
                        0L
                )).collect(Collectors.toList()),
                0L
        );

        result.setTotalDurationInSeconds(calculateTotalDurations(result));

        return result;
    }


    private AuthorDTO calculateAuthor(Integer userId) {
        return AuthorDTO.from(
            userRepository.findById(userId)
                .orElseThrow(() -> TaskExceptions.userNotFound(userId))
        );
    }

    private Map<Integer, Map<Integer, List<TimeWorklogDTO>>> getOneMainTaskWorkLogs(Integer mainTaskId, Map<Integer, Map<Integer, Map<Integer, List<TimeWorklogDTO>>>> durationsMap) {
        return durationsMap.computeIfAbsent(mainTaskId, k -> Collections.emptyMap());
    }

    private Map<Integer, List<TimeWorklogDTO>> getOneSubTaskWorkLogs(Integer subTaskId, Map<Integer, Map<Integer, Map<Integer, List<TimeWorklogDTO>>>> durationsMap) {
        if (durationsMap.isEmpty()) {
            return Collections.emptyMap();
        }

        if (durationsMap.size() != 1) {
            throw TaskWorklogExceptions.inconsistentWorkLogData("Worklogs from single main task expected");
        }

        return durationsMap.entrySet().iterator().next().getValue().computeIfAbsent(subTaskId, k -> Collections.emptyMap());
    }

    private Map<Integer, Map<Integer, Map<Integer, List<TimeWorklogDTO>>>> calculateWorkerDurations(List<TaskWorklogEntity> worklogEntities) {
        Map<Integer, Map<Integer, Map<Integer, List<TimeWorklogDTO>>>> result = new HashMap<>();

        final AtomicInteger previous = new AtomicInteger(0); // does not have to be atomic, but has to be final

        Map<Integer, TimeWorklogDTO> startIdMap = new HashMap<>();

        worklogEntities.forEach(e -> {
            Map<Integer, Map<Integer, List<TimeWorklogDTO>>> thisMainTaskLogs = result.computeIfAbsent(e.getMainTask().getId(), k -> new HashMap<>());
            Map<Integer, List<TimeWorklogDTO>> thisSubTaskLogs = thisMainTaskLogs.computeIfAbsent(e.getSubTask().getId(), k -> new HashMap<>());
            List<TimeWorklogDTO> thisUserLogs = thisSubTaskLogs.computeIfAbsent(e.getWorker().getId(), k -> new ArrayList<>());

            validateOrder(previous, e.getId(), thisUserLogs, e.getEvent());

            if (e.getEvent().equals(WorkLogEventType.START_TIMER)) {
                TimeWorklogDTO startDTO = TimeWorklogDTO.builder()
                        .mainTaskId(e.getMainTask().getId())
                        .subTaskId(e.getSubTask().getId())
                        .workerId(e.getWorker().getId())
                        .startTime(e.getTimestamp())
                        .startTimeWorklogId(e.getId())
                        .build();
                thisUserLogs.add(startDTO);
                startIdMap.put(e.getId(), startDTO);
            } else if (e.getEvent().equals(WorkLogEventType.STOP_TIMER)) {
                TimeWorklogDTO lastLog = thisUserLogs.get(thisUserLogs.size() - 1);

                lastLog.setStopTime(e.getTimestamp());
                lastLog.setStopTimeWorklogId(e.getId());
            } else if (e.getEvent().equals(WorkLogEventType.CANCEL_TIMER)) {
                startIdMap.get(e.getModifyingWorklogId()).setCancelled(true);
            }
        });

        return result;
    }

    // TODO: Need to check if same user has more than one timer running on DIFFERENT sub-tasks.
    private void validateOrder(AtomicInteger previousId, Integer nextId, List<TimeWorklogDTO> thisUserLogs, WorkLogEventType event) {
        if (previousId.get() >= nextId) {
            throw TaskWorklogExceptions.inconsistentWorkLogData("Entries out of order " + previousId.get() + " >= " + nextId);
        }
        previousId.set(nextId);

        if (thisUserLogs.isEmpty()) {
            if (event.equals(WorkLogEventType.STOP_TIMER)) {
                throw TaskWorklogExceptions.inconsistentWorkLogData("STOP_TIMER out of order. Can not be first event");
            }
        } else {
            TimeWorklogDTO lastLog = thisUserLogs.get(thisUserLogs.size() - 1);
            if (event.equals(WorkLogEventType.STOP_TIMER) && lastLog.getStopTime() != null) {
                throw TaskWorklogExceptions.inconsistentWorkLogData("STOP_TIMER out of order. Two in a row");
            }
            if (event.equals(WorkLogEventType.START_TIMER) && lastLog.getStopTime() == null) {
                throw TaskWorklogExceptions.inconsistentWorkLogData("START_TIMER out of order. Two in a row");
            }
        }
    }

    private Long calculateTotalDurations(MainTaskDurationDTO dto) {
        AtomicLong result = new AtomicLong(0L);
        dto.getSubTasksDuration().forEach(d -> {
            d.setTotalDurationInSeconds(calculateTotalDurations(d));
            result.addAndGet(d.getTotalDurationInSeconds());
        });
        return result.get();
    }

    private Long calculateTotalDurations(SubTaskDurationDTO dto) {
        AtomicLong result = new AtomicLong(0L);
        dto.getWorkersWorklogs().forEach(d -> {
            d.setTotalDurationInSeconds(calculateTotalDurations(d));
            result.addAndGet(d.getTotalDurationInSeconds());
        });
        return result.get();
    }

    private Long calculateTotalDurations(WorkerDurationDTO dto) {
        return dto.getTimeWorklogs().stream().map(TimeWorklogDTO::getFullDurationInSeconds).reduce(0L, Long::sum);
    }

}
