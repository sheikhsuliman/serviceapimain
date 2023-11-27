package com.siryus.swisscon.api.taskworklog;

import com.siryus.swisscon.api.taskworklog.dto.EventHistoryDTO;
import com.siryus.swisscon.api.taskworklog.dto.MainTaskDurationDTO;
import com.siryus.swisscon.api.taskworklog.dto.SubTaskDurationDTO;
import com.siryus.swisscon.api.taskworklog.dto.TaskWorklogDTO;
import com.siryus.swisscon.api.taskworklog.dto.WorkerStatusDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskWorkLogQueryService {
    TaskWorklogDTO getWorkLogById(Integer worklogId);

    SubTaskDurationDTO getWorkerSubTaskDurations(Integer workerId, Integer subTaskId);

    SubTaskDurationDTO getSubTaskDurations(Integer subTaskId);

    MainTaskDurationDTO getMainTaskDurations(Integer mainTaskId);

    Page<EventHistoryDTO> getWorklogHistoryForMainTaskPaged(Integer mainTaskId, Pageable pageable);

    TaskWorklogDTO getWorkerLastTimerWorklog(Integer workerId);

    SubTaskDurationDTO getMainTaskDefaultSubTaskDurations(Integer mainTaskId);

    WorkerStatusDTO getWorkerStatus(Integer workerId);
}
