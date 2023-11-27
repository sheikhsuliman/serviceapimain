package com.siryus.swisscon.api.tasks;

import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.siryus.swisscon.api.auth.permission.PermissionName;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.catalog.Constants;
import com.siryus.swisscon.api.catalog.SnpHelper;
import com.siryus.swisscon.api.catalog.dto.CatalogNodeDTO;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeEntity;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeRepository;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationRepository;
import com.siryus.swisscon.api.company.company.CompanyRepository;
import com.siryus.swisscon.api.company.companytrade.CompanyTradeRepository;
import com.siryus.swisscon.api.contract.TaskToContractResolver;
import com.siryus.swisscon.api.event.ProjectContractorCompanyEvent;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.location.location.Location;
import com.siryus.swisscon.api.location.location.LocationService;
import com.siryus.swisscon.api.mediawidget.MediaConstants;
import com.siryus.swisscon.api.mediawidget.MediaWidgetFileDTO;
import com.siryus.swisscon.api.mediawidget.MediaWidgetQueryScope;
import com.siryus.swisscon.api.mediawidget.MediaWidgetService;
import com.siryus.swisscon.api.project.project.ProjectRepository;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRole;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleRepository;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleService;
import com.siryus.swisscon.api.specification.Specification;
import com.siryus.swisscon.api.specification.SpecificationRepository;
import com.siryus.swisscon.api.tasks.dto.AssignCompanyToTaskRequest;
import com.siryus.swisscon.api.tasks.dto.CompanyCatalogItemDTO;
import com.siryus.swisscon.api.tasks.dto.CreateContractualTaskRequest;
import com.siryus.swisscon.api.tasks.dto.CreateDirectorialTaskRequest;
import com.siryus.swisscon.api.tasks.dto.CreateSubTaskRequest;
import com.siryus.swisscon.api.tasks.dto.ListTaskIdsRequest;
import com.siryus.swisscon.api.tasks.dto.ListTasksRequest;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import com.siryus.swisscon.api.tasks.dto.SpecificationDTO;
import com.siryus.swisscon.api.tasks.dto.SubTaskDTO;
import com.siryus.swisscon.api.tasks.dto.TaskChecklistItem;
import com.siryus.swisscon.api.tasks.dto.TaskStatus;
import com.siryus.swisscon.api.tasks.dto.UpdateDirectorialTaskRequest;
import com.siryus.swisscon.api.tasks.entity.MainTaskEntity;
import com.siryus.swisscon.api.tasks.entity.SubTaskEntity;
import com.siryus.swisscon.api.tasks.entity.SubTaskUserEntity;
import com.siryus.swisscon.api.tasks.entity.TaskType;
import com.siryus.swisscon.api.tasks.exceptions.TaskExceptions;
import com.siryus.swisscon.api.tasks.repos.MainTaskRepository;
import com.siryus.swisscon.api.tasks.repos.MainTaskSpecification;
import com.siryus.swisscon.api.tasks.repos.SubTaskCheckListRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskUserRepository;
import com.siryus.swisscon.api.taskworklog.entity.TaskWorklogEntity;
import com.siryus.swisscon.api.taskworklog.repos.TaskWorkLogRepository;
import com.siryus.swisscon.api.util.ValidationUtils;
import com.siryus.swisscon.api.util.counter.ReferenceBasedCounterFactory;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.api.util.validator.Reference;
import com.siryus.swisscon.soa.EventsEmitter;
import com.siryus.swisscon.soa.notification.contract.NotificationEvent;
import com.siryus.swisscon.soa.notification.contract.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Validated
public class BasicMainTaskService implements MainTaskService {
    private static final List<PermissionName> TASK_MANAGER_PERMISSIONS = Arrays.asList(
            PermissionName.TASK_CREATE_UPDATE_SUB,
            PermissionName.TASK_ARCHIVE_SUB,
            PermissionName.TASK_TEAM_SET_USER,
            PermissionName.TASK_TEAM_ARCHIVE,
            PermissionName.TASK_CONTRACTOR_REVIEW
    );

    private final ReferenceBasedCounterFactory counterFactory;

    private final SubTaskCheckListRepository subTaskCheckListRepository;
    private final LocationService locationService;

    private final MainTaskRepository mainTaskRepository;
    private final SubTaskRepository subTaskRepository;
    private final SpecificationRepository specificationRepository;
    private final CatalogNodeRepository catalogNodeRepository;
    private final CatalogVariationRepository catalogVariationRepository;
    private final CompanyRepository companyRepository;
    private final CompanyTradeRepository companyTradeRepository;
    private final SubTaskUserRepository subTaskUserRepository;
    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final ProjectRepository projectRepository;
    private final TaskWorkLogRepository taskWorkLogRepository;
    private final MediaWidgetService mediaWidgetService;
    private final SecurityHelper securityHelper;
    private final UserRepository userRepository;
    private final ProjectUserRoleService projectUserRoleService;
    private final TaskToContractResolver taskToContractResolver;
    private final EventsEmitter eventsEmitter;

    @Autowired
    public BasicMainTaskService(
            ReferenceBasedCounterFactory counterFactory,
            SubTaskCheckListRepository subTaskCheckListRepository,
            LocationService locationService,
            MainTaskRepository mainTaskRepository,
            SubTaskRepository subTaskRepository,
            SpecificationRepository specificationRepository,
            CatalogNodeRepository catalogNodeRepository,
            CatalogVariationRepository catalogVariationRepository,
            CompanyRepository companyRepository,
            CompanyTradeRepository companyTradeRepository,
            SubTaskUserRepository subTaskUserRepository,
            ProjectUserRoleRepository projectUserRoleRepository,
            ProjectRepository projectRepository,
            TaskWorkLogRepository taskWorkLogRepository,
            MediaWidgetService mediaWidgetService,
            SecurityHelper securityHelper,
            UserRepository userRepository,
            ProjectUserRoleService projectUserRoleService,
            TaskToContractResolver taskToContractResolver,
            EventsEmitter eventsEmitter) {
        this.counterFactory = counterFactory;
        this.subTaskCheckListRepository = subTaskCheckListRepository;
        this.locationService = locationService;
        this.mainTaskRepository = mainTaskRepository;
        this.subTaskRepository = subTaskRepository;
        this.specificationRepository = specificationRepository;
        this.catalogNodeRepository = catalogNodeRepository;
        this.catalogVariationRepository = catalogVariationRepository;
        this.companyRepository = companyRepository;
        this.companyTradeRepository = companyTradeRepository;
        this.subTaskUserRepository = subTaskUserRepository;
        this.projectUserRoleRepository = projectUserRoleRepository;
        this.projectRepository = projectRepository;
        this.taskWorkLogRepository = taskWorkLogRepository;
        this.mediaWidgetService = mediaWidgetService;
        this.securityHelper = securityHelper;
        this.userRepository = userRepository;
        this.projectUserRoleService = projectUserRoleService;
        this.taskToContractResolver = taskToContractResolver;
        this.eventsEmitter = eventsEmitter;
    }

    @Override
    public List<MainTaskDTO> listMainTasksForLocation(Integer locationId) {
        return toMainTaskDTOs(findAllMainTasksForLocation(locationId));
    }

    @Override
    public Map<Integer, List<Integer>> listMainAndSubTaskIdsForLocation(Integer locationId, ListTaskIdsRequest request) {
        return listSubTaskIdsInMainTaskIds(
                findAllMainTasksForLocation(locationId)
                        .stream()
                        .filter(t -> isInTradeIds(t.getGlobalCatalogNodeId(), request))
                        .map(MainTaskEntity::getId)
                        .collect(Collectors.toList()));
    }

    private boolean isInTradeIds(Integer tradeId, ListTaskIdsRequest request) {
        if (request.getTradeIds() != null && !request.getTradeIds().isEmpty()) {
            return request.getTradeIds().contains(tradeId);
        }
        return true;
    }

    @Transactional
    @Override
    public MainTaskDTO createContractualTask(@Valid CreateContractualTaskRequest request) {
        validateCreateContractualTaskRequest(request);

        CatalogVariationEntity companyCatalogVariation = catalogVariationRepository.save(
                createCompanyCatalogVariation(request)
        );

        Specification specification = specificationRepository.save(
                createSpecificationEntity(companyCatalogVariation, request)
        );

        final MainTaskDTO mainTaskDTO = toMainTaskDTO(
                createSystemFolders(
                        materializeTask(
                                createContractualTaskEntity(specification, request),
                                specification.getCompany().getId(),
                                request.getTaskTeam())
                )
        );

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .companyId(mainTaskDTO.getCompanyId())
                .projectId(mainTaskDTO.getProjectId())
                .notificationType(NotificationType.MAIN_TASK_CREATED)
                .referenceId(mainTaskDTO.getId())
                .subjectId(mainTaskDTO.getId())
                .senderId(securityHelper.currentUserId())
                .build());

        return mainTaskDTO;
    }

    @Transactional
    @Override
    public MainTaskDTO createDirectorialTask(@Valid CreateDirectorialTaskRequest request) {
        validateCreateDirectorialTaskRequest(request);

        final MainTaskDTO mainTaskDTO = toMainTaskDTO(
                materializeAttachments(
                        materializeTask(
                                createDirectorialTaskEntity(request),
                                request.getCompanyId(),
                                request.getTaskTeam()
                        ),
                        request.getAttachmentIDs()
                )
        );

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .companyId(mainTaskDTO.getCompanyId())
                .projectId(mainTaskDTO.getProjectId())
                .notificationType(NotificationType.MAIN_TASK_CREATED)
                .referenceId(mainTaskDTO.getId())
                .subjectId(mainTaskDTO.getId())
                .senderId(securityHelper.currentUserId())
                .build());

        return mainTaskDTO;
    }

    @Transactional
    @Override
    public MainTaskDTO setTemplateFlag( @NotNull @Reference(ReferenceType.MAIN_TASK) Integer taskId, boolean templateOnOff) {
        var task = validTask(taskId);

        task.setTemplate(templateOnOff);

        return toMainTaskDTO(task);
    }

    @Override
    public List<MainTaskDTO> listTemplatesInProject(@NotNull @Reference(ReferenceType.PROJECT) Integer projectId) {
        return toMainTaskDTOs(mainTaskRepository.listTemplatesInProject(projectId));
    }

    @Transactional
    @Override
    public MainTaskDTO updateDirectorialTask(
            @NotNull @Reference(ReferenceType.MAIN_TASK) Integer taskId,
            @Valid UpdateDirectorialTaskRequest request
    ) {
        var task = validateUpdateDirectorialTaskRequest(taskId, request);

        return toMainTaskDTO(
                updateMainTask(task, request)
        );
    }

    @Override
    @Transactional
    public MainTaskDTO assignCompanyToTask(
            @Reference(ReferenceType.MAIN_TASK) Integer taskId,
            @Valid AssignCompanyToTaskRequest request
    ) {
        var task = validTask(taskId);

        validateCompanyCanBeAssignedToTask(task);

        if (task.getCompanyId() != null) {
            unAssignWorkersFromMainTaskTeam(taskId);
        }

        task.setCompanyId(request.getCompanyId());
        task.setStatus(TaskStatus.ASSIGNED);
        assignWorkersToMainTaskTeam(request.getCompanyId(), task.getProjectId(), task.getId(), request.getTaskTeam());

        eventsEmitter.emitCacheUpdate(ReferenceType.MAIN_TASK, task.getId());
        return getMainTask(taskId);
    }

    @Override
    @Transactional
    public MainTaskDTO unAssignCompanyFromTask(
            @Reference(ReferenceType.MAIN_TASK) Integer taskId
    ) {
        var task = validTask(taskId);

        validateCompanyCanBeUnAssignedFromTask(task);

        task.setCompanyId(null);
        task.setStatus(TaskStatus.OPEN);

        unAssignWorkersFromMainTaskTeam(taskId);

        return getMainTask(taskId);
    }

    private void unAssignWorkersFromMainTaskTeam(Integer mainTaskId) {
        var defaultSubTask = subTaskRepository.getDefaultSubTask(mainTaskId)
                .orElseThrow(() -> TaskExceptions.defaultSubTaskNotFound(mainTaskId));

        subTaskUserRepository.deleteAllUsersBySubTask(defaultSubTask.getId());
    }

    private void validateCompanyCanBeUnAssignedFromTask(MainTaskEntity task) {
        if (taskToContractResolver.taskContractId(task.getId()).isPresent()) {
            throw TaskExceptions.canNotChangeCompanyAssignmentForContractedTask(task.getId());
        }

        if (!task.getStatus().equals(TaskStatus.ASSIGNED)) {
            throw TaskExceptions.canNotChangeCompanyAssignmentForTaskInProgress(task.getId());
        }
    }

    private void validateCompanyCanBeAssignedToTask(MainTaskEntity task) {
        if (taskToContractResolver.taskContractId(task.getId()).isPresent()) {
            throw TaskExceptions.canNotChangeCompanyAssignmentForContractedTask(task.getId());
        }

        if ((!task.getStatus().equals(TaskStatus.OPEN)) && (!task.getStatus().equals(TaskStatus.ASSIGNED))) {
            throw TaskExceptions.canNotChangeCompanyAssignmentForTaskInProgress(task.getId());
        }
    }

    @Override
    public List<SubTaskDTO> listSubTasks(Integer mainTaskId) {
        return toDTOs(subTaskRepository.listSubTasksForMainTask(mainTaskId));
    }

    @Override
    public List<Integer> listSubTaskIds(Integer mainTaskId) {
        return subTaskRepository
                .listSubTasksForMainTask(mainTaskId)
                .stream()
                .map(SubTaskEntity::getId)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Integer, List<Integer>> listSubTaskIdsInMainTaskIds(List<Integer> mainTaskIds) {
        final List<SubTaskEntity> subTaskEntities = subTaskRepository.listSubTasksInMainTasks(mainTaskIds);
        return mainTaskIds.stream()
                .collect(Collectors.toMap(id -> id,
                        id -> subTaskEntities.stream().filter(ste -> ste.getMainTask().getId().equals(id))
                                .map(SubTaskEntity::getId)
                                .collect(Collectors.toList())));
    }

    @Override
    public Map<Integer, List<Integer>> listSubTaskIdsInMainTaskIds(List<Integer> mainTaskIds, @Valid ListTaskIdsRequest request) {
        return listSubTaskIdsInMainTaskIds(
                mainTaskRepository.findAllById(mainTaskIds)
                        .stream()
                        .filter(t -> isInTradeIds(t.getGlobalCatalogNodeId(), request))
                        .distinct()
                        .map(MainTaskEntity::getId)
                        .collect(Collectors.toList()));
    }

    @Transactional
    @Override
    public synchronized SubTaskDTO createSubTask(@Valid CreateSubTaskRequest request) {
        SubTaskEntity subTaskEntity = createSubTaskEntity(request);
        SubTaskEntity savedSubTaskEntity = subTaskRepository.save(subTaskEntity);

        assignWorkersToSubTaskTeam(subTaskEntity.getMainTask().getCompanyId(),
                subTaskEntity.getMainTask().getProjectId(),
                subTaskEntity.getId(),
                request.getTaskTeam());

        mediaWidgetService.createDefaultFolders(ReferenceType.SUB_TASK, savedSubTaskEntity.getId());
        final SubTaskDTO subTaskDTO = toDTO(savedSubTaskEntity);

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .companyId(subTaskEntity.getMainTask().getCompanyId())
                .projectId(subTaskEntity.getMainTask().getProjectId())
                .notificationType(NotificationType.SUB_TASK_CREATED)
                .referenceId(subTaskEntity.getMainTask().getId())
                .subjectId(subTaskDTO.getId())
                .senderId(securityHelper.currentUserId())
                .build());
        return subTaskDTO;
    }

    @Override
    public @Nullable MainTaskDTO updateDueDate(Integer taskId, LocalDateTime dueDate) {
        MainTaskEntity task = this.mainTaskRepository.findById(taskId).orElse(null);
        if (null == task) {
            return null;
        }
        task.setDueDate(dueDate);
        this.mainTaskRepository.save(task);
        eventsEmitter.emitCacheUpdate(ReferenceType.MAIN_TASK, task.getId());
        return toMainTaskDTO(task);
    }

    @Override
    public MainTaskDTO getMainTask(Integer taskId) {
        return toMainTaskDTO(
                validTask(taskId)
        );
    }

    @Override
    public List<MainTaskDTO> getMainTasksById(List<Integer> taskIds) {
        return toMainTaskDTOs(mainTaskRepository.findAllById(taskIds));
    }

    @Override
    public SubTaskDTO getSubTask(Integer subTaskId) {
        return toDTO(
                subTaskRepository.findById(subTaskId)
                    .orElseThrow(() -> TaskExceptions.subTaskNotFound(subTaskId))
        );
    }

    @Override
    public void deleteTask(Integer taskId) {
        Optional<MainTaskEntity> result = mainTaskRepository.findById(taskId);

        if (result.isEmpty()) {
            throw TaskExceptions.mainTaskNotFound(taskId);
        }

        MainTaskEntity mainTaskEntity = result.get();

        List<SubTaskEntity> subTaskEntities = subTaskRepository.listSubTasksForMainTask(taskId);
        if (subTaskEntities.size() > 1) {
            throw TaskExceptions.canNotDeleteNotEmptyTask(taskId);
        } else if (subTaskEntities.size() == 1) {
            SubTaskEntity defaultSubTask = subTaskEntities.get(0);

            validateSubTaskIsEmpty(defaultSubTask);

            subTaskRepository.archive(defaultSubTask.getId());
        }

        mainTaskRepository.archive(mainTaskEntity.getId());

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .companyId(mainTaskEntity.getCompanyId())
                .projectId(mainTaskEntity.getProjectId())
                .notificationType(NotificationType.MAIN_TASK_ARCHIVED)
                .referenceId(mainTaskEntity.getId())
                .subjectId(mainTaskEntity.getId())
                .senderId(securityHelper.currentUserId())
                .build());
    }

    @Override
    public void deleteSubTask(Integer subTaskId) {
        Optional<SubTaskEntity> result = subTaskRepository.findById(subTaskId);

        if (result.isEmpty()) {
            throw TaskExceptions.subTaskNotFound(subTaskId);
        }

        SubTaskEntity subTaskEntity = result.get();

        validateSubTaskIsEmpty(subTaskEntity);

        subTaskRepository.archive(subTaskEntity.getId());

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .companyId(subTaskEntity.getMainTask().getCompanyId())
                .projectId(subTaskEntity.getMainTask().getProjectId())
                .notificationType(NotificationType.SUB_TASK_ARCHIVED)
                .referenceId(subTaskEntity.getMainTask().getId())
                .subjectId(subTaskEntity.getId())
                .senderId(securityHelper.currentUserId())
                .build());
    }

    @Override
    public List<MainTaskDTO> listMainTasksForProject(Integer projectID, @Valid ListTasksRequest request) {
        return toMainTaskDTOs(listMainTaskEntitiesForProject(projectID, request));
    }

    private List<MainTaskEntity> listMainTaskEntitiesForProject(Integer projectID, @Valid ListTasksRequest request) {
        validateListTasksRequest(projectID);

        List<Integer> locationIDs = calculateAllLocationIDs(request.getLocationIDs());
        List<Integer> companyIDs = calculateAllCompanyIDs(request.getCompanyIDs(), request.getTradeIDs());

        if (!request.getTradeIDs().isEmpty() && companyIDs.isEmpty()) {
            return Collections.emptyList();
        }

        List<MainTaskEntity> tasks;

        if (request.getUserID() != null) {
            tasks = findMainTasksForUser(
                    request.getUserID(),
                    projectID,
                    request.getStatuses(),
                    request.getSnpIDs(),
                    locationIDs,
                    companyIDs,
                    request.getFromDate(), request.getToDate(),
                    request.getTaskNumber(),
                    request.getTitle(),
                    request.getIsDisabled()
            );
        }
        else {
            tasks = findMainTasksForProject(
                    projectID,
                    request.getStatuses(),
                    request.getSnpIDs(),
                    locationIDs,
                    companyIDs,
                    request.getFromDate(),
                    request.getToDate(),
                    request.getTaskNumber(),
                    request.getTitle(),
                    request.getIsDisabled()
            );
        }

        return tasks;
    }

    @Override
    public Map<Integer, List<Integer>> listMainAndSubTaskIdsForProject(Integer projectID, @Valid ListTaskIdsRequest request) {
        return listSubTaskIdsInMainTaskIds(
                listMainTaskEntitiesForProject(projectID, ListTasksRequest.builder().build())
                        .stream()
                        .filter(t -> isInTradeIds(t.getGlobalCatalogNodeId(), request))
                        .map(MainTaskEntity::getId)
                        .collect(Collectors.toList()));
    }

    @Override
    public List<MainTaskDTO> listMainTasksForProjectAndCompanyOrUnassigned(Integer projectId, Integer companyId) {
        return toMainTaskDTOs(mainTaskRepository.listTasksForProjectAndCompanyOrUnassigned(projectId, companyId));
    }

    private MainTaskEntity updateMainTask(MainTaskEntity task, UpdateDirectorialTaskRequest request) {
        task.setLocationId(request.getLocationId());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        task.setTimeBudgetMinutes(request.getTimeBudgetMinutes());
        task.setMaterialsAndMachines(request.getMaterialsAndMachines());
        task.setGlobalCatalogNodeId(request.getTradeCatalogNodeId());
        task.setTemplate(request.getTemplate());

        eventsEmitter.emitCacheUpdate(ReferenceType.MAIN_TASK, task.getId());
        return task;
    }

    private MainTaskEntity materializeTask(MainTaskEntity entity, Integer companyId, List<Integer> taskTeam) {
        MainTaskEntity newMainTask = mainTaskRepository.save(entity);

        subTaskRepository.save(createDefaultSubTask(newMainTask));

        if (companyId != null) {
            assignWorkersToMainTaskTeam(companyId, newMainTask.getProjectId(), newMainTask.getId(), taskTeam);
        }

        mediaWidgetService.createDefaultFolders(ReferenceType.MAIN_TASK, entity.getId());

        return newMainTask;
    }

    private MainTaskEntity materializeAttachments(MainTaskEntity taskEntity, List<Integer> attachmentIds) {
        createSystemFolders(taskEntity);

        MediaWidgetFileDTO descriptionFolder = mediaWidgetService.getSystemFolderByName(ReferenceType.MAIN_TASK, taskEntity.getId(), MediaConstants.DESCRIPTION_FOLDER)
                .orElseThrow(() -> TaskExceptions.systemFolderForTaskNotFound(MediaConstants.DESCRIPTION_FOLDER, taskEntity.getId()));

        attachmentIds.forEach( a ->
                mediaWidgetService.convertTemporaryFileToSystemFile(a, ReferenceType.MAIN_TASK, taskEntity.getId(), descriptionFolder.getId()));

        return taskEntity;
    }

    private List<MediaWidgetFileDTO> listTaskDescriptionAttachments(MainTaskEntity mainTaskEntity) {
        Optional<MediaWidgetFileDTO> descriptionFolder = mediaWidgetService.getSystemFolderByName(ReferenceType.MAIN_TASK, mainTaskEntity.getId(), MediaConstants.DESCRIPTION_FOLDER);

        return descriptionFolder.isEmpty() ? Collections.emptyList() : mediaWidgetService.listChildren(descriptionFolder.get().getId(), MediaWidgetQueryScope.all());
    }

    private List<MainTaskEntity> findMainTasksForProject(
            Integer projectID,
            List<TaskStatus> statuses,
            List<String> snpIDs,
            List<Integer> locationIDs,
            List<Integer> companyIDs,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String taskNumber,
            String title,
            Boolean disabled) {
        return mainTaskRepository.findAll(
                MainTaskSpecification.withProject(projectID)
                        .and(MainTaskSpecification.withStatuses(statuses))
                        .and(MainTaskSpecification.withCatalogItems(snpIDs))
                        .and(MainTaskSpecification.withLocations(locationIDs))
                        .and(MainTaskSpecification.withCompanies(companyIDs))
                        .and(MainTaskSpecification.afterDate(fromDate))
                        .and(MainTaskSpecification.beforeDate(toDate))
                        .and(MainTaskSpecification.taskNumberStarts(taskNumber))
                        .and(MainTaskSpecification.titleStarts(title))
                        .and(MainTaskSpecification.disabled(disabled))
        );
    }

    private List<MainTaskEntity> findMainTasksForUser(
            Integer userID,
            Integer projectID,
            List<TaskStatus> statuses,
            List<String> snpIDs,
            List<Integer> locationIDs,
            List<Integer> companyIDs,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String taskNumber,
            String title,
            Boolean disabled) {
        List<Integer> thisUserTaskIDs = subTaskUserRepository.findByUserAndProject(userID, projectID).stream().map( s -> s.getSubTask().getMainTask().getId()).collect(Collectors.toList());

        if (thisUserTaskIDs.isEmpty()) {
            return Collections.emptyList();
        }

        return mainTaskRepository.findAll(
                MainTaskSpecification.withIDs(
                        thisUserTaskIDs
                )
                    .and(MainTaskSpecification.withProject(projectID))
                    .and(MainTaskSpecification.withStatuses(statuses))
                    .and(MainTaskSpecification.withCatalogItems(snpIDs))
                    .and(MainTaskSpecification.withLocations(locationIDs))
                    .and(MainTaskSpecification.withCompanies(companyIDs))
                    .and(MainTaskSpecification.afterDate(fromDate))
                    .and(MainTaskSpecification.beforeDate(toDate))
                    .and(MainTaskSpecification.taskNumberStarts(taskNumber))
                    .and(MainTaskSpecification.titleStarts(title))
                    .and(MainTaskSpecification.disabled(disabled))
        );
    }

    List<MainTaskEntity> findAllMainTasksForLocation(Integer locationId) {
        Location location = locationService.getValidLocation(locationId);

        if (location == null) {
            throw TaskExceptions.locationNotFound(locationId);
        }

        List<Integer> locationAndDescendents = locationService.getAllLocationsByParent(locationId).stream().map(Location::getId).collect(Collectors.toList());

        return mainTaskRepository.listTasksForLocations(locationAndDescendents);
    }

    private List<Integer> calculateAllLocationIDs(List<Integer> locationIDs) {
        return locationIDs.stream()
                .map(locationService::getDescendentIDsByParent)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Integer> calculateAllCompanyIDs(List<Integer> companyIDs, List<Integer> tradeIDs) {
        if (companyIDs.isEmpty() && tradeIDs.isEmpty()) {
            return Collections.emptyList();
        }
        if (! companyIDs.isEmpty() && ! tradeIDs.isEmpty()) {
            Set<Integer> distinctCompanies = new HashSet<>(companyIDs);
            distinctCompanies.addAll(companyTradeRepository.listCompaniesWithGivenTrades(tradeIDs));
            return new ArrayList<>(distinctCompanies);
        }
        if(! companyIDs.isEmpty()) {
            return companyIDs;
        }
        else {
            return companyTradeRepository.listCompaniesWithGivenTrades(tradeIDs);
        }
    }

    private void validateListTasksRequest(Integer projectID) {
        if (!projectRepository.existsById(projectID)) {
            throw TaskExceptions.projectNotFound(projectID);
        }
    }

    private void validateCreateContractualTaskRequest(CreateContractualTaskRequest request) {
        validateCreateTaskTeam(request.getTaskTeam());
    }

    private void validateCreateTaskTeam(List<Integer> taskTeam) {
        if(taskTeam != null && !taskTeam.isEmpty()) {
            taskTeam.forEach(securityHelper::validateUserIsPartOfCurrentUserCompany);
        }
    }

    private MainTaskEntity validateUpdateDirectorialTaskRequest(Integer taskId, UpdateDirectorialTaskRequest request) {
        var task = validTask(taskId);

        if (task.getStatus().isImmutable()) {
            throw TaskExceptions.canNotUpdateTaskInState(task.getId(), task.getStatus());
        }

        return task;
    }

    private MainTaskEntity validTask(Integer taskId) {
        return mainTaskRepository.findById(taskId)
                .orElseThrow(() -> TaskExceptions.mainTaskNotFound(taskId));
    }

    private void validateCreateDirectorialTaskRequest(CreateDirectorialTaskRequest request) {
        validateCreateTaskTeam(request.getTaskTeam());

        CatalogNodeEntity tradeNode = catalogNodeRepository.findById(request.getTradeCatalogNodeId())
                .orElseThrow(() -> TaskExceptions.catalogItemNotFound(request.getTradeCatalogNodeId()));

        if (tradeNode.getDisabled() != null) {
            throw TaskExceptions.catalogItemDeleted(request.getTradeCatalogNodeId());
        }

        if (tradeNode.getParentSnp() != null) {
            throw TaskExceptions.catalogItemIsNotTrade(request.getTradeCatalogNodeId());
        }
    }

    private CatalogVariationEntity createCompanyCatalogVariation(CreateContractualTaskRequest request) {
        String snpNumber = request.getCompanyCatalogItem().getSnpNumber();
        CatalogNodeEntity catalogNodeEntity = catalogNodeRepository.findLatestWithSnp(snpNumber)
                .orElseThrow( () -> TaskExceptions.catalogItemNotFound(snpNumber));
        CatalogVariationEntity globalCatalogVariation = catalogVariationRepository.findVariation(
                Constants.GLOBAL_CATALOG_COMPANY_ID,
                catalogNodeEntity.getId(),
                request.getCompanyCatalogItem().getVariationNumber()
        )
                .orElseThrow(() -> TaskExceptions.catalogItemNotFound(snpNumber));

        return CatalogVariationEntity.builder()
                .snp(snpNumber)
                .catalogNodeId(
                        catalogNodeEntity
                        .getId()
                )
                .companyId(request.getCompanyCatalogItem().getCompanyId())
                .variationNumber(request.getCompanyCatalogItem().getVariationNumber())
                .active(true)
                .taskName(globalCatalogVariation.getTaskName())
                .taskVariation(request.getCompanyCatalogItem().getCompanyDetails())
                .unit(globalCatalogVariation.getUnit())
                .build();
    }

    private SubTaskEntity createSubTaskEntity(CreateSubTaskRequest request) {
        return new SubTaskEntity(
                mainTaskRepository.getOne(request.getMainTaskId()),
                subTaskRepository.getMaxSubTaskNumber(request.getMainTaskId()) + 1,
                request.getTitle(),
                request.getDescription(),
                request.getTimeBudgetMinutes()
        );
    }

    private MainTaskEntity createContractualTaskEntity(Specification specification, CreateContractualTaskRequest request) {
        return new MainTaskEntity(
                TaskType.CONTRACTUAL,
                counterFactory.counter(ReferenceType.PROJECT, specification.getProject().getId(), TaskConstants.TASK_COUNTER).getNextValue(),
                specification.getLocation().getId(),
                specification.getLocation().getProject().getId(),
                specification.getCompany().getId(),
                specification,
                specification.getCompanyCatalogVariation().getCatalogNodeId(),
                request.getTitle(),
                request.getDescription(),
                request.getStartDate(),
                request.getDueDate(),
                request.getTimeBudgetMinutes(),
                null,
                TaskStatus.ASSIGNED,
                request.isTemplate()
        );
    }

    private MainTaskEntity createDirectorialTaskEntity(CreateDirectorialTaskRequest request) {
        Location location = locationService.getValidLocation(request.getLocationId());
        return new MainTaskEntity(
                TaskType.DIRECTORIAL,
                counterFactory.counter(ReferenceType.PROJECT, location.getProject().getId(), TaskConstants.TASK_COUNTER).getNextValue(),
                location.getId(),
                location.getProject().getId(),
                request.getCompanyId(),
                null,
                request.getTradeCatalogNodeId(),
                request.getTitle(),
                request.getDescription(),
                request.getStartDate(),
                request.getDueDate(),
                request.getTimeBudgetMinutes(),
                request.getMaterialsAndMachines(),
                calculateTaskStatus(request),
                request.isTemplate()
        );
    }

    private SubTaskEntity createDefaultSubTask(MainTaskEntity mainTaskEntity) {
        return new SubTaskEntity(
                mainTaskEntity,
                0,
                null,
                null
        );
    }

    private Specification createSpecificationEntity(CatalogVariationEntity companyCatalogItem, CreateContractualTaskRequest request) {
        Location location = locationService.getValidLocation(request.getLocationId());

        return Specification.builder()
                .variation(request.getSpecification().getVariation())
                .amount(request.getSpecification().getAmount())
                .price(request.getSpecification().getPrice())
                .companyCatalogVariation(companyCatalogItem)
                .company(companyRepository.getOne(companyCatalogItem.getCompanyId()))
                .project(location.getProject())
                .location(location)
                .build();
    }

    private void validateSubTaskIsEmpty(SubTaskEntity subTaskEntity) {
        if (!taskWorkLogRepository.getEventsCountForSubTask(subTaskEntity.getId()).isEmpty()) {
            throw TaskExceptions.canNotDeleteNotEmptyTask(subTaskEntity.getId());
        }
    }

    private List<MainTaskDTO> toMainTaskDTOs(List<MainTaskEntity> tasks) {
        return tasks.stream().map( this::toMainTaskDTO).collect(Collectors.toList());
    }

    @Override
    public MainTaskDTO toMainTaskDTO(MainTaskEntity task) {
        Specification specification = task.getSpecification();
        CatalogVariationEntity companyCatalogItem = specification == null ? null : specification.getCompanyCatalogVariation();
        SubTaskEntity defaultSubTask = subTaskRepository.getDefaultSubTask(task.getId())
                .orElseThrow(() -> TaskExceptions.defaultSubTaskNotFound(task.getId()));

        return new MainTaskDTO(
                task.getId(),
                task.getCreatedBy(),
                task.getCreatedDate(),
                task.getTaskType(),
                task.getTaskNumber(),
                task.getProjectId(),
                task.getCompanyId(),
                locationService.calculatePath(task.getLocationId()),
                SpecificationDTO.from(specification),
                CompanyCatalogItemDTO.from(companyCatalogItem),
                task.getTitle(),
                task.getDescription(),
                calculateTrade(task.getGlobalCatalogNodeId()),
                task.getStatus(),
                calculateRejectDTO(task),
                task.getStartDate(),
                task.getDueDate(),
                task.getTimeBudgetMinutes(),
                toDTOs(
                    subTaskRepository.listSubTasksForMainTask(task.getId()).stream()
                        .filter(s -> s.getSubTaskNumber() != 0)
                        .collect(Collectors.toList())
                ),
                toDTO(
                    subTaskRepository.getDefaultSubTask(task.getId())
                        .orElseThrow(() -> TaskExceptions.defaultSubTaskNotFound(task.getId()))
                ),
                calculateSubTaskTeam(defaultSubTask.getId()),
                task.getMaterialsAndMachines(),
                listTaskDescriptionAttachments(task),
                taskToContractResolver.taskContractId(task.getId()).orElse(null),
                task.isTemplate()
        );
    }

    private CatalogNodeDTO calculateTrade(Integer globalCatalogId) {
        if (globalCatalogId == null) {
            return null;
        }
        CatalogNodeEntity catalogNodeEntity = catalogNodeRepository
                .findById(globalCatalogId)
                .orElseThrow(() -> TaskExceptions.catalogItemNotFound(globalCatalogId));

        String tradeSnp = SnpHelper.topSnp(catalogNodeEntity.getSnp());
        return CatalogNodeDTO.from(
                catalogNodeRepository.findLatestWithSnp(tradeSnp)
                        .orElseThrow( () -> TaskExceptions.catalogItemNotFound(tradeSnp))
                , false
        );
    }
    private List<Integer> calculateSubTaskTeam(Integer subTaskId) {
        return subTaskUserRepository.findUsersBySubTask(subTaskId).stream()
                .map( User::getId ).collect(Collectors.toList());
    }

    private MainTaskDTO.RejectDTO calculateRejectDTO(MainTaskEntity task) {
        if (TaskStatus.COMPLETED.equals(task.getStatus())) {
            return null;
        }

        Optional<TaskWorklogEntity> lastReject = taskWorkLogRepository.getLastMainTaskWorkLog(task.getId(), TaskConstants.REJECT_TASK_SET);

        if(lastReject.isPresent()) {
            TaskWorklogEntity lastTaskWorkLog = lastReject.get();
            return new MainTaskDTO.RejectDTO(
                    lastTaskWorkLog.getTimestamp(), lastTaskWorkLog.getComment(),
                    lastTaskWorkLog.getAttachmentIDs().stream().map(
                            id -> mediaWidgetService.findByFileId(ReferenceType.MAIN_TASK, task.getId(), id.getFile().getId())
                    ).collect(Collectors.toList())
            );
        }
        return null;
    }

    private List<SubTaskDTO> toDTOs(List<SubTaskEntity> subTasks) {
        return subTasks.stream().map( this::toDTO).collect(Collectors.toList());
    }

    private SubTaskDTO toDTO(SubTaskEntity subTask) {
        List<TaskChecklistItem> subTaskCheckListDTOListItem = subTaskCheckListRepository.findAllBySubTaskId(subTask.getId())
                .stream().map(TaskChecklistItem::fromEntity).collect(Collectors.toList());
        return new SubTaskDTO(
                subTask.getId(),
                subTask.getCreatedBy(),
                subTask.getCreatedDate(),
                subTask.getMainTask().getId(),
                subTask.getMainTask().getCompanyId(),
                locationService.calculatePath(subTask.getMainTask().getLocationId()),
                subTask.getSubTaskNumber(),
                subTask.getTitle(),
                subTask.getDescription(),
                subTask.getStatus(),
                subTaskCheckListDTOListItem,
                subTask.getTimeBudgetMinutes(),
                calculateSubTaskTeam(subTask.getId())
            );
    }

    private MainTaskEntity createSystemFolders(MainTaskEntity mainTaskEntity) {
        mediaWidgetService.createSystemFolders(ReferenceType.MAIN_TASK, mainTaskEntity.getId());
        
        return mainTaskEntity;
    }


    private void assignWorkersToMainTaskTeam(Integer contractorCompanyId, Integer projectId, Integer mainTaskId, List<Integer> workerIds) {
        var defaultSubTask = subTaskRepository.getDefaultSubTask(mainTaskId)
                .orElseThrow(() -> TaskExceptions.defaultSubTaskNotFound(mainTaskId));
        List<Integer> managers = addProjectManagerToSubTask(contractorCompanyId, projectId, defaultSubTask.getId());
        Optional.ofNullable(workerIds).ifPresent(wIds-> wIds.stream()
                .filter(wId->!managers.contains(wId))
                .forEach(wId->addWorkerToSubTask(wId, projectId, contractorCompanyId, defaultSubTask.getId())));
    }

    private void assignWorkersToSubTaskTeam(Integer contractorCompanyId, Integer projectId, Integer subTaskId, List<Integer> workerIds) {
        List<Integer> managers = addProjectManagerToSubTask(contractorCompanyId, projectId, subTaskId);
        Optional.ofNullable(workerIds).ifPresent(wIds-> wIds.stream()
                .filter(wId->!managers.contains(wId))
                .forEach(wId->addWorkerToSubTask(wId, projectId, contractorCompanyId, subTaskId)));
    }

    private List<Integer> addProjectManagerToSubTask(Integer contractorCompanyId, Integer projectId, Integer subTaskId) {
        return getCompanyProjectManagers(contractorCompanyId, projectId).stream()
                .peek(workerId -> addValidUserToSubTaskIfNeeded(workerId, subTaskId))
                .collect(Collectors.toList());
    }

    private void addWorkerToSubTask(Integer workerId, Integer projectId, Integer contractorCompanyId, Integer subTaskId) {
        addUserToProjectIfNeeded(workerId, projectId, contractorCompanyId);
        addValidUserToSubTaskIfNeeded(workerId, subTaskId);
    }

    private void addUserToProjectIfNeeded(Integer workerId, Integer projectId, Integer contractorCompanyId) {
        List<ProjectUserRole> projectUserRoles = projectUserRoleRepository.findByUserAndProject(workerId, projectId);

        if (projectUserRoles.isEmpty()) {
            projectUserRoles.add(projectUserRoleService.addCompanyUserToProject(workerId, projectId));
        }

        if (projectUserRoles.stream()
                .noneMatch(pur -> pur.getProjectCompany().getCompany().getId().equals(contractorCompanyId))) {
            throw TaskExceptions.userNotInCompany(workerId, contractorCompanyId);
        }
    }

    private void addValidUserToSubTaskIfNeeded(Integer workerId, Integer subTaskId) {
        if (subTaskUserRepository.findUsersBySubTask(subTaskId).stream()
                .map(User::getId).noneMatch(id -> Objects.equals(id, workerId))) {
            subTaskUserRepository.save(
                    SubTaskUserEntity.builder()
                            .user(userRepository.getOne(workerId))
                            .subTask(SubTaskEntity.ref(subTaskId))
                            .createdBy(Integer.parseInt(LecwUtils.currentUser().getId()))
                            .createdDate(LocalDateTime.now())
                            .build()
            );
        }
    }

    private List<Integer> getCompanyProjectManagers(Integer companyId, Integer projectId) {
        return ValidationUtils.throwIfEmpty(
                SecurityHelper.uniqUserIdsFromRoles(securityHelper.getProjectMembersFromCompanyWithPermissions(projectId, companyId, TASK_MANAGER_PERMISSIONS)),
                () -> TaskExceptions.canNotLocateProjectManager(companyId, projectId)
        );
    }
    
    private TaskStatus calculateTaskStatus(CreateDirectorialTaskRequest request) {
        if (request.isRequiresContract()) {
            return TaskStatus.DRAFT;
        } 
        
        if (request.getCompanyId() == null) {
            return TaskStatus.OPEN;
        }
        
        return TaskStatus.ASSIGNED;
    }

    // Events

    @EventListener
    @Transactional
    public void onApplicationEvent(ProjectContractorCompanyEvent event) {
        event.getTaskIds().forEach(
                tId -> {
                    var defaultSubTask = subTaskRepository.getDefaultSubTask(tId)
                            .orElseThrow(() -> TaskExceptions.defaultSubTaskNotFound(tId));
                    addProjectManagerToSubTask(event.getContractorCompanyId(), event.getProjectId(), defaultSubTask.getId());
                    validTask(defaultSubTask.getMainTask().getId()).setCompanyId(event.getContractorCompanyId());
                }
        );
    }
}
