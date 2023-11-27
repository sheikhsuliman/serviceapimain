package com.siryus.swisscon.api.taskworklog;

import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.siryus.swisscon.api.exceptions.NotFoundException;
import com.siryus.swisscon.api.tasks.MainTaskService;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import com.siryus.swisscon.api.taskworklog.dto.CreateTimerWorkLogRequest;
import com.siryus.swisscon.api.taskworklog.dto.EventHistoryDTO;
import com.siryus.swisscon.api.taskworklog.dto.MainTaskDurationDTO;
import com.siryus.swisscon.api.taskworklog.dto.ModifyTimerWorkLogRequest;
import com.siryus.swisscon.api.taskworklog.dto.RejectTaskRequest;
import com.siryus.swisscon.api.taskworklog.dto.StartStopWorklogDTO;
import com.siryus.swisscon.api.taskworklog.dto.SubTaskDurationDTO;
import com.siryus.swisscon.api.taskworklog.dto.TaskStatusDTO;
import com.siryus.swisscon.api.taskworklog.dto.TaskWorklogDTO;
import com.siryus.swisscon.api.taskworklog.dto.TaskWorklogRequest;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventType;
import com.siryus.swisscon.api.taskworklog.dto.WorkerStatusDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZoneOffset;


@RestController("subTaskWorklogController")
@RequestMapping("/api/rest/work-log")
@Api(tags = {"Tasks:worklog"})
public class TaskWorklogController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskWorklogController.class);

    private final TaskWorkLogWriterService writerService;
    private final TaskWorkLogQueryService queryService;
    private final MainTaskService taskService;

    @Autowired
    public TaskWorklogController(TaskWorkLogWriterService writerService, TaskWorkLogQueryService queryService, MainTaskService taskService) {
        this.writerService = writerService;
        this.queryService = queryService;
        this.taskService = taskService;
    }

    @ApiOperation(
            value = "Start Timer on a task/sub-task",
            tags = "Tasks:worklog"
    )
    @PostMapping("start-timer")
    public TaskStatusDTO startTimer(@RequestBody TaskWorklogRequest request) {
        return writerService.createSubTaskWorkLog(
                WorkLogEventType.START_TIMER,
                Integer.valueOf(LecwUtils.currentUser().getId()),
                request
        );
    }

    @ApiOperation(
            value = "Start Timer on a task/sub-task without stopping any other timers",
            tags = "Tasks:worklog"
    )
    @PostMapping("start-multi-timer")
    public TaskStatusDTO startMultiTimer(@RequestBody TaskWorklogRequest request) {
        return writerService.createSubTaskWorkLog(
                WorkLogEventType.START_MULTI_TIMER,
                Integer.valueOf(LecwUtils.currentUser().getId()),
                request
        );
    }

    @ApiOperation(
            value = "Stop Timer on a task/sub-task",
            tags = "Tasks:worklog"
    )
    @PostMapping("stop-timer")
    public TaskStatusDTO stopTimer(@RequestBody TaskWorklogRequest request ) {
        return writerService.createSubTaskWorkLog(
                WorkLogEventType.STOP_TIMER,
                Integer.valueOf(LecwUtils.currentUser().getId()),
                request
        );
    }

    @ApiOperation(
            value = "Complete task",
            tags = "Tasks:worklog"
    )
    @PostMapping("complete-task")
    public TaskStatusDTO completeTask(@RequestBody  TaskWorklogRequest request) {
        final Integer userId = Integer.valueOf(LecwUtils.currentUser().getId());
        return writerService.completeTask(userId, request);
    }

    @ApiOperation(
            value = "Complete sub-task",
            tags = "Tasks:worklog"
    )
    @PostMapping("complete-sub-task")
    public TaskStatusDTO completeSubTask(@RequestBody  TaskWorklogRequest request) {
        final Integer userId = Integer.valueOf(LecwUtils.currentUser().getId());
        return writerService.createSubTaskWorkLog(
                WorkLogEventType.COMPLETE_SUB_TASK,
                userId,
                request
        );
    }

    @ApiOperation(
            value = "Approve task",
            tags = "Tasks:worklog"
    )
    @PostMapping("approve-task")
    public TaskStatusDTO approveTask(@RequestBody  TaskWorklogRequest request) {
        return writerService.createMainTaskWorkLog(
                WorkLogEventType.APPROVE_TASK,
                Integer.valueOf(LecwUtils.currentUser().getId()),
                request
        );
    }

    @ApiOperation(
            value = "Reject task",
            tags = "Tasks:worklog"
    )
    @PostMapping("reject-task")
    public TaskStatusDTO rejectTask(@RequestBody RejectTaskRequest request) {
        MainTaskDTO task = this.taskService.updateDueDate(
            request.getWorklogRequest().getMainTaskId(),
            request.getDueDate().withZoneSameInstant(ZoneId.of(ZoneOffset.UTC.getId())).toLocalDateTime()
        );
        if (null == task) {
            throw new NotFoundException();
        }

        return this.rejectTaskContractor(request.getWorklogRequest());
    }

    @ApiOperation(
            value = "Reject task (subcontractor)",
            tags = "Tasks:worklog"
    )
    @PostMapping("reject-task-contractor")
    public TaskStatusDTO rejectTaskContractor(@RequestBody  TaskWorklogRequest request) {
        return writerService.createMainTaskWorkLog(
                WorkLogEventType.REJECT_TASK,
                Integer.valueOf(LecwUtils.currentUser().getId()),
                request
        );
    }

    @ApiOperation(
            value = "Get worker status",
            tags = "Tasks:worklog"
    )    @GetMapping("worker-status")
    public WorkerStatusDTO getWorkerStatus() {
        return queryService.getWorkerStatus(Integer.valueOf(LecwUtils.currentUser().getId()));
    }

    @ApiOperation(
            value = "Calculate main task duration",
            tags = "Tasks:worklog"
    )
    @GetMapping("task/{mainTaskId}")
    public MainTaskDurationDTO getMainTaskDurations(@PathVariable Integer mainTaskId) {
        return queryService.getMainTaskDurations(mainTaskId);
    }

    @ApiOperation(
            value = "Get main task, default sub-task duration",
            tags = "Tasks:worklog"
    )
    @GetMapping("default-sub-task/{mainTaskId}")
    public SubTaskDurationDTO getMainTaskDefaultSubTaskDurations(@PathVariable Integer mainTaskId) {
        return queryService.getMainTaskDefaultSubTaskDurations(mainTaskId);
    }

    @ApiOperation(
            value = "Get sub-task duration",
            tags = "Tasks:worklog"
    )
    @GetMapping("sub-task/{subTaskId}")
    public SubTaskDurationDTO getSubTaskDurations(@PathVariable Integer subTaskId) {
        return queryService.getSubTaskDurations(subTaskId);
    }

    @ApiOperation(
            value = "Return latest work log event for given worker",
            tags = "Tasks:worklog"
    )
    @GetMapping("worker/{workerId}/last-timer-worklog")
    public TaskWorklogDTO workerLastTimerWorklog(@PathVariable Integer workerId) {
        return queryService.getWorkerLastTimerWorklog(workerId);
    }

    @ApiOperation(
            value = "Return time spent by given worker on given sub-task",
            tags = "Tasks:worklog"
    )
    @GetMapping("sub-task/{subTaskId}/worker/{workerId}")
    public SubTaskDurationDTO getWorkerSubTaskDurations(@PathVariable Integer subTaskId, @PathVariable Integer workerId) {
        return queryService.getWorkerSubTaskDurations(workerId, subTaskId);
    }

    @ApiOperation(
            value = "Cancel time work log",
            tags = "Tasks:worklog"
    )
    @PostMapping("sub-task/{subTaskId}/cancel-timer/{startTimerEventId}")
    @PreAuthorize("hasPermission(#subTaskId, 'SUB_TASK', 'WORKLOG_UPDATE')")
    public TaskWorklogDTO cancelTimer(@PathVariable Integer subTaskId, @PathVariable Integer startTimerEventId) {
        return writerService.cancelSubTaskTimerWorkLog(subTaskId, startTimerEventId);
    }

    @ApiOperation(
            value = "Create new time work log",
            tags = "Tasks:worklog"
    )
    @PostMapping("sub-task/{subTaskId}/create-timer")
    @PreAuthorize("hasPermission(#subTaskId, 'SUB_TASK', 'WORKLOG_UPDATE')")
    public StartStopWorklogDTO createTimeStamp(@PathVariable Integer subTaskId, @RequestBody CreateTimerWorkLogRequest request) {
        return writerService.createSubTaskTimerWorkLog(subTaskId, request);
    }

    @ApiOperation(
            value = "Modify existing time work log",
            tags = "Tasks:worklog"
    )
    @PostMapping("sub-task/{subTaskId}/modify-timer/{startTimerEventId}")
    @PreAuthorize("hasPermission(#subTaskId, 'SUB_TASK', 'WORKLOG_UPDATE')")
    public StartStopWorklogDTO modifyTimeStamp(
            @PathVariable Integer subTaskId,
            @PathVariable Integer startTimerEventId,
            @RequestBody ModifyTimerWorkLogRequest request
    ) {
        return writerService.modifySubTaskTimerWorkLog(subTaskId, startTimerEventId, request);
    }

    @ApiOperation(
            value = "Return main task work log events history",
            tags = "Tasks:worklog"
    )
    @GetMapping(value="/task/{mainTaskId}/history" )
    public Page<EventHistoryDTO> getMainTaskHistory(@PathVariable Integer mainTaskId,
                                                    @RequestParam(value="_pn", required=false, defaultValue = "0") Integer page,
                                                    @RequestParam(value="_ps", required=false, defaultValue = "10") Integer size) {
        Pageable pageable= PageRequest.of(page==null?0:page,size==null?10:size);
        return queryService.getWorklogHistoryForMainTaskPaged(mainTaskId,pageable);
    }


}

