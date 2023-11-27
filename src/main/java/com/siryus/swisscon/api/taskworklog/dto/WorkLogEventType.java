package com.siryus.swisscon.api.taskworklog.dto;

import com.siryus.swisscon.api.auth.permission.PermissionName;
import com.siryus.swisscon.api.mediawidget.MediaConstants;
import com.siryus.swisscon.api.tasks.dto.TaskStatus;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.siryus.swisscon.api.taskworklog.dto.TransitionRule.rule;

public enum  WorkLogEventType {
    // Contract Events
    ADDED_TO_CONTRACT(true, null,
            rule(TaskStatus.ASSIGNED, TaskStatus.DRAFT)
                    .requirePermission(PermissionName.TASK_CREATE_UPDATE_MAIN),
            rule(TaskStatus.OPEN, TaskStatus.DRAFT)
                    .requirePermission(PermissionName.TASK_CREATE_UPDATE_MAIN),
            rule(TaskStatus.DRAFT, TaskStatus.DRAFT)
                    .requirePermission(PermissionName.TASK_CREATE_UPDATE_MAIN)
    ),

    REMOVED_FROM_CONTRACT(true, null,
            rule(TaskStatus.DRAFT, TaskStatus.DRAFT)
                    .requirePermission(PermissionName.TASK_CREATE_UPDATE_MAIN)
    ),

    CONTRACT_ACCEPTED(true, null,
            rule(TaskStatus.DRAFT, TaskStatus.ACCEPTED)
                    .requirePermission(PermissionName.CONTRACT_ACCEPT_DECLINE_OFFER)
    ),

    CONTRACT_SELF_ACCEPTED(true, null,
                      rule(TaskStatus.DRAFT, TaskStatus.ACCEPTED)
                              .requirePermission(PermissionName.CONTRACT_SELF_ACCEPT_OFFER)
    ),

    CONTRACT_TASK_NEGATED(true, null,
                      rule(TaskStatus.ACCEPTED, TaskStatus.DRAFT)
    ),

    // Main Task Events
    START_TASK(true, null,
            rule(TaskStatus.ASSIGNED, TaskStatus.PAUSED),
            rule(TaskStatus.ACCEPTED, TaskStatus.PAUSED)
    ),

    // Sub Task Events
    START_TIMER(false, MediaConstants.START_TASK_FOLDER,
            rule(TaskStatus.ASSIGNED, TaskStatus.IN_PROGRESS, WorkLogEventType.START_TASK),
            rule(TaskStatus.ACCEPTED, TaskStatus.IN_PROGRESS, WorkLogEventType.START_TASK),
            rule(TaskStatus.REJECTED, TaskStatus.IN_PROGRESS),
            rule(TaskStatus.IN_PROGRESS, TaskStatus.IN_PROGRESS),
            rule(TaskStatus.PAUSED, TaskStatus.IN_PROGRESS) 
    ),
    START_MULTI_TIMER(START_TIMER),
    STOP_TIMER( false, null,
         rule(TaskStatus.IN_PROGRESS, TaskStatus.PAUSED)
    ),
    STOP_ALL_TIMERS( false, null,
            rule(TaskStatus.COMPLETED, TaskStatus.COMPLETED),
            rule(TaskStatus.ASSIGNED, TaskStatus.PAUSED),
            rule(TaskStatus.ACCEPTED, TaskStatus.PAUSED),
            rule(TaskStatus.PAUSED, TaskStatus.PAUSED),
            rule(TaskStatus.IN_PROGRESS, TaskStatus.PAUSED)
    ),
    COMPLETE_SUB_TASK(
            false, MediaConstants.COMPLETE_TASK_FOLDER,
            rule(TaskStatus.PAUSED, TaskStatus.COMPLETED),
            rule(TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED, WorkLogEventType.STOP_ALL_TIMERS)
    ),
    CANCEL_TIMER( false, null ),

    COMPLETE_CONTRACTOR_TASK(
            true, MediaConstants.COMPLETE_TASK_FOLDER,
            rule(TaskStatus.PAUSED, TaskStatus.IN_CONTRACTOR_REVIEW),
            rule(TaskStatus.IN_PROGRESS, TaskStatus.IN_CONTRACTOR_REVIEW, WorkLogEventType.STOP_ALL_TIMERS)
    ),
    REJECT_CONTRACTOR_TASK(
            true, null,
            rule(TaskStatus.IN_CONTRACTOR_REVIEW, TaskStatus.REJECTED)
    ),
    COMPLETE_TASK(
            true, MediaConstants.COMMENTS_FOLDER,
            rule(TaskStatus.PAUSED, TaskStatus.IN_REVIEW),
            rule(TaskStatus.IN_PROGRESS, TaskStatus.IN_REVIEW, WorkLogEventType.STOP_ALL_TIMERS)
    ),
    REJECT_TASK(
            true, MediaConstants.REJECT_TASK_FOLDER,
            rule(TaskStatus.IN_CONTRACTOR_REVIEW, TaskStatus.REJECTED),
            rule(TaskStatus.IN_REVIEW, TaskStatus.REJECTED)
                    .requirePermission(PermissionName.TASK_REVIEW)
    ),
    APPROVE_TASK(
            true, null,
            rule(TaskStatus.IN_CONTRACTOR_REVIEW, TaskStatus.IN_REVIEW),
            rule(TaskStatus.IN_REVIEW, TaskStatus.COMPLETED)
                    .requirePermission(PermissionName.TASK_REVIEW)
    );


    public boolean isMainTaskEvent() {
        return mainTaskEvent;
    }

    public String attachmentSystemFolder() {
        return attachmentSystemFolder;
    }
    
    public boolean supportsAttachments() {
        return StringUtils.isNotBlank(attachmentSystemFolder);
    }

    public boolean validCurrentStatus(TaskStatus currentStatus) {
        return mainTaskStateTransitionMap.containsKey(currentStatus);
    }

    public List<BoundEventType> impliedEvents(TaskStatus currentStatus) {
        return mainTaskStateTransitionMap.get(currentStatus).impliedEvents.stream()
                .map(BoundEventType::bound)
                .collect(Collectors.toList());
    }

    public TaskStatus nextStatus(TaskStatus currentStatus) {
        return mainTaskStateTransitionMap.get(currentStatus).nextStatus;
    }

    public PermissionName requirePermission(TaskStatus currentStatus) {
        return mainTaskStateTransitionMap.get(currentStatus).requiredPermission;
    }

    private final WorkLogEventType concreteEvent;
    private final boolean mainTaskEvent;
    private final String attachmentSystemFolder;
    private final Map<TaskStatus, TransitionRule> mainTaskStateTransitionMap;

    WorkLogEventType(WorkLogEventType concreteEvent) {
        this.concreteEvent = concreteEvent;
        this.mainTaskEvent = concreteEvent.mainTaskEvent;

        this.attachmentSystemFolder = concreteEvent.attachmentSystemFolder;

        this.mainTaskStateTransitionMap = concreteEvent.mainTaskStateTransitionMap;
    }

    WorkLogEventType(boolean mainTaskEvent, String attachmentSystemFolder, TransitionRule... rules) {
        this(mainTaskEvent, attachmentSystemFolder, Arrays.asList(rules));
    }

    WorkLogEventType(boolean mainTaskEvent, String attachmentSystemFolder, List<TransitionRule> rules) {
        this.concreteEvent = this;
        this.mainTaskEvent = mainTaskEvent;
        
        this.attachmentSystemFolder = attachmentSystemFolder;
        
        this.mainTaskStateTransitionMap = new HashMap<>();

        for(TransitionRule rule : rules) {
            this.mainTaskStateTransitionMap.put(rule.currentStatus, rule);
        }
    }

    public WorkLogEventType getConcreteEvent() {
        return concreteEvent;
    }

    public boolean sameAs(WorkLogEventType otherEvent) {
        return concreteEvent.equals(otherEvent);
    }

    public static class BoundEventType {
        private final Integer workerId;
        private final Integer subTaskId;
        private final WorkLogEventType eventType;

        BoundEventType(Integer workerId, Integer subTaskId, WorkLogEventType eventType) {
            this.workerId = workerId;
            this.subTaskId = subTaskId;
            this.eventType = eventType;
        }

        public Integer getWorkerId(Integer defaultId) {
            return workerId > 0 ? workerId : defaultId;
        }

        public Integer getSubTaskId(Integer defaultId) {
            return subTaskId > 0 ? subTaskId : defaultId;
        }

        public WorkLogEventType getEventType() {
            return eventType;
        }

        public static BoundEventType bound(WorkLogEventType eventType) {
            return bound(0, 0, eventType);
        }
        public static BoundEventType bound(Integer workerId, WorkLogEventType eventType) {
            return bound(workerId, 0, eventType);
        }
        public static BoundEventType bound(Integer workerId, Integer subTaskId, WorkLogEventType eventType) {
            return new BoundEventType(workerId, subTaskId, eventType);
        }
    }
}

class TransitionRule {
    final TaskStatus currentStatus;
    final TaskStatus nextStatus;
    final List<WorkLogEventType> impliedEvents;
    final PermissionName requiredPermission;

    TransitionRule(TaskStatus currentStatus, TaskStatus nextStatus, List<WorkLogEventType> impliedEvents, PermissionName requiredPermission) {
        this.currentStatus = currentStatus;
        this.nextStatus = nextStatus;
        this.impliedEvents = impliedEvents != null ? impliedEvents : Collections.emptyList();
        this.requiredPermission = requiredPermission;
    }

    TransitionRule requirePermission( PermissionName requiredPermission ) {
        return new TransitionRule(currentStatus, nextStatus, impliedEvents, requiredPermission);
    }

    static TransitionRule rule(TaskStatus currentStatus, TaskStatus nextStatus, WorkLogEventType... impliedEvents) {
        return new TransitionRule(currentStatus, nextStatus, Arrays.asList(impliedEvents), null);
    }
}
