package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.contract.dto.ContractDTO;
import com.siryus.swisscon.api.contract.dto.ContractEvent;
import com.siryus.swisscon.api.contract.dto.ContractState;
import com.siryus.swisscon.api.exceptions.LocalizedReason;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import static com.siryus.swisscon.api.exceptions.LocalizedReason.ParameterValue.pv;

public class ContractExceptions {
    private static int e(int n) {
        return LocalizedResponseStatusException.CONTRACT_ERROR_CODE + n;
    }

    static final LocalizedReason CONTRACT_WITH_ID_DOES_NOT_EXIST = LocalizedReason.like(e(1), "Contract with id {{contractId}} does not exist");
    static final LocalizedReason NON_UNIQ_CONTRACT_NAME = LocalizedReason.like(e(2), "Contract name '{{contractName}}' in not uniq in project {{projectId}} ");
    static final LocalizedReason CONTRACT_IS_IMMUTABLE = LocalizedReason.like(e(3), "Contract '{{contractId}}' in project {{projectId}} is in immutable state {{contractState}}");
    static final LocalizedReason TASK_DOES_NOT_BELONG_TO_SAME_PROJECT_AS_CONTRACT = LocalizedReason.like(e(4), "Task does not belong to the same project as the contract: ContractId: `{{contractId}}`, ProjectId: `{{projectId}}`");
    static final LocalizedReason TASK_ALREADY_EXISTS_IN_CONTRACT = LocalizedReason.like(e(5), "Task already exists in Contract. ContractId: `{{contractId}`, TaskId: `{{taskId}}`");
    static final LocalizedReason TASK_HAS_NOT_THE_INITIAL_STATUS = LocalizedReason.like(e(6), "Task has not the initial status, it has been already worked on, TaskId `{{taskId}}`, Status: `{{status}}`");
    static final LocalizedReason TASK_IS_ALREADY_PART_OF_ANOTHER_ACTIVE_CONTRACT = LocalizedReason.like(e(7), "Task is already part of another active contract, TaskId: `{{taskId}}`, ContractId: `{{contractId}}`");
    static final LocalizedReason DUPLICATE_TASK_IDS_IN_ADD_TASKS_REQUEST = LocalizedReason.like(e(8), "Duplicate Ids in Add Task Request");
    static final LocalizedReason CANNOT_ADD_EMPTY_COMMENT = LocalizedReason.like(e(9), "Cannot add empty comment on contract with id {{contractId}}");
    static final LocalizedReason USER_NOT_PART_OF_CONTRACT_COMPANIES = LocalizedReason.like(e(10), "User with id {{userId}} is not part of the companies working on contract with id {{contractId}}");
    static final LocalizedReason CONTRACT_TASK_WITH_ID_DOES_NOT_EXIST = LocalizedReason.like(e(11), "Contract Task with id {{contractTaskId}} does not exist");
    static final LocalizedReason TASK_HAS_NOT_THE_RIGHT_STATUS_TO_REMOVE = LocalizedReason.like(e(12), "Task has not the right status to remove, TaskId `{{taskId}}`, Status: `{{status}}`");
    static final LocalizedReason CURRENT_USER_NEITHER_CUSTOMER_NO_CONTRACTOR = LocalizedReason.like(e(13), "Current user neither customer no contractor in project {{projectId}}");
    static final LocalizedReason TASK_IS_PART_OF_MULTIPLE_NON_DECLINED_CONTRACTS = LocalizedReason.like(e(14), "Task is part of multiple non declined contracts: {{taskId}}");
    static final LocalizedReason CONTRACT_HAS_BEEN_DELETED = LocalizedReason.like(e(15), "Contract with id {{contractId}} has been deleted");
    static final LocalizedReason FAIL_TO_SAVE_CONTRACT_EVENT = LocalizedReason.like(e(16), "Fail to save contract event");
    static final LocalizedReason CONTRACT_DOES_NOT_ALLOW_EVENT = LocalizedReason.like(e(17), "Contract is in {{contractState}} state which does not allow {{event}} event");
    static final LocalizedReason CONTRACT_CUSTOMER_ALREADY_SET = LocalizedReason.like(e(18), "Can not set contract customer to {{customerId}}, it is already set to {{contractCustomerId}}");
    static final LocalizedReason COMMENT_DOES_NOT_EXIST = LocalizedReason.like(e(19), "Comment with ID {{commentId}} does not exist");

    static final LocalizedReason METHOD_CAN_NOT_BE_USED = LocalizedReason.like(e(20), "Method can not be used for this purposes");
    static final LocalizedReason CONTRACT_CONTRACTOR_ALREADY_SET = LocalizedReason.like(e(21), "Can not set contract contractor to {{contractorId}}, it is already set to {{contractContractorId}}");

    static final LocalizedReason CONTRACT_DOES_NOT_HAVE_ANY_TASKS = LocalizedReason.like(e(22), "Contract '{{contractId}}' does not have any tasks associated with it");
    static final LocalizedReason CONTRACT_HAS_SOME_UN_PRICED_TASKS = LocalizedReason.like(e(23), "Contract '{{contractId}}' has some un-priced tasks");
    static final LocalizedReason CONTRACT_CUSTOMER_NOT_SET = LocalizedReason.like(e(24), "Contract '{{contractId}}' customer not set");
    static final LocalizedReason PROJECT_HAS_NO_CUSTOMER_SET = LocalizedReason.like(e(25), "Project '{{projectId}}' has no customer set");
    static final LocalizedReason PRIMARY_CONTRACT_BELONGS_TO_OTHER_PROJECT = LocalizedReason.like(e(26), "Primary contract belongs to other project");
    static final LocalizedReason PRIMARY_CONTRACT_IN_INCORRECT_STATE = LocalizedReason.like(e(27), "Primary contract is in incorrect state {{contractState}}");
    static final LocalizedReason PRIMARY_CONTRACT_CAN_NOT_BE_SUBCONTRACT = LocalizedReason.like(e(28), "Primary contract can not be sub-contract");
    static final LocalizedReason CONTRACT_QUICK_LINK_ACTION_NOT_SUPPORTED = LocalizedReason.like(e(29), "Action '{{projectId}}' not supported for contract quicklink");

    static final LocalizedReason CONTRACT_IS_NOT_SUBCONTRACT = LocalizedReason.like(e(30), "Contract '{{contractId}}' is not sub-contract");
    static final LocalizedReason CONTRACT_IS_NOT_PRIMARY_CONTRACT = LocalizedReason.like(e(31), "Contract '{{contractId}}' is not primary contract");
    static final LocalizedReason CAN_NOT_NEGATE_TASK_IN_CONTRACT = LocalizedReason.like(e(32), "Can not negate task {{contractTaskId}} in contract {{contractId}}");
    static final LocalizedReason NEGATED_TASK_CAN_NOT_BE_MODIFIED = LocalizedReason.like(e(33), "Negated task {{contractTaskId}} can not be modified");

    private ContractExceptions() {
    }

    public static LocalizedResponseStatusException contractDoesNotExist(Integer contractId) {
        return LocalizedResponseStatusException.badRequest(CONTRACT_WITH_ID_DOES_NOT_EXIST.with(pv("contractId", contractId)));
    }

    public static LocalizedResponseStatusException contractHasBenDeleted(Integer contractId) {
        return LocalizedResponseStatusException.badRequest(CONTRACT_HAS_BEEN_DELETED.with(pv("contractId", contractId)));
    }

    static LocalizedResponseStatusException nonUniqueContractName(Integer projectId, String contractName) {
        return LocalizedResponseStatusException.badRequest(NON_UNIQ_CONTRACT_NAME.with(pv("projectId", projectId),pv("contractName", contractName)));
    }

    public static LocalizedResponseStatusException contractIsInImmutableState(ContractDTO contractDTO) {
        return contractIsInImmutableState(contractDTO.getId(), contractDTO.getProjectId(), contractDTO.getContractState());
    }

    public static LocalizedResponseStatusException contractIsInImmutableState(Integer contractId, Integer projectId, ContractState contractState) {
        return LocalizedResponseStatusException.badRequest(CONTRACT_IS_IMMUTABLE.with(
                pv("contractId", contractId),
                pv("projectId", projectId),
                pv("contractState", contractState)
        ));
    }

    static LocalizedResponseStatusException taskDoesNotBelongToSameProjectAsContract(Integer contractId, Integer projectId) {
        return LocalizedResponseStatusException.badRequest(TASK_DOES_NOT_BELONG_TO_SAME_PROJECT_AS_CONTRACT
                .with(pv("contractId", contractId), pv("projectId", projectId)));
    }

    static LocalizedResponseStatusException taskAlreadyExistsInContract(Integer contractId, Integer taskId) {
        return LocalizedResponseStatusException.badRequest(TASK_ALREADY_EXISTS_IN_CONTRACT
                .with(pv("contractId", contractId), pv("taskId", taskId)));
    }

    static LocalizedResponseStatusException taskHasNotTheInitialStatus(Integer taskId, String status) {
        return LocalizedResponseStatusException.badRequest(TASK_HAS_NOT_THE_INITIAL_STATUS.with(pv("taskId", taskId),(pv("status", status))));
    }

    static LocalizedResponseStatusException taskIsAlreadyPartOfAnotherActiveContract(Integer taskId, Integer contractId) {
        return LocalizedResponseStatusException.badRequest(TASK_IS_ALREADY_PART_OF_ANOTHER_ACTIVE_CONTRACT.with(pv("taskId", taskId), pv("contractId", contractId)));
    }

    static LocalizedResponseStatusException duplicateTaskIdsInAddTasksRequest() {
        return LocalizedResponseStatusException.badRequest(DUPLICATE_TASK_IDS_IN_ADD_TASKS_REQUEST.with());
    }

    static LocalizedResponseStatusException cannotAddEmptyCommentOnContract(Integer contractId) {
        return LocalizedResponseStatusException.badRequest(CANNOT_ADD_EMPTY_COMMENT.with(pv("contractId", contractId)));
    }
    
    public static LocalizedResponseStatusException userNotPartOfContractCompanies(Integer userId, Integer contractId) {
        return LocalizedResponseStatusException.badRequest(USER_NOT_PART_OF_CONTRACT_COMPANIES.with(pv("userId", userId), pv("contractId", contractId)));
    }

    public static LocalizedResponseStatusException contractTaskIdDoesNotExist(Integer contractTaskId) {
        return LocalizedResponseStatusException.badRequest(CONTRACT_TASK_WITH_ID_DOES_NOT_EXIST.with(pv("contractTaskId", contractTaskId)));
    }

    static LocalizedResponseStatusException taskHasNotTheRightStatusToRemove(Integer taskId, String status) {
        return LocalizedResponseStatusException.badRequest(TASK_HAS_NOT_THE_RIGHT_STATUS_TO_REMOVE.with(pv("taskId", taskId),(pv("status", status))));
    }

    static LocalizedResponseStatusException currentUserNeitherCustomerNotContractor(Integer projectId) {
        return LocalizedResponseStatusException.badRequest(CURRENT_USER_NEITHER_CUSTOMER_NO_CONTRACTOR.with(pv("projectId", projectId)));
    }

    static LocalizedResponseStatusException taskIsPartOfMultipleNonDeclinedContracts(Integer taskId) {
        return LocalizedResponseStatusException.internalError(TASK_IS_PART_OF_MULTIPLE_NON_DECLINED_CONTRACTS.with(pv("taskId", taskId)));
    }

    public static LocalizedResponseStatusException failToSaveEvent() {
        return LocalizedResponseStatusException.internalError(FAIL_TO_SAVE_CONTRACT_EVENT.with());
    }

    public static LocalizedResponseStatusException contractDoesNotAllowEvent(
            ContractState contractState,
            ContractEvent event
    ) {
        return LocalizedResponseStatusException.businessLogicError(CONTRACT_DOES_NOT_ALLOW_EVENT.with(
            pv("contractState", contractState.name()), pv("event", event.name())
        ));
    }

    public static LocalizedResponseStatusException contractCustomerAlreadySet(Integer contractCustomerId, Integer customerId) {
        return LocalizedResponseStatusException.businessLogicError(CONTRACT_CUSTOMER_ALREADY_SET.with(
                pv("contractCustomerId", contractCustomerId), pv("customerId", customerId)
        ));
    }
    
    public static LocalizedResponseStatusException commentDoesNotExist(Integer commentId) {
        return LocalizedResponseStatusException.badRequest(COMMENT_DOES_NOT_EXIST.with(pv("commentId", commentId)));        
    }

    public static LocalizedResponseStatusException methodCanNotBeUsed() {
        return LocalizedResponseStatusException.internalError(METHOD_CAN_NOT_BE_USED.with());
    }

    public static LocalizedResponseStatusException contractContractorAlreadySet(Integer contractContractorId, Integer contractorId) {
        return LocalizedResponseStatusException.businessLogicError(CONTRACT_CONTRACTOR_ALREADY_SET.with(
                pv("contractContractorId", contractContractorId), pv("contractorId", contractorId)
        ));
    }

    public static LocalizedResponseStatusException contractDoesNotHaveAnyTasks(Integer contractId) {
        return LocalizedResponseStatusException.businessLogicError(CONTRACT_DOES_NOT_HAVE_ANY_TASKS.with(
                pv("contractId", contractId)
        ));
    }

    public static LocalizedResponseStatusException contractHasSomeUnPricedTasks(Integer contractId) {
        return LocalizedResponseStatusException.businessLogicError(CONTRACT_HAS_SOME_UN_PRICED_TASKS.with(
                pv("contractId", contractId)
        ));
    }

    public static LocalizedResponseStatusException contractCustomerNotSet(Integer contractId) {
        return LocalizedResponseStatusException.businessLogicError(CONTRACT_CUSTOMER_NOT_SET.with(
                pv("contractId", contractId)));
    }

    public static LocalizedResponseStatusException projectHasNoCustomerSet(Integer projectId) {
        return LocalizedResponseStatusException.businessLogicError(PROJECT_HAS_NO_CUSTOMER_SET.with(
                pv("projectId", projectId)));
    }

    public static LocalizedResponseStatusException primaryContractBelongsToOtherProject() {
        return LocalizedResponseStatusException.badRequest(PRIMARY_CONTRACT_BELONGS_TO_OTHER_PROJECT.with());
    }

    public static LocalizedResponseStatusException primaryContractIsInIncorrectState(ContractState contractState) {
        return LocalizedResponseStatusException.businessLogicError(PRIMARY_CONTRACT_IN_INCORRECT_STATE.with(
                pv("contractState", contractState)));
    }

    public static LocalizedResponseStatusException primaryContractCanNotBeSubContract() {
        return LocalizedResponseStatusException.badRequest(PRIMARY_CONTRACT_CAN_NOT_BE_SUBCONTRACT.with());
    }
    
    public static LocalizedResponseStatusException contractQuickLinkActionNotSupported(String action) {
        return LocalizedResponseStatusException.badRequest(CONTRACT_QUICK_LINK_ACTION_NOT_SUPPORTED.with(
                pv("action", Optional.ofNullable(action).orElse(StringUtils.EMPTY))));
    }

    public static LocalizedResponseStatusException contractIsNotSubContract(Integer contractId) {
        return LocalizedResponseStatusException.badRequest(CONTRACT_IS_NOT_SUBCONTRACT.with(pv("contractId", contractId)));
    }

    public static LocalizedResponseStatusException contractIsNotPrimaryContract(Integer contractId) {
        return LocalizedResponseStatusException.badRequest(CONTRACT_IS_NOT_PRIMARY_CONTRACT.with(pv("contractId", contractId)));
    }

    public static LocalizedResponseStatusException canNotNegateTask(Integer contractId, Integer contractTaskId) {
        return LocalizedResponseStatusException.badRequest(CAN_NOT_NEGATE_TASK_IN_CONTRACT.with(
                pv("contractId", contractId), pv("contractTaskId", contractTaskId)
        ));
    }

    public static LocalizedResponseStatusException negatedTaskCanNotBeModified(Integer contractTaskId) {
        return LocalizedResponseStatusException.badRequest(NEGATED_TASK_CAN_NOT_BE_MODIFIED.with(
                pv("contractTaskId", contractTaskId)
        ));
    }
}
