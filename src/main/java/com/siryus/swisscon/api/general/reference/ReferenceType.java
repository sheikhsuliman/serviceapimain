package com.siryus.swisscon.api.general.reference;

import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.company.bankaccount.BankAccountEntity;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.companylegaltype.CompanyLegalType;
import com.siryus.swisscon.api.company.numworkersofcompany.NumWorkersOfCompany;
import com.siryus.swisscon.api.contract.repos.ContractCommentEntity;
import com.siryus.swisscon.api.contract.repos.ContractEntity;
import com.siryus.swisscon.api.contract.repos.ContractTaskEntity;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.general.GeneralException;
import com.siryus.swisscon.api.general.country.Country;
import com.siryus.swisscon.api.general.currency.Currency;
import com.siryus.swisscon.api.general.gender.Gender;
import com.siryus.swisscon.api.general.langcode.LangCode;
import com.siryus.swisscon.api.general.unit.Unit;
import com.siryus.swisscon.api.location.location.Location;
import com.siryus.swisscon.api.project.project.Project;
import com.siryus.swisscon.api.tasks.entity.CommentEntity;
import com.siryus.swisscon.api.tasks.entity.MainTaskEntity;
import com.siryus.swisscon.api.tasks.entity.SubTaskCheckListEntity;
import com.siryus.swisscon.api.tasks.entity.SubTaskEntity;
import com.siryus.swisscon.api.tasks.entity.TaskLinkEntity;

import java.util.Optional;

public enum ReferenceType {
    COMPANY(Company.class),
    PROJECT(Project.class),
    LOCATION(Location.class),
    USER(User.class),
    TEMPORARY(File.class), // We do want to rename it to TEMPORARY_FILE ... but it is fairly difficult to do without breaking things
    MAIN_TASK(MainTaskEntity.class),
    SUB_TASK(SubTaskEntity.class),
    SUB_TASK_CHECK_LIST_ITEM(SubTaskCheckListEntity.class),
    SUB_TASK_COMMENT(CommentEntity.class),
    COUNTRY(Country.class),
    GENDER(Gender.class),
    LANGUAGE(LangCode.class),
    CURRENCY(Currency.class),
    NUM_WORKERS_OF_COMPANY(NumWorkersOfCompany.class),
    TASK_LINK(TaskLinkEntity.class),
    FILE(File.class),
    BANK_ACCOUNT(BankAccountEntity.class),
    ROLE(Role.class),
    CONTRACT(ContractEntity.class),
    CONTRACT_TASK(ContractTaskEntity.class),
    CONTRACT_COMMENT(ContractCommentEntity.class),    
    UNIT(Unit.class),
    COMPANY_LEGAL_TYPE(CompanyLegalType.class)
    ;

    private final Class referencedClass;

    ReferenceType(Class referencedClass) {
        this.referencedClass = referencedClass;
    }

    public static boolean isValidReferenceType(String value) {
        try {
            ReferenceType.valueOf(value);
            return true;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }

    public Class getReferencedClass() {
        return Optional.ofNullable(referencedClass)
                .orElseThrow(() -> GeneralException.canNotMapReferenceTypeToClass(this));
    }
}
