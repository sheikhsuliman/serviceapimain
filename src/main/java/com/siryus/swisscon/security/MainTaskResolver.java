package com.siryus.swisscon.security;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.tasks.entity.MainTaskEntity;
import com.siryus.swisscon.api.tasks.repos.MainTaskRepository;

import java.io.Serializable;

public class MainTaskResolver implements SiryusPermissionChecker.TargetResolver {
    private final MainTaskRepository taskRepository;

    public MainTaskResolver(MainTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public AuthorizationTarget resolveTarget(Object target) {
        if (MainTaskEntity.class.isAssignableFrom(target.getClass())) {
            return this.fromProject(((MainTaskEntity)target).getProjectId());
        }

        throw SecurityException.unsupportedTargetType(target.getClass().toString());
    }

    @Override
    public AuthorizationTarget resolveTarget(Serializable target) {
        Integer projectId = taskRepository.getProjectIdForTask(Integer.valueOf(target.toString()))
                .orElseThrow(() -> SecurityException.requiredResolutionFailed(ReferenceType.MAIN_TASK.name(), target));
        return this.fromProject(projectId);
    }

    private AuthorizationTarget fromProject(Integer projectId) {
        return AuthorizationTarget.builder().projectId(projectId).build();
    }
}
