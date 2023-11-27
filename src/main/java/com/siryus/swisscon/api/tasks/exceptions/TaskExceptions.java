package com.siryus.swisscon.api.tasks.exceptions;

import com.siryus.swisscon.api.exceptions.LocalizedReason;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;
import com.siryus.swisscon.api.tasks.dto.TaskStatus;

import static com.siryus.swisscon.api.exceptions.LocalizedReason.ParameterValue.pv;

public class TaskExceptions {
    private static int e(int n) {
        return LocalizedResponseStatusException.TASKS_ERROR_CODE + n;
    }

    private static final LocalizedReason PROJECT_NOT_FOUND = LocalizedReason.like(e(1), "Project with id: {{projectId}} not found");
    private static final LocalizedReason LOCATION_NOT_FOUND = LocalizedReason.like(e(2), "Location with id: {{locationId}} not found");
    private static final LocalizedReason MAIN_TASK_DOES_NOT_HAVE_DEFAULT_SUB_TASK = LocalizedReason.like(e(3), "Main task with id: {{taskId}} does not have default sub-task");
    private static final LocalizedReason MAIN_TASK_NOT_FOUND = LocalizedReason.like(e(4), "Main task with id: {{taskId}} not found");
    private static final LocalizedReason SUB_TASK_NOT_FOUND = LocalizedReason.like(e(6), "Sub task with id: {{subTaskId}} not found");
    private static final LocalizedReason USER_IS_NOT_PART_OF_SUB_TASK_TEAM = LocalizedReason.like(e(7), "User with id: {{userId}} is not part of team of sub task with id: {{subTaskId}}");
    private static final LocalizedReason USER_IS_NOT_PART_OF_MAIN_TASK_TEAM = LocalizedReason.like(e(8), "User with id: {{userId}} is not part of team of main task with id: {{taskId}}");
    private static final LocalizedReason CAN_NOT_DELETE_NON_EMPTY_TASK = LocalizedReason.like(e(9),  "Can not delete Main Task with id: {{taskId}}. Main Task Has Sub Tasks");
    private static final LocalizedReason CAN_NOT_DELETE_NON_EMPTY_SUB_TASK = LocalizedReason.like(e(10),  "Can not delete Sub Task with id: {{subTaskId}}. Sub Task Has Work Log Items");
    private static final LocalizedReason SUB_TASKS_CHECKLIST_NOT_FOUND = LocalizedReason.like(e(11),  "SubTask checklist item with id : {{subTaskCheckListId}} cannot be found.");
    private static final LocalizedReason SUB_TASKS_CHECKLIST_ALREADY_CHECKED = LocalizedReason.like(e(12),  "SubTask checklist with id : {{subTaskCheckListId}} is already checked.");
    private static final LocalizedReason CAN_NOT_DELETE_TASK_USER_WHICH_IS_NOT_IN_TEAM = LocalizedReason.like(e(13),  "Can not delete user with id: {{userId}}. Cause he isn't a team member of sub task with id: {{subTaskId}}");
    private static final LocalizedReason USER_NOT_FOUND = LocalizedReason.like(e(14),  "User with id {{userId}} was not found");
    private static final LocalizedReason USER_NOT_IN_COMPANY = LocalizedReason.like(e(15),  "User with id {{userId}} was not found in company with id {{companyId}}");
    private static final LocalizedReason USER_ALREADY_ON_TEAM = LocalizedReason.like(e(16),  "User with id {{userId}} is already assigned to sub-task of id {{subTaskId}}");
    private static final LocalizedReason ATTACHMENT_NOT_FOUND = LocalizedReason.like(e(17),  "Attachment with id {{attachmentId}} was not found");
    private static final LocalizedReason USER_IS_NOT_PART_OF_PROJECT = LocalizedReason.like(e(18),  "The user making the request {{userId}} is not part of the project team in project {{projectId}}");
    private static final LocalizedReason WORKER_NOT_ASSIGNED_TO_MAIN_TASK = LocalizedReason.like(e(19),  "Worker {workerId} is not assigned to main task {{mainTaskId}}");
    private static final LocalizedReason CATALOG_ITEM_NOT_FOUND_ID = LocalizedReason.like(e(20),  "Catalog Item with id: {{catalogItemId}} can not be found");
    private static final LocalizedReason CATALOG_ITEM_NOT_FOUND_SNP = LocalizedReason.like(e(21),  "Catalog Item snp: {{snp}} can not be found");
    private static final LocalizedReason CATALOG_ITEM_DELETED = LocalizedReason.like(e(22),  "Catalog Item with id: {{catalogItemId}} is deleted");
    private static final LocalizedReason CATALOG_ITEM_IS_NOT_TRADE = LocalizedReason.like(e(23),  "Catalog Item with id: {{catalogItemId}} is not a trade");
    private static final LocalizedReason CAN_NOT_LINK_TASKS_FROM_DIFFERENT_PROJECTS = LocalizedReason.like(e(24),  "Can not link tasks from different projects");
    private static final LocalizedReason TASK_LINK_NOT_FOUND = LocalizedReason.like(e(25),  "Task link with id: {{taskLinkId}} does not exist");
    private static final LocalizedReason CAN_NOT_LOCATE_PROJECT_MANAGER = LocalizedReason.like(e(26),  "Can not locate project manager for given company {{companyId}} + project {{projectId}}");
    private static final LocalizedReason SYSTEM_FOLDER_FOR_TASK_NOT_FOUND = LocalizedReason.like(e(27),  "System Folder {{folderName}} for task: {{taskId}} not found");
    private static final LocalizedReason COMPANY_IS_NOT_PART_OF_PROJECT = LocalizedReason.like(e(28),  "Company {{companyId}} is not part of project {{projectId}} team.");
    private static final LocalizedReason TASK_NOT_ASSIGNED_TO_A_COMPANY = LocalizedReason.like(e(29),  "Non-contractual task {{taskId}} is not yet assigned to a company.");
    private static final LocalizedReason COMMENT_NOT_FOUND = LocalizedReason.like(e(30),  "Comment with id {{commentId}} not found.");
    private static final LocalizedReason CAN_NOT_CHANGE_COMPANY_TASK_IN_CONTRACT = LocalizedReason.like(e(31),  "Can not change company assignment for task {{taskId}}. The task is part of contract.");
    private static final LocalizedReason CAN_NOT_CHANGE_COMPANY_TASK_IN_PROGRESS = LocalizedReason.like(e(32),  "Can not change company assignment for task {{taskId}}. The task is in progress.");
    private static final LocalizedReason CAN_NOT_UPDATE_TASK_WITH_STATUS = LocalizedReason.like(e(33), "Can not update task {{taskId}} with status {{taskStatus}}");

    public static LocalizedResponseStatusException locationNotFound( Integer locationId ) {
        return  LocalizedResponseStatusException.badRequest(LOCATION_NOT_FOUND.with(pv("locationId", locationId)));
    }

    public static LocalizedResponseStatusException mainTaskNotFound( Integer taskId) {
        return  LocalizedResponseStatusException.notFound(MAIN_TASK_NOT_FOUND.with(pv("taskId", taskId)));
    }

    public static LocalizedResponseStatusException defaultSubTaskNotFound( Integer taskId) {
        return  LocalizedResponseStatusException.notFound(MAIN_TASK_DOES_NOT_HAVE_DEFAULT_SUB_TASK.with(pv("taskId", taskId)));
    }

    public static LocalizedResponseStatusException subTaskNotFound( Integer subTaskId) {
        return  LocalizedResponseStatusException.notFound(SUB_TASK_NOT_FOUND.with(pv("subTaskId", subTaskId)));
    }

    public static LocalizedResponseStatusException canNotDeleteNotEmptyTask( Integer taskId) {
        return  LocalizedResponseStatusException.businessLogicError(CAN_NOT_DELETE_NON_EMPTY_TASK.with(pv("taskId", taskId)));
    }

    public static LocalizedResponseStatusException canNotDeleteNotEmptySubTask( Integer subTaskId) {
        return  LocalizedResponseStatusException.businessLogicError(CAN_NOT_DELETE_NON_EMPTY_SUB_TASK.with(pv("subTaskId", subTaskId)));
    }

    public static LocalizedResponseStatusException subTaskCheckListAlreadyChecked(Integer subTaskCheckListId) {
        return  LocalizedResponseStatusException.businessLogicError(SUB_TASKS_CHECKLIST_ALREADY_CHECKED.with(pv("subTaskCheckListId", subTaskCheckListId)));
    }

    public static LocalizedResponseStatusException subTaskCheckListNotFound(Integer subTaskCheckListId) {
        return  LocalizedResponseStatusException.notFound(SUB_TASKS_CHECKLIST_NOT_FOUND.with(pv("subTaskCheckListId", subTaskCheckListId)));
    }

      public static LocalizedResponseStatusException canNotDeleteTaskUserWhichIsNotInTeam( Integer userId, Integer subTaskId) {
          return  LocalizedResponseStatusException.badRequest(CAN_NOT_DELETE_TASK_USER_WHICH_IS_NOT_IN_TEAM.with(pv("userId", userId),pv("subTaskId", subTaskId)));
    }

    public static LocalizedResponseStatusException projectNotFound(Integer projectId) {
        return  LocalizedResponseStatusException.notFound(PROJECT_NOT_FOUND.with(pv("projectId", projectId)));
    }

    public static LocalizedResponseStatusException userIsNotPartOfSubTaskTeam(Integer userId, Integer subTaskId) {
        return  LocalizedResponseStatusException.businessLogicError(USER_IS_NOT_PART_OF_SUB_TASK_TEAM.with(pv("userId", userId),pv("subTaskId", subTaskId)));
    }

    public static LocalizedResponseStatusException userNotFound(Integer userId) {
        return  LocalizedResponseStatusException.notFound(USER_NOT_FOUND.with(pv("userId", userId)));
    }
    
    public static LocalizedResponseStatusException userNotInCompany(Integer userId, Integer companyId) {
        return LocalizedResponseStatusException.businessLogicError(USER_NOT_IN_COMPANY.with(pv("userId", userId), pv("companyId", companyId)));
    }    
    
    public static LocalizedResponseStatusException userAlreadyExists(Integer userId, Integer subTaskId) {
        return  LocalizedResponseStatusException.businessLogicError(USER_ALREADY_ON_TEAM.with(pv("userId", userId), pv("subTaskId", subTaskId)));
    }

    public static LocalizedResponseStatusException attachmentNotFound(Integer attachmentId) {
        return  LocalizedResponseStatusException.notFound(ATTACHMENT_NOT_FOUND.with(pv("attachmentId", attachmentId)));
    }
    
    public static LocalizedResponseStatusException userIsNotPartOfProjectTeam(Integer userId, Integer projectId) {
        return  LocalizedResponseStatusException.businessLogicError(USER_IS_NOT_PART_OF_PROJECT.with(pv("userId", userId), pv("projectId", projectId)));
    }

    public static LocalizedResponseStatusException catalogItemNotFound(String snp) {
        return  LocalizedResponseStatusException.notFound(CATALOG_ITEM_NOT_FOUND_SNP.with(pv("snp", snp)));
    }

    public static LocalizedResponseStatusException catalogItemNotFound(Integer catalogItemId) {
        return  LocalizedResponseStatusException.notFound(CATALOG_ITEM_NOT_FOUND_ID.with(pv("catalogItemId", catalogItemId)));
    }

    public static LocalizedResponseStatusException catalogItemDeleted(Integer catalogItemId) {
        return  LocalizedResponseStatusException.businessLogicError(CATALOG_ITEM_DELETED.with(pv("catalogItemId", catalogItemId)));
    }

    public static LocalizedResponseStatusException catalogItemIsNotTrade(Integer catalogItemId) {
        return  LocalizedResponseStatusException.businessLogicError(CATALOG_ITEM_IS_NOT_TRADE.with(pv("catalogItemId", catalogItemId)));
    }

    public static LocalizedResponseStatusException canNotLinkTasksFromDifferentProjects() {
        return  LocalizedResponseStatusException.businessLogicError(CAN_NOT_LINK_TASKS_FROM_DIFFERENT_PROJECTS.with());
    }

    public static LocalizedResponseStatusException noSuchTaskLink (Integer taskLinkId) {
        return  LocalizedResponseStatusException.notFound(TASK_LINK_NOT_FOUND.with(pv("taskLinkId", taskLinkId)));
    }

    public static LocalizedResponseStatusException canNotLocateProjectManager (Integer companyId, Integer projectId) {
        return  LocalizedResponseStatusException.businessLogicError(CAN_NOT_LOCATE_PROJECT_MANAGER.with(pv("companyId", companyId), pv("projectId", projectId)));
    }

    public static LocalizedResponseStatusException systemFolderForTaskNotFound (String folderName, Integer taskId) {
        return  LocalizedResponseStatusException.internalError(SYSTEM_FOLDER_FOR_TASK_NOT_FOUND.with(pv("folderName", folderName), pv("taskId", taskId)));
    }

    public static LocalizedResponseStatusException companyIsNotPartOfProject (Integer companyId, Integer projectId) {
        return  LocalizedResponseStatusException.businessLogicError(COMPANY_IS_NOT_PART_OF_PROJECT.with(pv("companyId", companyId), pv("projectId", projectId)));
    }

    public static LocalizedResponseStatusException taskNotAssignedToACompany(Integer taskId) {
        return LocalizedResponseStatusException.badRequest(TASK_NOT_ASSIGNED_TO_A_COMPANY.with(pv("taskId", taskId)));
    }

    public static LocalizedResponseStatusException commentNotFound(Integer commentId) {
        return LocalizedResponseStatusException.notFound(COMMENT_NOT_FOUND.with(pv("commentId", commentId)));
    }

    public static LocalizedResponseStatusException canNotChangeCompanyAssignmentForContractedTask(Integer taskId) {
        return LocalizedResponseStatusException.businessLogicError(CAN_NOT_CHANGE_COMPANY_TASK_IN_CONTRACT.with(pv("taskId", taskId)));
    }

    public static LocalizedResponseStatusException canNotChangeCompanyAssignmentForTaskInProgress(Integer taskId) {
        return LocalizedResponseStatusException.businessLogicError(CAN_NOT_CHANGE_COMPANY_TASK_IN_PROGRESS.with(pv("taskId", taskId)));
    }

    public static LocalizedResponseStatusException canNotUpdateTaskInState(Integer taskId, TaskStatus taskStatus) {
        return LocalizedResponseStatusException.businessLogicError(CAN_NOT_UPDATE_TASK_WITH_STATUS.with(
                pv("taskId", taskId),
               pv("taskStatus", taskStatus)
        ));
    }
}
