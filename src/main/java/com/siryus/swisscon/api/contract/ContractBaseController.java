package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.contract.dto.ContractDTO;
import com.siryus.swisscon.api.contract.dto.ContractSummaryDTO;
import com.siryus.swisscon.api.contract.dto.CreateContractRequest;
import com.siryus.swisscon.api.contract.dto.ListContractsRequest;
import com.siryus.swisscon.api.contract.dto.UpdateContractRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rest/contract")
@Api( tags = {"Contract:base"})
class ContractBaseController {

    private final ContractBaseService baseService;

    @Autowired
    ContractBaseController(ContractBaseService baseService) {
        this.baseService = baseService;
    }

    @GetMapping(path = "{contractId}")
    @ApiOperation(
            value = "Get contract DTO by id",
            tags = {"Contract:base"}
    )
    @PreAuthorize("hasPermission(#contractId, 'CONTRACT', 'CONTRACT_VIEW_DETAILS')")
    ContractDTO getContract( @PathVariable Integer contractId) {
        return baseService.getContract( contractId );
    }

    @PostMapping(path = "create")
    @ApiOperation(
            value = "Create new contract",
            tags = {"Contract:base"}
    )
    @PreAuthorize("hasPermission(#request.projectId, 'PROJECT', 'CONTRACT_CREATE')")
    ContractDTO createContract( @RequestBody CreateContractRequest request) {
        return baseService.createContract(request);
    }

    @PostMapping(path = "{contractId}/update")
    @ApiOperation(
            value = "Update an existing contract",
            tags = {"Contract:base"}
    )
    @PreAuthorize("hasPermission(#contractId, 'CONTRACT', 'CONTRACT_UPDATE')")
    ContractDTO updateContract( @PathVariable Integer contractId, @RequestBody UpdateContractRequest request) {
        return baseService.updateContract(contractId, request);
    }

    @PostMapping(path = "list")
    @ApiOperation(
            value = "List Primary Contracts based on provided search criteria",
            tags = {"Contract:base"}
    )
    @PreAuthorize("hasPermission(#request.projectId, 'PROJECT', 'CONTRACT_VIEW_STATUS')")
    List<ContractSummaryDTO> listContracts(@RequestBody ListContractsRequest request) {
        return baseService.listPrimaryContracts(request);
    }

    @GetMapping(path="primary/{primaryContractId}/list-extensions")
    @ApiOperation(
            value = "List All Primary Contract contract extensions",
            tags = {"Contract:base"}
    )
    @PreAuthorize("hasPermission(#primaryContractId, 'CONTRACT', 'CONTRACT_VIEW_STATUS')")
    List<ContractSummaryDTO> listPrimaryContractExtensions(@PathVariable Integer primaryContractId ){
        return baseService.listPrimaryContractExtensions(primaryContractId);
    }
}
