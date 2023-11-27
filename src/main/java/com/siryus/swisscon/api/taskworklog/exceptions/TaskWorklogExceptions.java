package com.siryus.swisscon.api.taskworklog.exceptions;

import com.siryus.swisscon.api.exceptions.LocalizedReason;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;
import com.siryus.swisscon.api.tasks.dto.TaskStatus;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventType;

import static com.siryus.swisscon.api.exceptions.LocalizedReason.ParameterValue.pv;

public class TaskWorklogExceptions {
    private static int e(int n) {
        return LocalizedResponseStatusException.TASK_WORK_LOG_ERROR_CODE + n;
    }

    private static final LocalizedReason CAN_NOT_SAVE_EVENT_IN_STATUS = LocalizedReason.like(e(1), "Can not {{eventType}}  for Task with {{taskStatus}} status");
    private static final LocalizedReason WORKER_IS_IDLE = LocalizedReason.like(e(2), "Worker {{workerId}} is idle");
    private static final LocalizedReason WORKER_IS_BUSY = LocalizedReason.like(e(3), "Worker {{workerId}} is busy");
    private static final LocalizedReason WORKER_HAS_NO_WORK_LOGS = LocalizedReason.like(e(4), "Worker {{workerId}} has no work-logs");
    private static final LocalizedReason INCONSISTENT_WORK_LOG_DATA = LocalizedReason.like(e(5), "Inconsistent work-log data : {{message}}");

    private static final LocalizedReason EVENT_NOT_FOUND = LocalizedReason.like(e(6), "Can not find event with id {{eventId}}");
    private static final LocalizedReason EVENT_IS_NOT_START = LocalizedReason.like(e(7), "Event with id {{eventId}} is not START event");
    private static final LocalizedReason EVENT_DOES_NOT_BELONG_TO_SUB_TASK = LocalizedReason.like(e(8), "Event with id {{eventId}} does not belong to sub-task {{subTaskId}}");
    private static final LocalizedReason CAN_NOT_LOG_MAIN_TASK_EVENT_FOR_SUB_TASK = LocalizedReason.like(e(9), "Can not log main task event for sub-task");
    private static final LocalizedReason CAN_NOT_LOG_SUB_TASK_EVENT_FOR_MAIN_TASK = LocalizedReason.like(e(10), "Can not log sbu-task event for main task");
    private static final LocalizedReason EITHER_MAIN_TASK_ID_OR_SUB_TASK_ID_MUST_BE_PRESENT = LocalizedReason.like(e(11), "Either main task id or sub-task id must be present");
    private static final LocalizedReason ATTACHMENT_NOT_VALID = LocalizedReason.like(e(12), "Attachment with id {{attachmentId}} is not valid");
    private static final LocalizedReason TASK_ID_IS_MISSING = LocalizedReason.like(e(13), "Task Id is null or 0");
    private static final LocalizedReason UNEXPECTED_EVENT_TYPE  = LocalizedReason.like(e(14), "Unexpected Event Type");
    private static final LocalizedReason EVENT_DOES_NOT_SUPPORT_ATTACHMENTS  = LocalizedReason.like(e(15), "Event {{eventName}} does not support attachments");


    public static LocalizedResponseStatusException canNotSaveEventInStatus(WorkLogEventType eventType, TaskStatus currentStatus) {
        return LocalizedResponseStatusException.businessLogicError(CAN_NOT_SAVE_EVENT_IN_STATUS.with(pv("eventType", eventType), pv("taskStatus", currentStatus)));
    }

    public static LocalizedResponseStatusException workerIsIdle(Integer workerId) {
        return LocalizedResponseStatusException.businessLogicError(WORKER_IS_IDLE.with(pv("workerId", workerId)));
    }
    public static LocalizedResponseStatusException workerIsBusy(Integer workerId) {
        return LocalizedResponseStatusException.businessLogicError(WORKER_IS_BUSY.with(pv("workerId", workerId)));
    }

    public static LocalizedResponseStatusException workerHasNoWorklogs(Integer workerId) {
        return LocalizedResponseStatusException.notFound(WORKER_HAS_NO_WORK_LOGS.with(pv("workerId", workerId)));
    }

    public static LocalizedResponseStatusException inconsistentWorkLogData( String message) {
        return LocalizedResponseStatusException.internalError(INCONSISTENT_WORK_LOG_DATA.with(pv("message", message)));
    }

    public static LocalizedResponseStatusException eventNotFound( Integer eventId) {
        return LocalizedResponseStatusException.notFound(EVENT_NOT_FOUND.with(pv("eventId", eventId)));
    }

    public static LocalizedResponseStatusException eventIsNotStartEvent( Integer eventId) {
        return LocalizedResponseStatusException.internalError(EVENT_IS_NOT_START.with(pv("eventId", eventId)));
    }

    public static LocalizedResponseStatusException eventDoesNotBelongToSubTask( Integer eventId, Integer subTaskId) {
        return LocalizedResponseStatusException.internalError(EVENT_DOES_NOT_BELONG_TO_SUB_TASK.with(pv("eventId", eventId), pv("subTaskId", subTaskId)));
    }

    public static LocalizedResponseStatusException canNotLogMainTaskEventForSubTask() {
        return LocalizedResponseStatusException.internalError(CAN_NOT_LOG_MAIN_TASK_EVENT_FOR_SUB_TASK.with());
    }

    public static LocalizedResponseStatusException canNotLogSubTaskEventForMainTask() {
        return LocalizedResponseStatusException.internalError(CAN_NOT_LOG_SUB_TASK_EVENT_FOR_MAIN_TASK.with());
    }

    public static LocalizedResponseStatusException eitherMainTaskIdOrSubTaskIdMustBePresent() {
        return LocalizedResponseStatusException.badRequest(EITHER_MAIN_TASK_ID_OR_SUB_TASK_ID_MUST_BE_PRESENT.with());
    }

    public static LocalizedResponseStatusException attachmentIsNotValid(Integer attachmentId) {
        return LocalizedResponseStatusException.badRequest(ATTACHMENT_NOT_VALID.with(pv("attachmentId", attachmentId)));
    }

    public static LocalizedResponseStatusException taskIdIsMissing() {
        return LocalizedResponseStatusException.badRequest(TASK_ID_IS_MISSING.with());
    }
    public static LocalizedResponseStatusException unexpectedEventType() {
        return LocalizedResponseStatusException.internalError(UNEXPECTED_EVENT_TYPE.with());
    }

    public static LocalizedResponseStatusException eventDoesNotSupportAttachments(WorkLogEventType event) {
        return LocalizedResponseStatusException.badRequest(EVENT_DOES_NOT_SUPPORT_ATTACHMENTS.with(pv("eventName", event.name())));
    }

}
