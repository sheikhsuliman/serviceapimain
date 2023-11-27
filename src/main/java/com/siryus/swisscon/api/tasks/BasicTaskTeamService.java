package com.siryus.swisscon.api.tasks;

import com.siryus.swisscon.api.auth.permission.PermissionName;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleService;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleRepository;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleService;
import com.siryus.swisscon.api.tasks.entity.MainTaskEntity;
import com.siryus.swisscon.api.tasks.entity.SubTaskEntity;
import com.siryus.swisscon.api.tasks.entity.SubTaskUserEntity;
import com.siryus.swisscon.api.tasks.exceptions.TaskExceptions;
import com.siryus.swisscon.api.tasks.repos.MainTaskRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskUserRepository;
import com.siryus.swisscon.api.taskworklog.entity.TaskWorklogEntity;
import com.siryus.swisscon.api.taskworklog.repos.TaskWorkLogRepository;
import com.siryus.swisscon.api.util.StreamUtils;
import com.siryus.swisscon.api.util.ValidationUtils;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.soa.EventsEmitter;
import com.siryus.swisscon.soa.notification.contract.NotificationEvent;
import com.siryus.swisscon.soa.notification.contract.NotificationType;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BasicTaskTeamService implements TaskTeamService {
    private final MainTaskRepository mainTaskRepository;
    private final SubTaskRepository subTaskRepository;
    private final SubTaskUserRepository subTaskUserRepository;
    private final TaskWorkLogRepository taskWorkLogRepository;
    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final BasicMainTaskService mainTaskService;
    private final UserRepository userRepository;
    private final CompanyUserRoleService companyUserRoleService;
    private final ProjectUserRoleService projectUserRoleService;
    private final EventsEmitter eventsEmitter;

    private final SecurityHelper securityHelper;

    @Autowired
    public BasicTaskTeamService(
            MainTaskRepository mainTaskRepository,
            SubTaskRepository subTaskRepository,
            SubTaskUserRepository subTaskUserRepository,
            TaskWorkLogRepository taskWorkLogRepository,
            ProjectUserRoleRepository projectUserRoleRepository,
            BasicMainTaskService mainTaskService,
            UserRepository userRepository,
            CompanyUserRoleService companyUserRoleService, ProjectUserRoleService projectUserRoleService, EventsEmitter eventsEmitter, SecurityHelper securityHelper
    ) {
        this.mainTaskRepository = mainTaskRepository;
        this.subTaskRepository = subTaskRepository;
        this.subTaskUserRepository = subTaskUserRepository;
        this.taskWorkLogRepository = taskWorkLogRepository;
        this.projectUserRoleRepository = projectUserRoleRepository;
        this.mainTaskService = mainTaskService;
        this.userRepository = userRepository;
        this.companyUserRoleService = companyUserRoleService;
        this.projectUserRoleService = projectUserRoleService;
        this.eventsEmitter = eventsEmitter;
        this.securityHelper = securityHelper;
    }

    @Override
    public List<TeamUserDTO> getLocationTeam(Integer locationId) {
        return mainTaskService.findAllMainTasksForLocation(locationId).stream()
                .map(mt -> subTaskRepository.listSubTasksForMainTask(mt.getId())).flatMap(List::stream)
                .map(st -> getSubTaskTeam(st.getId())).flatMap(List::stream)
                .filter(StreamUtils.distinctByKey(TeamUserDTO::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<TeamUserDTO> getMainTaskTeam(Integer taskId) {
        // retrieve sub tasks of main task > at least one sub task has to exist
        SubTaskEntity defaultSubTask = subTaskRepository.getDefaultSubTask(taskId)
                .orElseThrow( () -> TaskExceptions.defaultSubTaskNotFound(taskId));

        // retrieve main task and project id
        Integer projectId =  mainTaskRepository.getProjectIdForTask(taskId)
                .orElseThrow(() -> TaskExceptions.mainTaskNotFound(taskId));

        // retrieve all distinct user ids which are involved in subtasks
        List<Integer> userIds = subTaskUserRepository.findUsersBySubTask(defaultSubTask.getId())
                .stream().map(User::getId).collect(Collectors.toList());

        // retrieve all project user roles and convert them into team user dto
        return getTeamUserDTOS(projectId, userIds);

    }

    @Override
    public List<TeamUserDTO> getSubTaskTeam(Integer subTaskId) {
        // retrieve sub task and project id
        SubTaskEntity subTaskEntity = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> TaskExceptions.subTaskNotFound(subTaskId));
        Integer projectId = subTaskEntity.getMainTask().getProjectId();

        List<Integer> userIds = subTaskUserRepository.findUsersBySubTask(subTaskEntity.getId())
                .stream()
                .map(User::getId)
                .collect(Collectors.toList());

        // retrieve all project user roles and convert them into team user dto
        return getTeamUserDTOS(projectId, userIds);
    }

    @Override
    public List<TeamUserDTO> getMainTaskAvailableUsers(Integer taskId) {
        // retrieve sub tasks of main task > at least one sub task has to exist
        SubTaskEntity defaultSubTask = subTaskRepository.getDefaultSubTask(taskId)
                .orElseThrow( () -> TaskExceptions.defaultSubTaskNotFound(taskId));

        return getSubTaskAvailableUsers(defaultSubTask.getId());
    }

    @Override
    public List<TeamUserDTO> getMainTaskAvailableUsersOnCreation(Integer projectId, Integer companyId) {
        securityHelper.validateUserIsPartOfCompany(securityHelper.currentUserId(), companyId);
        securityHelper.validateUserIsPartOfProject(securityHelper.currentUserId(), projectId);
        return availableUsersByProjectAndCompany(projectId, companyId, Collections.emptySet());
    }

    @Override
    public List<TeamUserDTO> getSubTaskAvailableUsers(Integer subTaskId) {
        SubTaskEntity subTaskEntity = subTaskRepository.findById(subTaskId).orElseThrow(() -> TaskExceptions.subTaskNotFound(subTaskId));

        MainTaskEntity mainTaskEntity = subTaskEntity.getMainTask();

        if (mainTaskEntity.getCompanyId() == null) {
            return Collections.emptyList();
        }

        Set<Integer> taskUserIds = subTaskUserRepository.findUsersBySubTask(subTaskId).stream()
                .map(User::getId).collect(Collectors.toSet());
        return availableUsersByProjectAndCompany(mainTaskEntity.getProjectId(), mainTaskEntity.getCompanyId(), taskUserIds);
    }

    private List<TeamUserDTO> availableUsersByProjectAndCompany(Integer projectId, Integer companyId, Set<Integer> taskUserIds) {
        Set<Integer> projectUserIds = projectUserRoleService.getProjectTeamByCompany(projectId, companyId)
                .stream().map(TeamUserDTO::getId).collect(Collectors.toSet());
        List<TeamUserDTO> companyUserDTOs = new ArrayList<>(companyUserRoleService.getCompanyTeam(companyId).getTeam());

        return getAvailableUsersToAddToTask(projectId, taskUserIds, projectUserIds, companyUserDTOs);
    }

    private List<TeamUserDTO> getAvailableUsersToAddToTask(Integer projectId, Set<Integer> taskUserIds,
                                                           Set<Integer> projectUserIds,
                                                           List<TeamUserDTO> companyUserDTOs) {
        List<TeamUserDTO> availableUsersToAdd = companyUserDTOs.stream()
                .filter(dto -> !taskUserIds.contains(dto.getId()) && projectUserIds.contains(dto.getId()))
                .map(dto -> dto.inProject(true))
                .collect(Collectors.toList());

        if (currentUserCanAddUsersToProject(projectId)) {
            List<TeamUserDTO> availableCompanyUsers = companyUserDTOs.stream()
                    .filter(dto -> !taskUserIds.contains(dto.getId()) && !projectUserIds.contains(dto.getId()))
                    .map(dto -> dto.inProject(false))
                    .collect(Collectors.toList());

            availableUsersToAdd.addAll(availableCompanyUsers);
        }
        return availableUsersToAdd;
    }

    @Transactional
    @Override
    public void addUserToSubTask(Integer userId, Integer subTaskId) {
        // Check that the user exists
        User u = userRepository.findById(userId)
                .orElseThrow(() -> TaskExceptions.userNotFound(userId));

        // Check that the subtask exists
        SubTaskEntity subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> TaskExceptions.subTaskNotFound(subTaskId));

        internalAddUserToSubTask(u, subTask);
        eventsEmitter.emitNotification(NotificationEvent.builder()
                .notificationType(NotificationType.SUB_TASK_USER_ADDED)
                .senderId(securityHelper.currentUserId())
                .projectId(subTask.getMainTask().getProjectId())
                .companyId(subTask.getMainTask().getCompanyId())
                .referenceId(subTaskId)
                .subjectId(userId)
                .build());
    }

    @Transactional
    @Override
    public void addUserToTask(Integer userId, Integer taskId) {
        // Check that the user exists
        User u = userRepository.findById(userId)
                .orElseThrow(() -> TaskExceptions.userNotFound(userId));

        // Check that the task exists
        MainTaskEntity task = mainTaskRepository.findById(taskId)
                .orElseThrow(() -> TaskExceptions.mainTaskNotFound(taskId));

        SubTaskEntity sue =  subTaskRepository.getDefaultSubTask(task.getId()).orElseThrow(
                () -> TaskExceptions.defaultSubTaskNotFound(taskId)
        );


        internalAddUserToSubTask(u, sue);
        eventsEmitter.emitNotification(NotificationEvent.builder()
                .notificationType(NotificationType.MAIN_TASK_USER_ADDED)
                .senderId(securityHelper.currentUserId())
                .projectId(task.getProjectId())
                .referenceId(taskId)
                .subjectId(userId)
                .build());
    }

    public void removeMainTaskUser(Integer taskId, Integer userId) {
        // retrieve main task
        Optional<MainTaskEntity> mainTaskEntityOpt = mainTaskRepository.findById(taskId);
        if (mainTaskEntityOpt.isEmpty()) {
            throw TaskExceptions.mainTaskNotFound(taskId);
        }

        // retrieve sub tasks of main task > at least one sub task has to exist (default sub task)
        List<SubTaskEntity> subTaskEntities = subTaskRepository.listSubTasksForMainTask(taskId);
        if(subTaskEntities.isEmpty()) {
            throw TaskExceptions.defaultSubTaskNotFound(taskId);
        }

        // remove the user of the default sub task
        SubTaskEntity defaultSubTask = subTaskEntities.get(0);
        removeSubTaskUser(defaultSubTask.getId(), userId);

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .notificationType(NotificationType.MAIN_TASK_USER_REMOVED)
                .senderId(securityHelper.currentUserId())
                .projectId(defaultSubTask.getMainTask().getProjectId())
                .referenceId(taskId)
                .subjectId(userId)
                .build());
    }

    public void removeSubTaskUser(Integer subTaskId, Integer userId) {
        final SubTaskEntity subTaskEntity = removeSubTaskUserInternal(subTaskId, userId);
        eventsEmitter.emitNotification(NotificationEvent.builder()
                .notificationType(NotificationType.SUB_TASK_USER_REMOVED)
                .senderId(securityHelper.currentUserId())
                .projectId(subTaskEntity.getMainTask().getProjectId())
                .referenceId(subTaskId)
                .subjectId(userId)
                .build());
    }

    private SubTaskEntity removeSubTaskUserInternal(Integer subTaskId, Integer userId) {
        // retrieve sub task
        Optional<SubTaskEntity> subTaskEntityOpt = subTaskRepository.findById(subTaskId);
        if (subTaskEntityOpt.isEmpty()) {
            throw TaskExceptions.subTaskNotFound(subTaskId);
        }

        // retrieve sub task user
        SubTaskUserEntity subTaskUser = subTaskUserRepository.findByUserAndSubTask(userId, subTaskId)
                .orElseThrow(()-> TaskExceptions.userIsNotPartOfSubTaskTeam(userId, subTaskId));

        // if user has existing work logs > he cannot be deleted
        List<TaskWorklogEntity> taskWorkLogs = taskWorkLogRepository.findByWorkerAndSubTask(userId, subTaskId,  TaskWorkLogRepository.TIMER_EVENTS);
        if(!taskWorkLogs.isEmpty()) {
            throw TaskExceptions.canNotDeleteTaskUserWhichIsNotInTeam(userId, subTaskId);
        }

        subTaskUserRepository.deleteById(subTaskUser.getId());
        return subTaskEntityOpt.get();
    }

    private List<TeamUserDTO> getTeamUserDTOS(Integer projectId, List<Integer> userIds) {
        return userIds.isEmpty() ? Collections.emptyList() :
                new ArrayList<>(projectUserRoleRepository
                        .findByProjectAndUsers(projectId, userIds).stream()
                        .map(TeamUserDTO::fromProjectUserRole)
                        .collect(Collectors.toMap(TeamUserDTO::getId, zu -> zu,
                                (existing, replacement) ->
                                    existing.toBuilder().roleIds(
                                            ListUtils.union(existing.getRoleIds(), replacement.getRoleIds())
                                    ).build()
                        )).values());
    }

    private void internalAddUserToSubTask(User user, SubTaskEntity subTask) {
        Integer projectId = subTask.getMainTask().getProjectId();

        securityHelper.validateUserIsPartOfCompany(user.getId(), subTask.getMainTask().getCompanyId());

        addUserToProjectIfHasPermissions(user, projectId);

        securityHelper.validateUserIsPartOfProject(user.getId(), subTask.getMainTask().getProjectId());
        ValidationUtils.throwIf(subTaskUserRepository.findByUserAndSubTask(user.getId(), subTask.getId()).isPresent(),
                ()->TaskExceptions.userAlreadyExists(user.getId(), subTask.getId()));

        subTaskUserRepository.save(SubTaskUserEntity
                .builder()
                .user(user)
                .subTask(subTask)
                .build());
    }

    private void addUserToProjectIfHasPermissions(User user, Integer projectId) {
        if (!securityHelper.checkUserIsPartOfProject(user.getId(), projectId)) {
            projectUserRoleService.addCompanyUserToProject(user.getId(), projectId);
        }
    }

    private boolean currentUserCanAddUsersToProject(Integer projectId) {
        return securityHelper.checkIfUserHasProjectPermission(securityHelper.currentUserId(),
                projectId, PermissionName.PROJECT_TEAM_ADD_USER);
    }
}
