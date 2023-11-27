package com.siryus.swisscon.security;

import com.siryus.swisscon.api.project.project.Project;

import java.io.Serializable;

public class ProjectResolver implements SiryusPermissionChecker.TargetResolver {
    @Override
    public AuthorizationTarget resolveTarget(Object target) {
        if (!Project.class.isAssignableFrom(target.getClass())) {
            throw new IllegalArgumentException("Can only resolve " + Project.class.toString());
        }

        return this.resolveTarget(((Project)target).getId());
    }

    @Override
    public AuthorizationTarget resolveTarget(Serializable target) {
        // this is already the project id
        return AuthorizationTarget.builder().projectId(Integer.valueOf(target.toString())).build();
    }
}