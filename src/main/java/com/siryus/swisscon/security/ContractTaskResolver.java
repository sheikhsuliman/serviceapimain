package com.siryus.swisscon.security;

import com.siryus.swisscon.api.contract.repos.ContractTaskEntity;
import com.siryus.swisscon.api.contract.repos.ContractTaskRepository;
import com.siryus.swisscon.api.general.reference.ReferenceType;

import java.io.Serializable;

public class ContractTaskResolver implements SiryusPermissionChecker.TargetResolver {
    private final ContractTaskRepository repository;

    public ContractTaskResolver(ContractTaskRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuthorizationTarget resolveTarget(Object target) {
        if (!ContractTaskEntity.class.isAssignableFrom(target.getClass())) {
            throw SecurityException.unsupportedTargetType(target.getClass().toString());
        }

        return AuthorizationTarget.builder().projectId(((ContractTaskEntity)target).getProjectId()).build();
    }

    @Override
    public AuthorizationTarget resolveTarget(Serializable target) {
        final ContractTaskEntity contractTask = this.repository.findById(Integer.valueOf(target.toString()))
                .orElseThrow(() -> SecurityException.requiredResolutionFailed(ReferenceType.CONTRACT_TASK.name(), target));
        return this.resolveTarget(contractTask);
    }
}
