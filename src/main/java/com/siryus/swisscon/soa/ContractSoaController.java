package com.siryus.swisscon.soa;

import com.siryus.swisscon.api.contract.ContractPublicService;
import com.siryus.swisscon.api.contract.dto.ContractDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/soa/")
public class ContractSoaController {

    private final ContractPublicService contractPublicService;

    @Autowired
    public ContractSoaController(ContractPublicService contractPublicService) {
        this.contractPublicService = contractPublicService;
    }

    @GetMapping(path="contract/{contractId}")
    @ApiOperation(value = "Retrieve contract details")
    public ContractDTO getProject(@PathVariable Integer contractId) {
        return contractPublicService.getContract(contractId);
    }
}
