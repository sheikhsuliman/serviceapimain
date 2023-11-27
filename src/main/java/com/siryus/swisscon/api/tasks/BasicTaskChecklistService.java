package com.siryus.swisscon.api.tasks;

import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.siryus.swisscon.api.tasks.dto.AddTaskChecklistItemRequest;
import com.siryus.swisscon.api.tasks.dto.EditTaskChecklistItemRequest;
import com.siryus.swisscon.api.tasks.dto.IdResponse;
import com.siryus.swisscon.api.tasks.dto.TaskChecklistItem;
import com.siryus.swisscon.api.tasks.entity.SubTaskCheckListEntity;
import com.siryus.swisscon.api.tasks.exceptions.TaskExceptions;
import com.siryus.swisscon.api.tasks.repos.SubTaskCheckListRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskRepository;
import com.siryus.swisscon.api.util.validator.DTOValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BasicTaskChecklistService implements TaskChecklistService {
    private SubTaskRepository subTaskRepository;
    private SubTaskCheckListRepository subTaskCheckListRepository;

    @Autowired
    public void setSubTaskRepository(SubTaskRepository subTaskRepository) {
        this.subTaskRepository = subTaskRepository;
    }

    @Autowired
    public void setSubTaskCheckListRepository(SubTaskCheckListRepository subTaskCheckListRepository) {
        this.subTaskCheckListRepository = subTaskCheckListRepository;
    }

    @Override
    public List<TaskChecklistItem> getMainTaskChecklistItems(Integer mainTaskId) {
        return getSubTaskChecklistItems(
                subTaskRepository.getDefaultSubTask(mainTaskId)
                        .orElseThrow( () -> TaskExceptions.defaultSubTaskNotFound(mainTaskId))
                        .getId()
        );
    }

    @Override
    public List<TaskChecklistItem> getSubTaskChecklistItems(Integer subTaskId) {
        if(!subTaskRepository.existsById(subTaskId)) {
            throw TaskExceptions.subTaskNotFound(subTaskId);
        }

        return subTaskCheckListRepository.findAllBySubTaskId(subTaskId)
                .stream()
                .map(TaskChecklistItem::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public IdResponse addMainTaskChecklistItem(Integer mainTaskId, AddTaskChecklistItemRequest request) {
        return addSubTaskChecklistItem(
                subTaskRepository.getDefaultSubTask(mainTaskId)
                        .orElseThrow( () -> TaskExceptions.defaultSubTaskNotFound(mainTaskId))
                        .getId(),
                request
        );
    }

    @Override
    @Transactional
    public IdResponse addSubTaskChecklistItem(Integer subTaskId, AddTaskChecklistItemRequest request) {
        DTOValidator.validateAndThrow(request);

        SubTaskCheckListEntity savedEntity = subTaskCheckListRepository.save(
                new SubTaskCheckListEntity(subTaskId, request.getTitle())
        );

        return new IdResponse(savedEntity.getId());
    }

    @Override
    @Transactional
    public void editTaskChecklistItem(Integer taskChecklistItemId, EditTaskChecklistItemRequest request) {
        DTOValidator.validateAndThrow(request);

        SubTaskCheckListEntity entity = subTaskCheckListRepository.findById(taskChecklistItemId)
                .orElseThrow( () -> TaskExceptions.subTaskCheckListNotFound(taskChecklistItemId));

        entity.setTitle( request.getTitle());

        subTaskCheckListRepository.save(entity);
    }

    @Override
    @Transactional
    public void onTaskChecklistItem(Integer taskChecklistItemId) {
        SubTaskCheckListEntity entity = subTaskCheckListRepository.findById(taskChecklistItemId)
                .orElseThrow( () -> TaskExceptions.subTaskCheckListNotFound(taskChecklistItemId));

        if (entity.getCheckedDate() == null) {
            entity.setCheckedDate(LocalDateTime.now());
            entity.setCheckedBy(Integer.parseInt(LecwUtils.currentUser().getId()));
        }

        subTaskCheckListRepository.save(entity);
    }

    @Override
    @Transactional
    public void offTaskChecklistItem(Integer taskChecklistItemId) {
        SubTaskCheckListEntity entity = subTaskCheckListRepository.findById(taskChecklistItemId)
                .orElseThrow( () -> TaskExceptions.subTaskCheckListNotFound(taskChecklistItemId));

        if (entity.getCheckedDate() != null) {
            entity.setCheckedDate(null);
            entity.setCheckedBy(null);
        }

        subTaskCheckListRepository.save(entity);
    }

    @Override
    @Transactional
    public void deleteTaskChecklistItem(Integer taskChecklistItemId) {
        SubTaskCheckListEntity entity = subTaskCheckListRepository.findById(taskChecklistItemId)
                .orElseThrow( () -> TaskExceptions.subTaskCheckListNotFound(taskChecklistItemId));

        subTaskCheckListRepository.delete(entity);
    }
}
