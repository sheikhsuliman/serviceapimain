package com.siryus.swisscon.api.customroles;

import com.siryus.swisscon.api.exceptions.LocalizedReason;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;

import static com.siryus.swisscon.api.exceptions.LocalizedReason.ParameterValue.pv;

public class CustomRoleExceptions {
    private static int e(int n) {
        return LocalizedResponseStatusException.CUSTOM_ROLE_ERROR_CODE + n;
    }

    static final LocalizedReason ROLE_WITH_NAME_DOES_NOT_EXIST = LocalizedReason.like(e(1), "Role with name {{roleName}} does not exist");
    static final LocalizedReason ROLE_WITH_ID_DOES_NOT_EXIST = LocalizedReason.like(e(2), "Role with id {{roleId}} does not exist");
    static final LocalizedReason ROLE_ALREADY_EXIST = LocalizedReason.like(e(3), "Role with name {{roleName}} already exist");
    static final LocalizedReason PERMISSION_DOES_NOT_EXIST = LocalizedReason.like(e(4), "Permission {{permissionName}}:{{permissionId}} does not exist");
    static final LocalizedReason ROLE_HAS_TO_HAVE_ID = LocalizedReason.like(e(5), "Role has to have id");
    static final LocalizedReason ROLE_IS_NOT_PROJECT_ROLE = LocalizedReason.like(e(6), "Role {{roleName}} is not project role");
    static final LocalizedReason ROLE_IS_NOT_COMPANY_ROLE = LocalizedReason.like(e(7), "Role {{roleName}} is not company role");
    static final LocalizedReason USER_IS_NOT_MEMBER_OF_ANY_COMPANY = LocalizedReason.like(e(8), "User {{userId}} is not a member of any company");
    static final LocalizedReason USER_IS_MEMBER_OF_MORE_THAN_ONE_COMPANY = LocalizedReason.like(e(9), "User {{userId}} is member of more than one company");
    static final LocalizedReason USER_COMPANY_IS_NOT_PART_OF_PROJECT = LocalizedReason.like(e(10), "User {{userId}} Company {{companyId}} is not part of Project {{projectId}} team");
    static final LocalizedReason CAN_NOT_UN_ASSIGN_ALL_COMPANY_ROLES = LocalizedReason.like(e(11), "Can not un-assign all company roles. Use remove member from company instead.");
    static final LocalizedReason CAN_NOT_UN_ASSIGN_ALL_PROJECT_ROLES = LocalizedReason.like(e(12), "Can not un-assign all project roles. Use remove member from project instead.");

    static final LocalizedReason CAN_NOT_REASSIGN_UNIQ_AND_MANDATORY_ROLES = LocalizedReason.like(e(13), "Can not re-assign uniq and mandatory roles");
    static final LocalizedReason CAN_NOT_MUTATE_SYSTEM_ROLE = LocalizedReason.like(e(14), "Can not mutate system role {{roleName}}");
    static final LocalizedReason ROLE_HAS_TO_HAVE_PERMISSIONS = LocalizedReason.like(e(15), "Role has to have at least one permission");


    static LocalizedResponseStatusException roleDoesNotExist(String roleName) {
        return LocalizedResponseStatusException.notFound(ROLE_WITH_NAME_DOES_NOT_EXIST.with(pv("roleName", roleName)));
    }

    public static LocalizedResponseStatusException roleDoesNotExist(Integer roleId) {
        return LocalizedResponseStatusException.notFound(ROLE_WITH_ID_DOES_NOT_EXIST.with(pv("roleId", roleId)));
    }

    public static LocalizedResponseStatusException roleWithNameAlreadyExists(String roleName) {
        return LocalizedResponseStatusException.badRequest(ROLE_ALREADY_EXIST.with(pv("roleName", roleName)));
    }

    public static LocalizedResponseStatusException permissionDoesNotExist(String permissionName, Integer permissionId) {
        return LocalizedResponseStatusException.notFound(
                PERMISSION_DOES_NOT_EXIST.with(pv("permissionName", permissionName), pv("permissionId", permissionId))
        );
    }

    public static LocalizedResponseStatusException roleHasToHaveId() {
        return LocalizedResponseStatusException.notFound(
                ROLE_HAS_TO_HAVE_ID.with()
        );
    }

    public static LocalizedResponseStatusException roleIsNotProjectRole(String roleName) {
        return LocalizedResponseStatusException.badRequest(ROLE_IS_NOT_PROJECT_ROLE.with(pv("roleName", roleName)));
    }

    public static LocalizedResponseStatusException roleIsNotCompanyRole(String roleName) {
        return LocalizedResponseStatusException.badRequest(ROLE_IS_NOT_COMPANY_ROLE.with(pv("roleName", roleName)));
    }

    public static LocalizedResponseStatusException userIsNotMemberOfAnyCompany(Integer userId) {
        return LocalizedResponseStatusException.badRequest(USER_IS_NOT_MEMBER_OF_ANY_COMPANY.with(pv("userId", userId)));
    }

    public static LocalizedResponseStatusException userIsMemberOfMoreThanOneCompany(Integer userId) {
        return LocalizedResponseStatusException.badRequest(USER_IS_MEMBER_OF_MORE_THAN_ONE_COMPANY.with(pv("userId", userId)));
    }

    public static LocalizedResponseStatusException userCompanyIsNotPartOfTheProject(Integer userId, Integer companyId, Integer projectId) {
        return LocalizedResponseStatusException.badRequest(USER_COMPANY_IS_NOT_PART_OF_PROJECT.with(pv("userId", userId), pv("companyId", companyId), pv("projectId", projectId)));
    }

    public static LocalizedResponseStatusException canNotUnAssignAllCompanyRoles() {
        return LocalizedResponseStatusException.badRequest(CAN_NOT_UN_ASSIGN_ALL_COMPANY_ROLES.with());
    }

    public static LocalizedResponseStatusException canNotUnAssignAllProjectRoles() {
        return LocalizedResponseStatusException.badRequest(CAN_NOT_UN_ASSIGN_ALL_PROJECT_ROLES.with());
    }

    public static LocalizedResponseStatusException canNotReassignUniqAndMandatoryRoles() {
        return LocalizedResponseStatusException.badRequest(CAN_NOT_REASSIGN_UNIQ_AND_MANDATORY_ROLES.with());
    }

    public static LocalizedResponseStatusException canNotMutateSystemRole(String roleName) {
        return LocalizedResponseStatusException.badRequest(CAN_NOT_MUTATE_SYSTEM_ROLE.with(pv("roleName", roleName)));
    }

    public static LocalizedResponseStatusException roleHasToHaveAtLeastOnePermission() {
        return LocalizedResponseStatusException.badRequest(ROLE_HAS_TO_HAVE_PERMISSIONS.with());
    }
}
