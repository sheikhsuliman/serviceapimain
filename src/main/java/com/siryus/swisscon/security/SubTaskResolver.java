package com.siryus.swisscon.security;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.tasks.entity.SubTaskEntity;
import com.siryus.swisscon.api.tasks.repos.SubTaskRepository;

import java.io.Serializable;

public class SubTaskResolver implements SiryusPermissionChecker.TargetResolver {
    private final SubTaskRepository subTaskRepository;

    public SubTaskResolver(SubTaskRepository subTaskRepository) {
        this.subTaskRepository = subTaskRepository;
    }

    @Override
    public AuthorizationTarget resolveTarget(Object target) {
        if (SubTaskEntity.class.isAssignableFrom(target.getClass())) {
            return AuthorizationTarget.builder()
                    .projectId(((SubTaskEntity)target).getMainTask().getProjectId())
                    .build();
        }

        throw SecurityException.unsupportedTargetType(target.getClass().toString());
    }

    @Override
    public AuthorizationTarget resolveTarget(Serializable target) {
        final SubTaskEntity subTask = this.subTaskRepository.findById(Integer.valueOf(target.toString()))
                .orElseThrow(() -> SecurityException.requiredResolutionFailed(ReferenceType.SUB_TASK.name(), target));
        return this.resolveTarget(subTask);
    }
}
