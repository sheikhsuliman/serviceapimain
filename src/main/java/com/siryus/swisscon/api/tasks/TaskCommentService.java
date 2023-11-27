package com.siryus.swisscon.api.tasks;

import com.siryus.swisscon.api.tasks.dto.CommentDTO;
import com.siryus.swisscon.api.tasks.dto.CreateCommentDTO;

import java.util.List;

public interface TaskCommentService {
    /**
     * Retrieves all comments associated to this task or an empty array if none are present
     *
     * @param mainTaskId the id of the task for which we want to retrieve the comments
     * @return CommentDTO
     */
    List<CommentDTO> getMainTaskComments(Integer mainTaskId);

    /**
     * Retrieves all comments associated to this sub-task or an empty array if none are present
     *
     * @param subTaskId the id of the sub-task for which we want to retrieve the comments
     * @return CommentDTO
     */
    List<CommentDTO> getSubTaskComments(Integer subTaskId);

    /**
     * Adds a comment to the main task
     *
     * @param  mainTaskId the id of the main task for which we want to retrieve the comments
     * @param comment information
     */
    CommentDTO addCommentToMainTask(Integer mainTaskId, CreateCommentDTO comment);

    /**
     * Adds a comment to the sub-task
     *
     * @param subTaskId the id of the sub-task for which we want to retrieve the comments
     * @param comment information
     */
    CommentDTO addCommentToSubTask(Integer subTaskId, CreateCommentDTO comment);

    /**
     * Retrieve comment with given id
     *
     * @param commentId
     * @return
     */
    CommentDTO getComment(Integer commentId);

    /**
     * Updates comment with given id
     *
     * @param commentId
     * @param comment
     * @return
     */
    CommentDTO updateComment(Integer commentId, CreateCommentDTO comment);

    /**
     * Archives comment with given id
     * @param commentId
     * @return
     */
    void archiveComment(Integer commentId);
}
