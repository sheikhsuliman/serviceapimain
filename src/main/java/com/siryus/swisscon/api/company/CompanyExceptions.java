package com.siryus.swisscon.api.company;

import com.siryus.swisscon.api.exceptions.LocalizedReason;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;

import java.util.List;

import static com.siryus.swisscon.api.exceptions.LocalizedReason.ParameterValue.pv;

public final class CompanyExceptions {
    private static int e(int n) {
        return LocalizedResponseStatusException.COMPANY_ERROR_CODE + n;
    }

    public static final LocalizedReason COMPANY_WITH_PROJECTS_CAN_NOT_BE_REMOVED = LocalizedReason.like(e(1), "Company cannot be removed as long as it is assigned to projects : {{projectIds}}");
    public static final LocalizedReason COMPANY_CAN_NOT_BE_FOUND = LocalizedReason.like(e(2), "Company with id {{companyId}} can not be found");
    public static final LocalizedReason USER_IN_PROJECTS_CAN_NOT_BE_REMOVED_FROM_COMPANY = LocalizedReason.like(e(3), "User cannot be deleted because he is part in projects: {{projectIds}}");
    public static final LocalizedReason ONE_OWNER_PER_COMPANY = LocalizedReason.like(e(4), "There should be exactly one company owner per company. CompanyId: {{companyId}}");
    public static final LocalizedReason USER_IS_NOT_FROM_COMPANY = LocalizedReason.like(e(5), "User {{userId}} is not part of company {{companyId}}");
    public static final LocalizedReason BANK_ACCOUNT_NOT_FOUND = LocalizedReason.like(e(6), "Bank Account with id {{bankAccountId}} not found");
    public static final LocalizedReason IBAN_NUMBER_NOT_VALID = LocalizedReason.like(e(7), "This is not a valid IBAN number: {{iban}}");
    public static final LocalizedReason BANK_ACCOUNT_DOES_NOT_BELONG_TO_COMPANY = LocalizedReason.like(e(8), "Bank Account with id: {{bankAccountId}} doesn't belong to company with id: {{companyId}}");
    public static final LocalizedReason NEW_BANK_ACCOUNT_CANNOT_HAVE_ID = LocalizedReason.like(e(9), "A new bank account cannot have an id assigned. Remove the id for the request: {[bankAccountId}}");
    public static final LocalizedReason USER_HAS_NO_COMPANY_USER_ROLE = LocalizedReason.like(e(10), "User has no company role at all: {[userId}}");

    private CompanyExceptions() {
    }

    public static LocalizedResponseStatusException companyWithProjectsCanNotBeRemoved(List<Integer> projectIds) {
        return LocalizedResponseStatusException.businessLogicError(COMPANY_WITH_PROJECTS_CAN_NOT_BE_REMOVED.with(pv("projectIds", projectIds)));
    }

    public static LocalizedResponseStatusException companyCanNotBeFound(Integer companyId) {
        return LocalizedResponseStatusException.notFound(COMPANY_CAN_NOT_BE_FOUND.with(pv("companyId", companyId)));
    }

    public static LocalizedResponseStatusException userInProjectsCanNotBeRemoved(List<Integer> projectIds) {
        return LocalizedResponseStatusException.businessLogicError(USER_IN_PROJECTS_CAN_NOT_BE_REMOVED_FROM_COMPANY.with(pv("projectIds", projectIds)));
    }

    public static LocalizedResponseStatusException oneOwnerPerCompany(Integer companyId) {
        return LocalizedResponseStatusException.businessLogicError(ONE_OWNER_PER_COMPANY.with(pv("companyId", companyId)));
    }

    public static LocalizedResponseStatusException userIsNotFromCompany(Integer userId, Integer companyId) {
        return LocalizedResponseStatusException.businessLogicError(USER_IS_NOT_FROM_COMPANY.with(pv("userId", userId), pv("companyId", companyId)));
    }

    public static LocalizedResponseStatusException bankAccountNotFound(Integer bankAccountId) {
        return LocalizedResponseStatusException.notFound(BANK_ACCOUNT_NOT_FOUND.with(pv("bankAccountId", bankAccountId)));
    }

    public static LocalizedResponseStatusException ibanNotValid(String iban) {
        return LocalizedResponseStatusException.badRequest(IBAN_NUMBER_NOT_VALID.with(pv("iban", iban)));
    }

    public static LocalizedResponseStatusException bankAccountDoesNotBelongToCompany(Integer companyId, Integer bankAccountId) {
        return LocalizedResponseStatusException.badRequest(BANK_ACCOUNT_DOES_NOT_BELONG_TO_COMPANY.with(pv("bankAccountId", bankAccountId), pv("companyId", companyId)));
    }

    public static LocalizedResponseStatusException newBankAccountCannotHaveId(Integer bankAccountId) {
        return LocalizedResponseStatusException.badRequest(NEW_BANK_ACCOUNT_CANNOT_HAVE_ID.with(pv("bankAccountId", bankAccountId)));
    }

    public static LocalizedResponseStatusException userHasNoCompanyUserRole(Integer userId) {
        return LocalizedResponseStatusException.internalError(USER_HAS_NO_COMPANY_USER_ROLE.with(pv("userId", userId)));
    }
}
