package com.siryus.swisscon.security;

import com.siryus.swisscon.api.contract.repos.ContractEntity;
import com.siryus.swisscon.api.contract.repos.ContractRepository;
import com.siryus.swisscon.api.general.reference.ReferenceType;

import java.io.Serializable;

public class ContractResolver implements SiryusPermissionChecker.TargetResolver {
    private final ContractRepository repository;

    public ContractResolver(ContractRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuthorizationTarget resolveTarget(Object target) {
        if (!ContractEntity.class.isAssignableFrom(target.getClass())) {
            throw SecurityException.unsupportedTargetType(target.getClass().toString());
        }

        return AuthorizationTarget.builder().projectId(((ContractEntity)target).getProjectId()).build();
    }

    @Override
    public AuthorizationTarget resolveTarget(Serializable target) {
        final ContractEntity contract = this.repository.findById(Integer.valueOf(target.toString()))
                .orElseThrow(() -> SecurityException.requiredResolutionFailed(ReferenceType.CONTRACT.name(), target));
        return this.resolveTarget(contract);
    }
}
