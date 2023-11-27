package com.siryus.swisscon.security;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.tasks.entity.SubTaskCheckListEntity;
import com.siryus.swisscon.api.tasks.repos.SubTaskCheckListRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskRepository;

import java.io.Serializable;

public class SubTaskCheckListEntityResolver implements SiryusPermissionChecker.TargetResolver {
    private final SubTaskRepository subTaskRepository;
    private final SubTaskCheckListRepository repository;

    public SubTaskCheckListEntityResolver(SubTaskRepository subTaskRepository, SubTaskCheckListRepository repository) {
        this.subTaskRepository = subTaskRepository;
        this.repository = repository;
    }

    @Override
    public AuthorizationTarget resolveTarget(Object target) {
        if (SubTaskCheckListEntity.class.isAssignableFrom(target.getClass())) {
            return AuthorizationTarget.builder()
                .projectId(
                    subTaskRepository.findById(((SubTaskCheckListEntity)target).getSubTaskId())
                        .orElseThrow(() -> SecurityException.requiredResolutionFailed(ReferenceType.SUB_TASK.name(), ((SubTaskCheckListEntity)target).getSubTaskId()))
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
                .orElseThrow(() -> SecurityException.requiredResolutionFailed(ReferenceType.SUB_TASK_CHECK_LIST_ITEM.name(), target))
        );
    }
}
