package com.siryus.swisscon.api.tasks;

import com.siryus.swisscon.api.tasks.dto.TaskLinkDTO;
import com.siryus.swisscon.api.tasks.entity.TaskLinkType;

import java.util.List;

public interface TaskLinkService {
    List<TaskLinkDTO> listAllTaskLinksForProject(Integer projectId);
    List<TaskLinkDTO> listAllTasksLWithSourceTask(Integer taskId);
    List<TaskLinkDTO> listAllTasksLWithDestinationTask(Integer taskId);

    TaskLinkDTO linkTasks(TaskLinkType linkType, Integer sourceTaskId, Integer destinationTaskId);
    TaskLinkDTO archiveTaskLik(Integer taskLinkId);
}
