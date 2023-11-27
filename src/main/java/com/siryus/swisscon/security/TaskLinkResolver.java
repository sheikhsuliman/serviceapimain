package com.siryus.swisscon.security;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.tasks.entity.TaskLinkEntity;
import com.siryus.swisscon.api.tasks.repos.TaskLinkRepository;

import java.io.Serializable;

public class TaskLinkResolver implements SiryusPermissionChecker.TargetResolver {
    private final TaskLinkRepository taskLinkRepository;

    public TaskLinkResolver(TaskLinkRepository taskLinkRepository) {
        this.taskLinkRepository = taskLinkRepository;
    }

    @Override
    public AuthorizationTarget resolveTarget(Object target) {
        if (!TaskLinkEntity.class.isAssignableFrom(target.getClass())) {
            throw SecurityException.unsupportedTargetType(target.getClass().toString());
        }

        return AuthorizationTarget.builder().projectId(((TaskLinkEntity)target).getProject().getId()).build();
    }

    @Override
    public AuthorizationTarget resolveTarget(Serializable target) {
        final TaskLinkEntity location = this.taskLinkRepository.findById(Integer.valueOf(target.toString()))
                .orElseThrow(() -> SecurityException.requiredResolutionFailed(ReferenceType.TASK_LINK.name(), target));
        return this.resolveTarget(location);
    }
}
