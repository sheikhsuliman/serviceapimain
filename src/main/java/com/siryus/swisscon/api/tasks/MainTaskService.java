package com.siryus.swisscon.api.tasks;


import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.tasks.dto.AssignCompanyToTaskRequest;
import com.siryus.swisscon.api.tasks.dto.CreateContractualTaskRequest;
import com.siryus.swisscon.api.tasks.dto.CreateDirectorialTaskRequest;
import com.siryus.swisscon.api.tasks.dto.CreateSubTaskRequest;
import com.siryus.swisscon.api.tasks.dto.ListTaskIdsRequest;
import com.siryus.swisscon.api.tasks.dto.ListTasksRequest;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import com.siryus.swisscon.api.tasks.dto.SubTaskDTO;
import com.siryus.swisscon.api.tasks.dto.UpdateDirectorialTaskRequest;
import com.siryus.swisscon.api.tasks.entity.MainTaskEntity;
import com.siryus.swisscon.api.util.validator.Reference;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface MainTaskService {

    /**
     * List all MainTask(s) associated with location and all its descendants
     *
     * @param locationId
     * @return
     */
    List<MainTaskDTO> listMainTasksForLocation(Integer locationId);


    /**
     * List all MainTask Id(s) and SubTask Id(s) associated with location and all its descendants
     *
     * @param locationId
     * @param request
     * @return
     */
    Map<Integer, List<Integer>> listMainAndSubTaskIdsForLocation(Integer locationId, ListTaskIdsRequest request);

    /**
     * Create Contractual Task (and associated Specification)
     *
     * @param request
     * @return
     */
    MainTaskDTO createContractualTask(@Valid CreateContractualTaskRequest request);

    /**
     * Create Directorial Task (and associated Specification)
     *
     * @param request
     * @return
     */
    MainTaskDTO createDirectorialTask(@Valid CreateDirectorialTaskRequest request);

    /**
     * Set/Clear template flag for task with given id
     *
     * @param taskId
     * @param templateOnOff
     * @return
     */
    MainTaskDTO setTemplateFlag( @NotNull @Reference(ReferenceType.MAIN_TASK) Integer taskId, boolean templateOnOff);

    /**
     * Returns list of tasks from the project with 'template' == true
     * @param projectId
     * @return
     */
    List<MainTaskDTO> listTemplatesInProject(@NotNull @Reference(ReferenceType.PROJECT) Integer projectId);

    /**
     * Update Directorial Task
     *
     * @param request
     * @return
     */
    MainTaskDTO updateDirectorialTask(@NotNull @Reference(ReferenceType.MAIN_TASK) Integer taskId, @Valid UpdateDirectorialTaskRequest request);

    /**
     * Assign company and (optionally) team to exisitng task with given id
     *
     * @param taskId
     * @param request
     * @return
     */
    MainTaskDTO assignCompanyToTask(
            @Reference(ReferenceType.MAIN_TASK) Integer taskId,
            @Valid AssignCompanyToTaskRequest request
    );

    /**
     * Un Assign company from task
     *
     * @param taskId
     * @return
     */
    MainTaskDTO unAssignCompanyFromTask(@Reference(ReferenceType.MAIN_TASK) Integer taskId);

    /**
     * Create Sub Task
     *
     * @param request
     * @return
     */
    SubTaskDTO createSubTask(@Valid CreateSubTaskRequest request);

    /**
     * List All Sub Tasks for given Main Task
     *
     * @param mainTaskId
     * @return
     */
    List<SubTaskDTO> listSubTasks(Integer mainTaskId);

    /**
     * List All Sub Tasks Ids for given Main Task
     *
     * @param mainTaskId
     * @return
     */
    List<Integer> listSubTaskIds(Integer mainTaskId);

    /**
     * List All Sub Tasks Ids which are part of the main task id list
     *
     * @param mainTaskIds
     * @return
     */
    Map<Integer, List<Integer>> listSubTaskIdsInMainTaskIds(List<Integer> mainTaskIds);

    /**
     * List All Sub Tasks Ids which are part of the main task id list and match the filters of the request
     *
     * @param mainTaskIds
     * @param request
     * @return
     */
    Map<Integer, List<Integer>> listSubTaskIdsInMainTaskIds(List<Integer> mainTaskIds,@Valid ListTaskIdsRequest request);

    /**
     * Changes the due date on a main task
     *
     * @param taskId
     * @param dueDate
     */
    MainTaskDTO updateDueDate(Integer taskId, LocalDateTime dueDate);

    /**
     * Get task with given ID.
     * @param taskId
     * @return
     */
    MainTaskDTO getMainTask(Integer taskId);

    List<MainTaskDTO> getMainTasksById(List<Integer> taskId);

    /**
     *
     * @param subTaskId
     * @return
     */
    SubTaskDTO getSubTask(Integer subTaskId);

    MainTaskDTO toMainTaskDTO(MainTaskEntity tasks);

        /**
         *
         * @param taskId
         */
    void deleteTask(Integer taskId);

    /**
     *
     * @param subTaskId
     */
    void deleteSubTask(Integer subTaskId);

    /**
     * @param projectID
     * @param request
     * @return
     */
    List<MainTaskDTO> listMainTasksForProject(Integer projectID, @Valid ListTasksRequest request);

    Map<Integer, List<Integer>> listMainAndSubTaskIdsForProject(Integer projectID, @Valid ListTaskIdsRequest request);

    List<MainTaskDTO> listMainTasksForProjectAndCompanyOrUnassigned(Integer projectId, Integer companyId);
}
