package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.contract.dto.ContractDTO;
import com.siryus.swisscon.api.contract.dto.ContractSummaryDTO;
import com.siryus.swisscon.api.contract.dto.ListContractsRequest;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.List;

public interface ContractPublicService {
    ContractDTO getContract(@Reference(ReferenceType.CONTRACT) Integer contractId);

    List<ContractSummaryDTO> listPrimaryContracts(@Valid ListContractsRequest request);

    default List<ContractSummaryDTO> listContractsForProject(Integer projectId) {
        return listPrimaryContracts(ListContractsRequest.builder().projectId(projectId).build());
    }

    boolean projectCustomerIsReassignable(@Reference(ReferenceType.PROJECT) Integer projectId);

    @Transactional
    void updateProjectContractsCustomers(
            @Reference(ReferenceType.PROJECT) Integer projectId,
            @Reference(ReferenceType.COMPANY) Integer customerCompanyId
    );
}
