package com.siryus.swisscon.security;

import com.siryus.swisscon.api.contract.repos.ContractCommentEntity;
import com.siryus.swisscon.api.contract.repos.ContractCommentRepository;
import com.siryus.swisscon.api.contract.repos.ContractEntity;
import com.siryus.swisscon.api.contract.repos.ContractRepository;
import com.siryus.swisscon.api.general.reference.ReferenceType;

import java.io.Serializable;

public class ContractCommentResolver implements SiryusPermissionChecker.TargetResolver {
    private final ContractCommentRepository repository;
    private final ContractRepository contractRepository;

    public ContractCommentResolver(
            ContractCommentRepository repository,
            ContractRepository contractRepository
    ) {
        this.repository = repository;
        this.contractRepository = contractRepository;
    }

    @Override
    public AuthorizationTarget resolveTarget(Object target) {
        if (!ContractCommentEntity.class.isAssignableFrom(target.getClass())) {
            throw SecurityException.unsupportedTargetType(target.getClass().toString());
        }

        return AuthorizationTarget.builder().projectId(
                contractRepository.findById(((ContractCommentEntity)target).getContractId())
                .map(ContractEntity::getProjectId)
                .orElseThrow(
                    () -> SecurityException.requiredResolutionFailed(ReferenceType.CONTRACT_COMMENT.name(), target)
                )
        ).build();
    }

    @Override
    public AuthorizationTarget resolveTarget(Serializable target) {
        final ContractCommentEntity contractTask = this.repository.findById(Integer.valueOf(target.toString()))
                .orElseThrow(() -> SecurityException.requiredResolutionFailed(ReferenceType.CONTRACT_COMMENT.name(), target));
        return this.resolveTarget(contractTask);
    }
}