package com.siryus.swisscon.api.tasks;

import com.siryus.swisscon.api.auth.user.TeamUserDTO;

import java.util.List;

public interface TaskTeamService {
    /**
     * Return union of task teams for all main tasks associated with location
     *
     * @param locationId
     * @return
     */
    List<TeamUserDTO> getLocationTeam(Integer locationId);

    /**
     * Get task team with belongs to the main task with the given Id
     * @param taskId the task Id
     * @return List of DTOs which belongs to the Task
     */
    List<TeamUserDTO> getMainTaskTeam(Integer taskId);

    /**
     * Get sub task team with belongs to the main task with the given Id
     * @param subTaskId the sub task Id
     * @return List of DTOs which belongs to the Sub Task
     */
    List<TeamUserDTO> getSubTaskTeam(Integer subTaskId);

    /**
     * Get the available users which can be added to the task
     * @param taskId the task Id
     * @return List of User DTO's which can be added to the task
     */
    List<TeamUserDTO> getMainTaskAvailableUsers(Integer taskId);

    /**
     * Get the available users which can be added to a task on creation
     * @return List of User DTO's which can be added to the task on creation
     */
    List<TeamUserDTO> getMainTaskAvailableUsersOnCreation(Integer projectId, Integer companyId);

    /**
     * Get the available users which can be added to the sub task
     * @param subTaskId the sub task Id
     * @return List of User DTO's which can be added to the sub task
     */
    List<TeamUserDTO> getSubTaskAvailableUsers(Integer subTaskId);

    /**
     * Adds a user to the given Sub Task
     *
     * @param userId the id of the user which is to be added
     * @param subTaskId the id of the Sub Task
     *
     * @return
     */
    void addUserToSubTask(Integer userId, Integer subTaskId);

    /**
     * Adds a user to the default subtask of the given Main Task
     *
     * @param userId the id of the user which is to be added
     * @param taskId the id of the Main Task
     *
     * @return
     */
    void addUserToTask(Integer userId, Integer taskId);

    /**
     * Remove the user of the main task team
     *
     * @param taskId the main task id
     * @param userId the user id
     */
    void removeMainTaskUser(Integer taskId, Integer userId);

    /**
     * Remove the user of the sub task team
     *
     * @param subTaskId the sub task id
     * @param userId the user id
     */
    void removeSubTaskUser(Integer subTaskId, Integer userId);
}
