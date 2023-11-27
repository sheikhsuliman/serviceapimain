package com.siryus.swisscon.security;

import com.siryus.swisscon.api.company.company.Company;

import java.io.Serializable;

public class CompanyResolver implements SiryusPermissionChecker.TargetResolver {
    @Override
    public AuthorizationTarget resolveTarget(Object target) {
        if (!Company.class.isAssignableFrom(target.getClass())) {
            throw SecurityException.unsupportedTargetType(target.getClass().toString());
        }

        return this.resolveTarget(((Company)target).getId());
    }

    @Override
    public AuthorizationTarget resolveTarget(Serializable target) {
        return AuthorizationTarget.builder().companyId(Integer.valueOf(target.toString())).build();
    }
}
