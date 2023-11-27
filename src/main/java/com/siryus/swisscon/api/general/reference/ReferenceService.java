package com.siryus.swisscon.api.general.reference;

import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.company.company.CompanyRepository;
import com.siryus.swisscon.api.company.companylegaltype.CompanyLegalTypeRepository;
import com.siryus.swisscon.api.company.numworkersofcompany.NumWorkersOfCompanyRepository;
import com.siryus.swisscon.api.contract.repos.ContractRepository;
import com.siryus.swisscon.api.contract.repos.ContractTaskRepository;
import com.siryus.swisscon.api.file.file.FileRepository;
import com.siryus.swisscon.api.company.bankaccount.BankAccountRepository;
import com.siryus.swisscon.api.contract.repos.ContractCommentRepository;
import com.siryus.swisscon.api.general.country.CountryRepository;
import com.siryus.swisscon.api.general.currency.CurrencyRepository;
import com.siryus.swisscon.api.general.gender.GenderRepository;
import com.siryus.swisscon.api.general.langcode.LangCodeRepository;
import com.siryus.swisscon.api.general.unit.UnitRepository;
import com.siryus.swisscon.api.location.location.LocationRepository;
import com.siryus.swisscon.api.project.project.ProjectRepository;
import com.siryus.swisscon.api.tasks.repos.CommentRepository;
import com.siryus.swisscon.api.tasks.repos.MainTaskRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskCheckListRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskRepository;
import com.siryus.swisscon.api.tasks.repos.TaskLinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static com.siryus.swisscon.api.general.reference.ReferenceType.BANK_ACCOUNT;
import static com.siryus.swisscon.api.general.reference.ReferenceType.COMPANY;
import static com.siryus.swisscon.api.general.reference.ReferenceType.COMPANY_LEGAL_TYPE;
import static com.siryus.swisscon.api.general.reference.ReferenceType.CONTRACT;
import static com.siryus.swisscon.api.general.reference.ReferenceType.CONTRACT_TASK;
import static com.siryus.swisscon.api.general.reference.ReferenceType.CONTRACT_COMMENT;
import static com.siryus.swisscon.api.general.reference.ReferenceType.COUNTRY;
import static com.siryus.swisscon.api.general.reference.ReferenceType.CURRENCY;
import static com.siryus.swisscon.api.general.reference.ReferenceType.FILE;
import static com.siryus.swisscon.api.general.reference.ReferenceType.GENDER;
import static com.siryus.swisscon.api.general.reference.ReferenceType.LANGUAGE;
import static com.siryus.swisscon.api.general.reference.ReferenceType.LOCATION;
import static com.siryus.swisscon.api.general.reference.ReferenceType.MAIN_TASK;
import static com.siryus.swisscon.api.general.reference.ReferenceType.NUM_WORKERS_OF_COMPANY;
import static com.siryus.swisscon.api.general.reference.ReferenceType.PROJECT;
import static com.siryus.swisscon.api.general.reference.ReferenceType.ROLE;
import static com.siryus.swisscon.api.general.reference.ReferenceType.SUB_TASK;
import static com.siryus.swisscon.api.general.reference.ReferenceType.SUB_TASK_CHECK_LIST_ITEM;
import static com.siryus.swisscon.api.general.reference.ReferenceType.SUB_TASK_COMMENT;
import static com.siryus.swisscon.api.general.reference.ReferenceType.TASK_LINK;
import static com.siryus.swisscon.api.general.reference.ReferenceType.TEMPORARY;
import static com.siryus.swisscon.api.general.reference.ReferenceType.UNIT;
import static com.siryus.swisscon.api.general.reference.ReferenceType.USER;
import static com.siryus.swisscon.api.general.reference.ReferenceType.values;

@Service("referenceService")
public class ReferenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceService.class);

    private Map<ReferenceType, JpaRepository> repositoryMap = new EnumMap<>(ReferenceType.class);

    @Autowired
    public ReferenceService(
            CompanyRepository companyRepository,
            ProjectRepository projectRepository,
            LocationRepository locationRepository,
            UserRepository userRepository,
            MainTaskRepository mainTaskRepository,
            SubTaskRepository subTaskRepository,
            SubTaskCheckListRepository checkListRepository,
            CountryRepository countryRepository,
            NumWorkersOfCompanyRepository numWorkersOfCompanyRepository,
            GenderRepository genderRepository,
            LangCodeRepository langCodeRepository,
            CurrencyRepository currencyRepository,
            TaskLinkRepository taskLinkRepository,
            FileRepository fileRepository,
            BankAccountRepository bankAccountRepository,
            RoleRepository roleRepository,
            ContractRepository contractRepository,
            ContractTaskRepository contractTaskRepository,
            ContractCommentRepository contractCommentRepository,
            UnitRepository unitRepository,
            CompanyLegalTypeRepository companyLegalTypeRepository,
            CommentRepository commentRepository
    ) {
        repositoryMap.put(COMPANY, companyRepository);
        repositoryMap.put(PROJECT, projectRepository);
        repositoryMap.put(LOCATION, locationRepository);
        repositoryMap.put(USER, userRepository);
        repositoryMap.put(MAIN_TASK,mainTaskRepository);
        repositoryMap.put(SUB_TASK,subTaskRepository);
        repositoryMap.put(SUB_TASK_CHECK_LIST_ITEM,checkListRepository);
        repositoryMap.put(SUB_TASK_COMMENT,commentRepository);
        repositoryMap.put(COUNTRY, countryRepository);
        repositoryMap.put(GENDER, genderRepository);
        repositoryMap.put(LANGUAGE, langCodeRepository);
        repositoryMap.put(CURRENCY, currencyRepository);
        repositoryMap.put(NUM_WORKERS_OF_COMPANY, numWorkersOfCompanyRepository);
        repositoryMap.put(TASK_LINK, taskLinkRepository);
        repositoryMap.put(TEMPORARY, fileRepository);
        repositoryMap.put(FILE, fileRepository);
        repositoryMap.put(BANK_ACCOUNT, bankAccountRepository);
        repositoryMap.put(ROLE, roleRepository);
        repositoryMap.put(CONTRACT, contractRepository);
        repositoryMap.put(CONTRACT_TASK, contractTaskRepository);
        repositoryMap.put(CONTRACT_COMMENT, contractCommentRepository);
        repositoryMap.put(UNIT, unitRepository);
        repositoryMap.put(COMPANY_LEGAL_TYPE, companyLegalTypeRepository);

        validateRepositoryMap();
    }

    private void validateRepositoryMap() {
        if (values().length != repositoryMap.size()) { //+1 = Temporary has no repository
            String message = "All ReferenceType Enums have to have a matching service to make a foreign key check except Temporary";
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
    }

    @SuppressWarnings("unchecked")
    public boolean isValidReference(ReferenceType referenceType, Object referenceId, boolean canBeDisabled) {
        JpaRepository repository = repositoryMap.get(referenceType);

        if (repository == null) {
            return false;
        }

        if (canBeDisabled) {
            // existsById() is much faster (primary index search) than findById()
            return repository.existsById(referenceId);
        }

        try {
            Optional<Object> reference = repository.findById(referenceId);

            if (!reference.isPresent()) {
                return false;
            }

            Object entity = reference.get();

            Method getDeletedMethod = entity.getClass().getMethod("getDisabled");

            return getDeletedMethod.invoke(entity) == null;
        }
        catch (NoSuchMethodException | IllegalAccessException |  InvocationTargetException e) {
            return true;
        }
    }

    public void validateForeignKey(String referenceType, Integer referenceId) {
        if(!TEMPORARY.equals(ReferenceType.valueOf(referenceType))) {
            getForeignEntity(referenceType, referenceId);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getForeignEntity(String referenceType, Integer referenceId) {
        ReferenceType type = ReferenceType.valueOf(referenceType);

        JpaRepository<T, Integer> repository = repositoryMap.get(type);

        Optional<T> modelOptional = repository.findById(referenceId);

        if (modelOptional.isEmpty()) {
            String message = "Reference with type " + type.toString() + " with id " + referenceId + " not found";
            LOGGER.info(message);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        }
        return modelOptional.get();
    }

    public <T> T getForeignEntityAndValidateType(ReferenceType referenceType, Integer referenceId) {
        T foreignEntity = getForeignEntity(referenceType.name(), referenceId);
        validateType(foreignEntity, referenceType.getReferencedClass());
        return foreignEntity;
    }

    @SuppressWarnings("unchecked")
    private <T> void validateType(Object model, Class<T> clazz) {
        if (!clazz.isAssignableFrom(model.getClass())) {
            String message = "The type " + model.getClass() + " isn't assignable to " + clazz.getName();
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
    }

}
