package com.siryus.swisscon.security;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationTarget {
    private Integer companyId;
    private Integer projectId;

    public boolean hasCompanyId() {
        return null != this.companyId && this.companyId >= 0;
    }

    public boolean hasProjectId() {
        return null != this.projectId && this.projectId >= 0;
    }
}
