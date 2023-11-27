package com.siryus.swisscon.security;

import com.siryus.swisscon.api.auth.permission.PermissionName;
import com.siryus.swisscon.api.exceptions.LocalizedReason;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;

import static com.siryus.swisscon.api.exceptions.LocalizedReason.ParameterValue.pv;

public class SecurityException {
    private static int e(int n) {
        return LocalizedResponseStatusException.SECURITY_ERROR_CODE + n;
    }

    public static final LocalizedReason USER_NOT_PART_OF_COMPANY = LocalizedReason.like(e(1), "User {{userId}} is not part of company {{companyId}}");
    public static final LocalizedReason USER_NOT_PART_OF_PROJECT = LocalizedReason.like(e(2), "User {{userId}} is not part of project {{projectId}} team");
    public static final LocalizedReason USER_NOT_PART_OF_TASK_TEAM = LocalizedReason.like(e(3), "User {{userId}} is not part of task {{taskId}} team");
    public static final LocalizedReason USER_NOT_PART_OF_SUB_TASK_TEAM = LocalizedReason.like(e(4), "User {{userId}} is not part of sub-task {{subTaskId}} team");
    public static final LocalizedReason USER_DOES_NOT_HAVE_PERMISSION = LocalizedReason.like(e(5), "User {{userId}} does not have permissions {{permission}}");

    public static final LocalizedReason NO_SUCH_TARGET_TYPE = LocalizedReason.like(e(6), "No such target type {{targetClass}}");
    public static final LocalizedReason UNSUPPORTED_TARGET_TYPE = LocalizedReason.like(e(7), "Unsupported target type {{targetClass}}");
    public static final LocalizedReason UNABLE_TO_RESOLVE_TARGET_OF_TYPE = LocalizedReason.like(e(8), "Unable to resolve a required target {{targetId}} of type {{targetType}}");
    public static final LocalizedReason USER_IS_NOT_PART_OF_CURRENT_USER_COMPANY = LocalizedReason.like(e(9), "User isn't part of current users company {{userId}}");

    public static LocalizedResponseStatusException userNotPartOfCompany(Integer userId, Integer companyId) {
        return LocalizedResponseStatusException.notPermitted(USER_NOT_PART_OF_COMPANY.with(pv("userId", userId), pv("companyId", companyId)));
    }

    public static LocalizedResponseStatusException userNotPartOfProject(Integer userId, Integer projectId) {
        return LocalizedResponseStatusException.notPermitted(USER_NOT_PART_OF_PROJECT.with(pv("userId", userId), pv("projectId", projectId)));
    }

    public static LocalizedResponseStatusException userNotPartOfTaskTeam(Integer userId, Integer taskId) {
        return LocalizedResponseStatusException.notPermitted(USER_NOT_PART_OF_TASK_TEAM.with(pv("userId", userId), pv("taskId", taskId)));
    }

    public static LocalizedResponseStatusException userNotPartOfSubTaskTeam(Integer userId, Integer subTaskId) {
        return LocalizedResponseStatusException.notPermitted(USER_NOT_PART_OF_SUB_TASK_TEAM.with(pv("userId", userId), pv("subTaskId", subTaskId)));
    }

    public static LocalizedResponseStatusException userDoesNotHavePermission(Integer userId, PermissionName permission) {
        return LocalizedResponseStatusException.notPermitted(USER_DOES_NOT_HAVE_PERMISSION.with(pv("userId", userId), pv("permission", permission.name())));
    }

    public static LocalizedResponseStatusException unknownTargetType(String targetType) {
        return LocalizedResponseStatusException.notPermitted(NO_SUCH_TARGET_TYPE.with(pv("targetType", targetType)));
    }

    public static LocalizedResponseStatusException unsupportedTargetType(String targetType) {
        return LocalizedResponseStatusException.notPermitted(UNSUPPORTED_TARGET_TYPE.with(pv("targetType", targetType)));
    }

    public static LocalizedResponseStatusException requiredResolutionFailed(String targetType, Object targetId) {
        return LocalizedResponseStatusException.notPermitted(UNABLE_TO_RESOLVE_TARGET_OF_TYPE.with(pv("targetType", targetType),pv("targetId", targetId)));
    }

    public static LocalizedResponseStatusException userIsNotPartOfCurrentUserCompany(Integer userId) {
        return LocalizedResponseStatusException.notPermitted(USER_IS_NOT_PART_OF_CURRENT_USER_COMPANY.with(pv("userId", userId)));
    }
}
