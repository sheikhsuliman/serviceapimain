package com.siryus.swisscon.api.company.company;

import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.file.file.FileDTO;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRole;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDirectoryDTO {

    private Integer id;
    private String name;
    private FileDTO picture;
    private TeamUserDTO owner;

    public static CompanyDirectoryDTO from(CompanyUserRole companyUserRole) {

        TeamUserDTO owner = TeamUserDTO.fromCompanyUserRole(companyUserRole);
        Company company = companyUserRole.getCompany();

        return CompanyDirectoryDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .picture(FileDTO.fromFile(company.getPicture()))
                .owner(owner)
                .build();
    }

    public static CompanyDirectoryDTO from(ProjectUserRole projectUserRole, Company company) {

        TeamUserDTO owner = TeamUserDTO.fromProjectUserRole(projectUserRole);

        return CompanyDirectoryDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .picture(FileDTO.fromFile(company.getPicture()))
                .owner(owner)
                .build();
    }

}
