package com.siryus.swisscon.api.taskworklog;

import com.siryus.swisscon.api.taskworklog.dto.CreateTimerWorkLogRequest;
import com.siryus.swisscon.api.taskworklog.dto.ModifyTimerWorkLogRequest;
import com.siryus.swisscon.api.taskworklog.dto.StartStopWorklogDTO;
import com.siryus.swisscon.api.taskworklog.dto.TaskStatusDTO;
import com.siryus.swisscon.api.taskworklog.dto.TaskWorklogDTO;
import com.siryus.swisscon.api.taskworklog.dto.TaskWorklogRequest;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventType;


public interface TaskWorkLogWriterService {

    TaskStatusDTO createMainTaskWorkLog(WorkLogEventType event, Integer workerId, TaskWorklogRequest request);

    TaskStatusDTO completeTask(Integer workerId, TaskWorklogRequest request);

    TaskStatusDTO createSubTaskWorkLog(WorkLogEventType event, Integer workerId, TaskWorklogRequest request);

    TaskWorklogDTO cancelSubTaskTimerWorkLog(Integer subTaskId, Integer startTimerEventId );

    StartStopWorklogDTO createSubTaskTimerWorkLog(Integer subTaskId, CreateTimerWorkLogRequest request);

    StartStopWorklogDTO  modifySubTaskTimerWorkLog(Integer subTaskId, Integer startTimerEventId, ModifyTimerWorkLogRequest request);
}
