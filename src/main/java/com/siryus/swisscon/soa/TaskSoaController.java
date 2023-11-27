package com.siryus.swisscon.soa;

import com.siryus.swisscon.api.tasks.MainTaskService;
import com.siryus.swisscon.api.tasks.TaskCommentService;
import com.siryus.swisscon.api.tasks.dto.CommentDTO;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import com.siryus.swisscon.api.tasks.dto.SubTaskDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/soa/")
public class TaskSoaController {

    private final MainTaskService mainTaskService;
    private final TaskCommentService taskCommentService;

    @Autowired
    public TaskSoaController(MainTaskService mainTaskService, TaskCommentService taskCommentService) {
        this.mainTaskService = mainTaskService;
        this.taskCommentService = taskCommentService;
    }

    @GetMapping(path="task/main-task/{mainTaskId}", produces="application/json")
    @ApiOperation(value = "Retrieve main task details", notes = "This is only for non-sensitive information")
    public MainTaskDTO getMainTask(@PathVariable Integer mainTaskId) {
        return mainTaskService.getMainTask(mainTaskId);
    }

    @GetMapping(path="task/sub-task/{subTaskId}", produces="application/json")
    @ApiOperation(value = "Retrieve sub task details", notes = "This is only for non-sensitive information")
    public SubTaskDTO getSubTask(@PathVariable Integer subTaskId) {
        return mainTaskService.getSubTask(subTaskId);
    }

    @GetMapping(path="task/comment/{commentId}", produces="application/json")
    @ApiOperation(value = "Retrieve comment of a task", notes = "This is only for non-sensitive information")
    public CommentDTO getTaskComment(@PathVariable Integer commentId) {
        return taskCommentService.getComment(commentId);
    }

}
