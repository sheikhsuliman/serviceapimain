package com.siryus.swisscon.api.tasks;

import com.siryus.swisscon.api.tasks.dto.EditTaskChecklistItemRequest;
import com.siryus.swisscon.api.tasks.dto.IdResponse;
import com.siryus.swisscon.api.tasks.dto.TaskChecklistItem;
import com.siryus.swisscon.api.tasks.dto.AddTaskChecklistItemRequest;

import java.util.List;

public interface TaskChecklistService {

    /**
     * Return all checklist items associated with default sub-task of main task with given id
     *
     * @param mainTaskId
     * @return
     */
    List<TaskChecklistItem>  getMainTaskChecklistItems(Integer mainTaskId);

    /**
     * Return all checklist items associated with sub-task with given id
     *
     * @param subTaskId
     * @return
     */
    List<TaskChecklistItem>  getSubTaskChecklistItems(Integer subTaskId);

    IdResponse addMainTaskChecklistItem(Integer mainTaskId, AddTaskChecklistItemRequest request);

    IdResponse addSubTaskChecklistItem(Integer subTaskId, AddTaskChecklistItemRequest request);

    void editTaskChecklistItem(Integer taskChecklistItemId, EditTaskChecklistItemRequest request);

    void onTaskChecklistItem(Integer taskChecklistItemId);

    void offTaskChecklistItem(Integer taskChecklistItemId);

    void deleteTaskChecklistItem(Integer taskChecklistItemId);
}
