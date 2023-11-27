package com.siryus.swisscon.api.tasks;

import com.google.common.collect.ImmutableSet;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventType;

import java.util.Set;

public class TaskConstants {
    static final Set<String> REJECT_TASK_SET = ImmutableSet.of(WorkLogEventType.REJECT_TASK.name());
    
    static final String TASK_COUNTER = "TASK-COUNTER";    
}
