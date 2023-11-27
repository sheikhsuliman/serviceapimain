package com.siryus.swisscon.api.project.projectuserrole;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUserRoleDTO {

    private Integer userId;
    private Integer roleId;

}
