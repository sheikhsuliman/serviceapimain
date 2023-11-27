package com.siryus.swisscon.security;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.tasks.entity.CommentEntity;
import com.siryus.swisscon.api.tasks.repos.CommentRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskRepository;

import java.io.Serializable;

public class SubTaskCommentEntityResolver implements SiryusPermissionChecker.TargetResolver {
    private final SubTaskRepository subTaskRepository;
    private final CommentRepository repository;

    public SubTaskCommentEntityResolver(SubTaskRepository subTaskRepository, CommentRepository repository) {
        this.subTaskRepository = subTaskRepository;
        this.repository = repository;
    }

    @Override
    public AuthorizationTarget resolveTarget(Object target) {
        if (CommentEntity.class.isAssignableFrom(target.getClass())) {
            return AuthorizationTarget.builder()
                .projectId(
                    subTaskRepository.findById(((CommentEntity)target).getSubTaskId())
                        .orElseThrow(() -> SecurityException.requiredResolutionFailed(ReferenceType.SUB_TASK.name(), ((CommentEntity)target).getSubTaskId()))
                            .getMainTask().getProjectId()
                )
                .build();
        }
        throw SecurityException.unsupportedTargetType(target.getClass().toString());
    }

    @Override
    public AuthorizationTarget resolveTarget(Serializable target) {
        return resolveTarget(
            repository.findById(Integer.parseInt(target.toString()))
                .orElseThrow(() -> SecurityException.requiredResolutionFailed(ReferenceType.SUB_TASK_COMMENT.name(), target))
        );
    }
}
