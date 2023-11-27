package com.siryus.swisscon.api.project;

import com.siryus.swisscon.api.exceptions.LocalizedReason;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;

import static com.siryus.swisscon.api.exceptions.LocalizedReason.ParameterValue.pv;

public final class ProjectException {
    private static int e(int n) {
        return LocalizedResponseStatusException.PROJECT_ERROR_CODE + n;
    }

    public static final LocalizedReason COMPANY_ALREADY_PART_OF_PROJECT = LocalizedReason.like(e(1), "Company  is already part of the project: Project: {{projectId}} Company: {{companyId}}");
    public static final LocalizedReason COMPANY_IS_NOT_PART_OF_PROJECT = LocalizedReason.like(e(2), "Company  is not part of the project: Project: {{projectId}} Company: {{companyId}}");
    public static final LocalizedReason PROJECT_NOT_FOUND = LocalizedReason.like(e(3), "Project with id {{projectId}} does not exist");
    public static final LocalizedReason PROJECT_TYPE_NOT_FOUND = LocalizedReason.like(e(4), "Project type with id {{projectTypeId}} does not exist");
    public static final LocalizedReason PROJECT_HAS_NO_OWNER = LocalizedReason.like(e(5), "Project {{projectId}} has no owner");

    public static final LocalizedReason INVALID_COMPANY_OR_USER = LocalizedReason.like(e(6), "Invalid company or user id");
    public static final LocalizedReason CAN_NOT_ADD_USER_FROM_OTHER_COMPANY_TO_PROJECT = LocalizedReason.like(e(7), "Can not add user from another company to project");
    public static final LocalizedReason USER_ALREADY_PART_OF_PROJECT = LocalizedReason.like(e(8), "User already exists in project");
    public static final LocalizedReason USER_CAN_NOT_BE_REMOVED_ASSIGNED_TO_TASK = LocalizedReason.like(e(9), "The user is assigned to tasks, he cannot be removed: (User = {{userId}}, Tasks = {{taskIds}})");
    public static final LocalizedReason USER_WITH_ROLE_CAN_NOT_BE_REMOVED = LocalizedReason.like(e(10), "The user with role {{roleName}} cannot be removed: (User = {{userId}})");
    public static final LocalizedReason USER_IS_NOT_PART_OF_THE_PROJECT = LocalizedReason.like(e(11), "User isn't part of the Project (Project = {{projectId}} User = {{userId}})");
    public static final LocalizedReason USER_CAN_NOT_BE_REMOVED_COMPANY_OWNER = LocalizedReason.like(e(12), "The user is company owner, he cannot be removed: (User = {{userId}})");
    public static final LocalizedReason CAN_NOT_CHANGE_ROLE_NOT_YOUR_COMPANY = LocalizedReason.like(e(13), "The role cannot be changed if it's not your own company");
    public static final LocalizedReason NON_ADMIN_CAN_NOT_ELEVATE_TO_ADMIN_ROLE = LocalizedReason.like(e(14), "The target role or the existing role is project admin.");
    public static final LocalizedReason ROLE_DOES_NOT_EXIST = LocalizedReason.like(e(15), "Role {{roleId}} does not exist");
    public static final LocalizedReason PROJECT_OWNER_ROLE_CAN_NOT_BE_DOWNGRADED = LocalizedReason.like(e(16), "The role to change has to be a project role and it cannot be the project owner role");
    public static final LocalizedReason PROJECT_ALREADY_HAS_CUSTOMER_SET = LocalizedReason.like(e(17), "Project already has customer");
    public static final LocalizedReason PROJECT_ALREADY_HAS_CONTRACTOR_SET = LocalizedReason.like(e(18), "Project already has contractor");

    private static final LocalizedReason INVALID_PURS = LocalizedReason.like(e(19), "Project roles are in invalid state for project {{projectId}} and company {{companyId}}");
    private static final LocalizedReason CAN_NOT_CHANGE_CUSTOMER_WITH_ACTIVE_CONTRACTS = LocalizedReason.like(e(20), "Can not change customer for project {{projectId}}, this project has at least one active contract");

    public static LocalizedResponseStatusException companyAlreadyPartOfProject(Integer companyId, Integer projectId) {
        return LocalizedResponseStatusException.businessLogicError(COMPANY_ALREADY_PART_OF_PROJECT.with(pv("companyId", companyId), pv("projectId", projectId)));
    }

    public static LocalizedResponseStatusException companyIsNotPartOfProject(Integer companyId, Integer projectId) {
        return LocalizedResponseStatusException.businessLogicError(COMPANY_IS_NOT_PART_OF_PROJECT.with(pv("companyId", companyId), pv("projectId", projectId)));
    }

    public static LocalizedResponseStatusException projectNotFound(Integer projectId) {
        return LocalizedResponseStatusException.notFound(PROJECT_NOT_FOUND.with(pv("projectId", projectId)));
    }

    public static LocalizedResponseStatusException projectTypeNotFound(Integer projectTypeId) {
        return LocalizedResponseStatusException.notFound(PROJECT_TYPE_NOT_FOUND.with(pv("projectTypeId", projectTypeId)));
    }

    public static LocalizedResponseStatusException projectHasNoOwner(Integer projectId) {
        return LocalizedResponseStatusException.internalError(PROJECT_HAS_NO_OWNER.with(pv("projectId", projectId)));
    }


    public static LocalizedResponseStatusException invalidCompanyOrUser() {
        return LocalizedResponseStatusException.badRequest(INVALID_COMPANY_OR_USER.with());
    }

    public static LocalizedResponseStatusException canNotAddUserFromOtherCompanyToProject() {
        return LocalizedResponseStatusException.notPermitted(CAN_NOT_ADD_USER_FROM_OTHER_COMPANY_TO_PROJECT.with());
    }

    public static LocalizedResponseStatusException userAlreadyPartOfProject() {
        return LocalizedResponseStatusException.businessLogicError(USER_ALREADY_PART_OF_PROJECT.with());
    }

    public static LocalizedResponseStatusException userAssignedToTasksCanNotBeRemoved(Integer userId, String taskIds) {
        return LocalizedResponseStatusException.businessLogicError(USER_CAN_NOT_BE_REMOVED_ASSIGNED_TO_TASK.with(pv("userId", userId), pv("taskIds", taskIds)));
    }

    public static LocalizedResponseStatusException projectUserWithRoleCanNotBeRemoved(Integer userId, String roleName) {
        return LocalizedResponseStatusException.businessLogicError(USER_WITH_ROLE_CAN_NOT_BE_REMOVED.with(pv("userId", userId), pv("roleName", roleName)));
    }

    public static LocalizedResponseStatusException userIsNotPartOfProject(Integer userId, Integer projectId) {
        return LocalizedResponseStatusException.businessLogicError(USER_IS_NOT_PART_OF_THE_PROJECT.with(pv("userId", userId), pv("projectId", projectId)));
    }

    public static LocalizedResponseStatusException companyOwnerCanNotBeRemoved(Integer userId) {
        return LocalizedResponseStatusException.businessLogicError(USER_CAN_NOT_BE_REMOVED_COMPANY_OWNER.with(pv("userId", userId)));
    }

    public static LocalizedResponseStatusException canNotChangeRoleInOtherCompany() {
        return LocalizedResponseStatusException.notPermitted(CAN_NOT_CHANGE_ROLE_NOT_YOUR_COMPANY.with());
    }

    public static LocalizedResponseStatusException nonAdminCanNotElevateToAdminRole() {
        return LocalizedResponseStatusException.notPermitted(NON_ADMIN_CAN_NOT_ELEVATE_TO_ADMIN_ROLE.with());
    }

    public static LocalizedResponseStatusException roleDoesNotExists(Integer roleId) {
        return LocalizedResponseStatusException.notFound(ROLE_DOES_NOT_EXIST.with(pv("roleId", roleId)));
    }

    public static LocalizedResponseStatusException projectOwnerRoleCanNotBeDowngraded() {
        return LocalizedResponseStatusException.businessLogicError(PROJECT_OWNER_ROLE_CAN_NOT_BE_DOWNGRADED.with());
    }

    public static LocalizedResponseStatusException projectAlreadyHasCustomerSet() {
        return LocalizedResponseStatusException.businessLogicError(PROJECT_ALREADY_HAS_CUSTOMER_SET.with());
    }

    public static LocalizedResponseStatusException projectAlreadyHasContractorSet() {
        return LocalizedResponseStatusException.businessLogicError(PROJECT_ALREADY_HAS_CONTRACTOR_SET.with());
    }

    public static LocalizedResponseStatusException invalidPURs(Integer projectId, Integer companyId) {
        return LocalizedResponseStatusException.internalError(
                INVALID_PURS.with(pv("projectId", projectId), pv("companyId",companyId))
        );
    }

    public static LocalizedResponseStatusException canNotChangeCustomerActiveContracts(Integer projectId) {
        return LocalizedResponseStatusException.internalError(
                CAN_NOT_CHANGE_CUSTOMER_WITH_ACTIVE_CONTRACTS.with(pv("projectId", projectId))
        );
    }
}
