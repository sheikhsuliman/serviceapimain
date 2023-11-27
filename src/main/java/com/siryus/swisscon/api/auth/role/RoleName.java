package com.siryus.swisscon.api.auth.role;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum RoleName {
    PROJECT_OWNER(true),
    @Deprecated
    PROJECT_ADMIN,
    PROJECT_WORKER,
    PROJECT_MANAGER,
    COMPANY_BOOTSTRAP(true),
    COMPANY_OWNER(true),
    @Deprecated
    COMPANY_ADMIN,
    COMPANY_WORKER,
    CUSTOMER(true),
    PROJECT_CUSTOMER(true),
    GLOBAL_NOTIFICATION_ADMIN
    ;

    private final boolean uniqAndMandatory;

    RoleName() {
        this(false);
    }
    RoleName(boolean uniqAndMandatory) {
        this.uniqAndMandatory = uniqAndMandatory;
    }

    private boolean isUniqAndMandatory() {
        return uniqAndMandatory;
    }

    public static boolean isUniqAndMandatory(String roleName) {
        var matchingKnownRoleNames = Arrays.stream(RoleName.values()).filter( rn -> rn.name().equals(roleName)).collect(Collectors.toList());

        if (matchingKnownRoleNames.isEmpty()) {
            return false;
        }

        return matchingKnownRoleNames.get(0).isUniqAndMandatory();
    }
}
