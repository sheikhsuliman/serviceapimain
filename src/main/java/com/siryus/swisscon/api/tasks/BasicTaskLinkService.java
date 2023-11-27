package com.siryus.swisscon.api.tasks;

import com.siryus.swisscon.api.project.project.Project;
import com.siryus.swisscon.api.project.project.ProjectRepository;
import com.siryus.swisscon.api.tasks.dto.TaskLinkDTO;
import com.siryus.swisscon.api.tasks.entity.MainTaskEntity;
import com.siryus.swisscon.api.tasks.entity.TaskLinkEntity;
import com.siryus.swisscon.api.tasks.entity.TaskLinkType;
import com.siryus.swisscon.api.tasks.exceptions.TaskExceptions;
import com.siryus.swisscon.api.tasks.repos.MainTaskRepository;
import com.siryus.swisscon.api.tasks.repos.TaskLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BasicTaskLinkService implements TaskLinkService {
    private final TaskLinkRepository repository;
    private final ProjectRepository projectRepository;
    private final MainTaskRepository mainTaskRepository;

    @Autowired
    public BasicTaskLinkService(TaskLinkRepository repository, ProjectRepository projectRepository, MainTaskRepository mainTaskRepository) {
        this.repository = repository;
        this.projectRepository = projectRepository;
        this.mainTaskRepository = mainTaskRepository;
    }

    @Override
    public List<TaskLinkDTO> listAllTaskLinksForProject(Integer projectId) {
        validateProjectId(projectId);

        return repository.findTaskLinksInProject(projectId).stream()
                    .map(TaskLinkDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskLinkDTO> listAllTasksLWithSourceTask(Integer taskId) {
        validateTaskId(taskId);

        return repository.findTaskLinksWithSourceTask(taskId).stream()
                .map(TaskLinkDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskLinkDTO> listAllTasksLWithDestinationTask(Integer taskId) {
        validateTaskId(taskId);

        return repository.findTaskLinksWithDestinationTask(taskId).stream()
                .map(TaskLinkDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public TaskLinkDTO linkTasks(TaskLinkType linkType, Integer sourceTaskId, Integer destinationTaskId) {
        ProjectAndTwoTasks valid = validateSrcAndDestinationTaskIds(sourceTaskId, destinationTaskId);

        return TaskLinkDTO.from(
                repository.save(
                    TaskLinkEntity.builder()
                            .project(Project.ref(valid.projectId))
                            .linkType(linkType)
                            .sourceTask(valid.sourceTask)
                            .destinationTask(valid.destinationTask)
                    .build()
                )
        );
    }

    @Transactional
    @Override
    public TaskLinkDTO archiveTaskLik(Integer taskLinkId) {
        TaskLinkEntity taskLink = validateTaskLinkId(taskLinkId);

        return TaskLinkDTO.from(
                taskLink.getDisabled() != null
                    ? taskLink
                    : repository.save(
                        taskLink.toBuilder().disabled(LocalDateTime.now()).build()
                    )
        );
    }

    private Project validateProjectId(Integer projectId) {
        return projectRepository.findById(projectId).orElseThrow(
                () -> TaskExceptions.projectNotFound(projectId)
        );
    }

    private MainTaskEntity validateTaskId(Integer taskId) {
        return mainTaskRepository.findById(taskId).orElseThrow(
                () -> TaskExceptions.mainTaskNotFound(taskId)
        );
    }

    private TaskLinkEntity validateTaskLinkId(Integer taskLinkId) {
        return repository.findById(taskLinkId).orElseThrow(
                () -> TaskExceptions.noSuchTaskLink(taskLinkId)
        );
    }

    private ProjectAndTwoTasks validateSrcAndDestinationTaskIds(Integer sourceTaskId, Integer destinationTaskId) {
        MainTaskEntity sourceTask = validateTaskId(sourceTaskId);

        MainTaskEntity destinationTask = validateTaskId(destinationTaskId);

        if(!sourceTask.getProjectId().equals(destinationTask.getProjectId())) {
            throw TaskExceptions.canNotLinkTasksFromDifferentProjects();
        }

        return ProjectAndTwoTasks.of(sourceTask.getProjectId(), sourceTask, destinationTask);
    }
}

class ProjectAndTwoTasks {
    final Integer projectId;
    final MainTaskEntity sourceTask;
    final MainTaskEntity destinationTask;

    ProjectAndTwoTasks(Integer projectId, MainTaskEntity sourceTask, MainTaskEntity destinationTask) {
        this.projectId = projectId;
        this.sourceTask = sourceTask;
        this.destinationTask = destinationTask;
    }

    static ProjectAndTwoTasks of(Integer projectId, MainTaskEntity sourceTask, MainTaskEntity destinationTask) {
        return new ProjectAndTwoTasks(projectId, sourceTask, destinationTask);
    }
}
