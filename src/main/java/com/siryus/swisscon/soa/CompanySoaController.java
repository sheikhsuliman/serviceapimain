package com.siryus.swisscon.soa;

import com.siryus.swisscon.api.company.company.CompanyDirectoryDTO;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/soa/")
public class CompanySoaController {
    private final CompanyUserRoleService companyUserRoleService;

    @Autowired
    public CompanySoaController(
            CompanyUserRoleService companyUserRoleService
    ) {
        this.companyUserRoleService = companyUserRoleService;
    }

    @GetMapping(path="company-directory/{companyId}")
    @ApiOperation(value = "Retrieve company details")
    public CompanyDirectoryDTO getCompany(@PathVariable Integer companyId) {
        return CompanyDirectoryDTO.from(companyUserRoleService.getCompanyOwner(companyId));
    }
}
