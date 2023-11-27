package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.contract.dto.ContractAddTasksRequest;
import com.siryus.swisscon.api.contract.dto.ContractDTO;
import com.siryus.swisscon.api.contract.dto.ContractState;
import com.siryus.swisscon.api.contract.dto.ContractSummaryDTO;
import com.siryus.swisscon.api.contract.dto.ContractTaskDTO;
import com.siryus.swisscon.api.contract.dto.ContractUpdateTaskRequest;
import com.siryus.swisscon.api.contract.repos.ContractTaskEntity;
import com.siryus.swisscon.api.contract.repos.ContractTaskRepository;
import com.siryus.swisscon.api.event.TaskCompletedEvent;
import com.siryus.swisscon.api.event.TaskTimerStartedEvent;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.general.unit.Unit;
import com.siryus.swisscon.api.general.unit.UnitService;
import com.siryus.swisscon.api.tasks.BasicMainTaskService;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import com.siryus.swisscon.api.tasks.dto.TaskStatus;
import com.siryus.swisscon.api.taskworklog.TaskWorkLogWriterService;
import com.siryus.swisscon.api.taskworklog.dto.TaskWorklogRequest;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventType;
import com.siryus.swisscon.api.util.ValidationUtils;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.api.util.validator.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Validated
class ContractTaskService {

    private final ContractTasksReader reader;

    private final ContractBaseService contractBaseService;
    private final ContractTaskRepository contractTaskRepository;
    private final ContractTaskValidator contractTaskValidator;
    private final TaskWorkLogWriterService taskWorkLogWriterService;
    private final SecurityHelper securityHelper;
    private final BasicMainTaskService basicMainTaskService;
    private final ContractEventLogService contractEventLogService;
    private final UnitService unitService;

    @Autowired
    ContractTaskService(
            ContractTasksReader reader,
            ContractBaseService contractBaseService,
            ContractTaskRepository contractTaskRepository,
            ContractTaskValidator contractTaskValidator,
            TaskWorkLogWriterService taskWorkLogWriterService,
            SecurityHelper securityHelper,
            BasicMainTaskService basicMainTaskService,
            ContractEventLogService contractEventLogService,
            UnitService unitService
    ) {
        this.reader = reader;
        this.contractBaseService = contractBaseService;
        this.contractTaskRepository = contractTaskRepository;
        this.contractTaskValidator = contractTaskValidator;
        this.taskWorkLogWriterService = taskWorkLogWriterService;
        this.securityHelper = securityHelper;
        this.basicMainTaskService = basicMainTaskService;
        this.contractEventLogService = contractEventLogService;
        this.unitService = unitService;
    }

    @Transactional
    public List<ContractTaskDTO> addTasks(@Valid ContractAddTasksRequest request) {
        ContractDTO contract = contractBaseService.getContract(request.getContractId());
        List<MainTaskDTO> tasks = basicMainTaskService.getMainTasksById(request.getTaskIds());

        contractTaskValidator.validateTasksToAdd(contract.getId(), contract.getProjectId(), tasks);

        updateTasksStatusForEvent(tasks.stream().map( MainTaskDTO::getId).collect(Collectors.toList()), WorkLogEventType.ADDED_TO_CONTRACT);
        return materializeContractTasks(contract.getPrimaryContractId(), contract.getId(), contract.getProjectId(), tasks);
    }

    @Transactional
    public ContractTaskDTO updateTask(@Valid ContractUpdateTaskRequest request) {
        ContractTaskEntity contractTask = contractTaskRepository.findById(request.getContractTaskId())
                .orElseThrow(() -> ContractExceptions.contractTaskIdDoesNotExist(request.getContractTaskId()));
        MainTaskDTO task = basicMainTaskService.getMainTask(contractTask.getTaskId());

        contractTaskValidator.validateTaskToModify(contractTask, task);

        updateContractTask(contractTask, request);

        return reader.toContractTaskDTO(contractTask);
    }

    @Transactional
    public void removeTask(@Reference(ReferenceType.CONTRACT_TASK) Integer contractTaskId) {
        ContractTaskEntity contractTask = contractTaskRepository.findById(contractTaskId)
                .orElseThrow(() -> ContractExceptions.contractTaskIdDoesNotExist(contractTaskId));
        MainTaskDTO task = basicMainTaskService.getMainTask(contractTask.getTaskId());

        contractTaskValidator.validateTaskToModify(contractTask, task);
        updateTaskStatusForEvent(contractTask.getTaskId(), WorkLogEventType.REMOVED_FROM_CONTRACT);
        contractTaskRepository.disable(contractTask.getId());
    }

    @Transactional
    @EventListener
    public void handleTaskCompletedEvent(TaskCompletedEvent event) {
        List<ContractTaskEntity> contractTasks = contractTaskRepository.findByTask(event.getTaskId());
        calculateContractIdForTask(event.getTaskId(), contractTasks)
                .ifPresent(this::setContractToCompletedIfAllTasksAreCompleted);
    }

    @Transactional
    @EventListener
    public void handleTaskStartedEvent(TaskTimerStartedEvent event) {
        contractTaskRepository
                .findByTask(event.getMainTaskId())
                .stream()
                .filter(ct -> contractEventLogService
                        .getContractState(ct.getContractId())
                        .equals(ContractState.CONTRACT_ACCEPTED))
                .forEach(ct -> contractEventLogService.startContract(ct.getContractId()));
    }

    public List<MainTaskDTO> availableTasksToAdd(@Reference(ReferenceType.CONTRACT) Integer contractId) {
        ContractDTO contract = contractBaseService.getContract(contractId);

        Integer companyId = securityHelper.validateAndGetCurrentUserContractCompany(contractId);
        contractTaskValidator.validateAddableTasks(contract.getId(), contract.getProjectId());

        return findTasksNotInContracts(contract.getProjectId(), companyId);
    }

    public List<ContractTaskDTO> listNegateableTasks(@Reference(ReferenceType.CONTRACT) Integer primaryContractId) {
        validateContractIsPrimaryContract(primaryContractId);
        var allTasks = contractTaskRepository.findByPrimaryContract(primaryContractId);

        Set<Integer> negatedTasks = allTasks.stream()
                .filter(t -> t.getNegatedContractTaskId() != null)
                .map(ContractTaskEntity::getNegatedContractTaskId)
                .collect(Collectors.toSet());

        return allTasks.stream()
                .filter(t -> ! (negatedTasks.contains(t.getId()) || t.getNegatedContractTaskId() != null))
                .map(reader::toContractTaskDTO)
                .filter(t -> t.getTask().getStatus().isNegateable())
                .collect(Collectors.toList());
    }

    @Transactional
    public ContractTaskDTO negateTask(
            @Reference(ReferenceType.CONTRACT) Integer contractId,
            @Reference(ReferenceType.CONTRACT_TASK) Integer contractTaskId
    ) {
        var contract = validateContractIsMutable(contractId);

        validateTaskIsNegateable(contract.getPrimaryContractId(), contractTaskId);

        var contractTask = reader.getTask(contractTaskId);

        return negateContractTasks(contract.getPrimaryContractId(), contract.getId(), contract.getProjectId(), contractTask);
    }

    private void validateContractIsPrimaryContract(Integer primaryContractId) {
        ValidationUtils.throwIfNot(
                contractBaseService.getContract(primaryContractId).isPrimaryContract(),
                () -> ContractExceptions.contractIsNotPrimaryContract(primaryContractId)
        );
    }

    private ContractDTO validateContractIsMutable(Integer contractId) {
        return ValidationUtils.throwIfNot(
                contractBaseService.getContract(contractId),
                ContractDTO::isMutable,
                ContractExceptions::contractIsInImmutableState
        );
    }

    private void validateTaskIsNegateable(Integer contractId, Integer contractTaskId) {
        ValidationUtils.throwIfNot(
                listNegateableTasks(contractId).stream().anyMatch( t -> t.getContractTaskId().equals(contractTaskId)),
                () -> ContractExceptions.canNotNegateTask(contractId, contractTaskId)
        );
    }

    private List<MainTaskDTO> findTasksNotInContracts(Integer projectId, Integer companyId) {
        Set<Integer> allTasksCurrentlyInContractsForProject = findAllTasksCurrentlyInContractsForProject(projectId);

        return basicMainTaskService
                .listMainTasksForProjectAndCompanyOrUnassigned(projectId, companyId)
                .stream()
                .filter(t -> t.getStatus().isAddableToContract())
                .filter(t -> ! allTasksCurrentlyInContractsForProject.contains(t.getId()))
                .collect(Collectors.toList());
    }

    private List<Integer> nonDeclinedContractIdsForProject(Integer projectId) {

        return contractBaseService
                .listAllContractsInProject(projectId)
                .stream()
                .filter(c -> ! contractEventLogService.getContractState(c.getContractId()).isDeclined())
                .map(ContractSummaryDTO::getContractId)
                .collect(Collectors.toList());
    }

    private Set<Integer> findAllTasksCurrentlyInContractsForProject(Integer projectId) {
        List<ContractTaskEntity> allExistingContractTasksInProject = contractTaskRepository
                .findInContracts(nonDeclinedContractIdsForProject(projectId));

        Map<Integer, ContractTaskEntity> allContractTasksCurrentlyInContracts = new HashMap<>();

        allExistingContractTasksInProject.forEach( t -> {
            if (t.getNegatedContractTaskId() == null) {
                allContractTasksCurrentlyInContracts.put(t.getId(), t);
            }
            else {
                allContractTasksCurrentlyInContracts.remove(t.getNegatedContractTaskId());
            }
        });

        return allContractTasksCurrentlyInContracts.values().stream()
                .map(ContractTaskEntity::getTaskId)
                .collect(Collectors.toSet());
    }

    private List<ContractTaskDTO> materializeContractTasks(Integer primaryContractId, Integer contractId, Integer projectId, List<MainTaskDTO> tasks) {
        //TODO as soon as we have contracts with catalogs > we need to set here the unit,
        //TODO price per unit and price according to the company catalog
        BigDecimal amount = BigDecimal.ONE;
        Unit unit = unitService.findBySymbolName("FP");

        return tasks.stream().map(task -> contractTaskRepository
                .save(ContractTaskEntity
                        .builder()
                        .contractId(contractId)
                        .primaryContractId(Optional.ofNullable(primaryContractId).orElse(contractId))
                        .taskId(task.getId())
                        .projectId(projectId)
                        .pricePerUnit(null)
                        .price(null)
                        .amount(amount)
                        .unitId(unit.getId())
                        .build()))
                .map(reader::toContractTaskDTO)
                .collect(Collectors.toList());
    }

    private ContractTaskDTO negateContractTasks(
            Integer primaryContractId,
            Integer contractId,
            Integer projectId,
            ContractTaskDTO contractTask
    ) {
        return reader.toContractTaskDTO(
                contractTaskRepository.save(
                        ContractTaskEntity.builder()
                                .contractId(contractId)
                                .primaryContractId(primaryContractId)
                                .negatedContractTaskId(contractTask.getContractTaskId())
                                .taskId(contractTask.getTask().getId())
                                .projectId(projectId)
                                .pricePerUnit(contractTask.getPricePerUnit())
                                .price(contractTask.getPrice().negate())
                                .amount(contractTask.getAmount().negate())
                                .unitId(contractTask.getUnit().getId())
                            .build()
                )
        );
    }



    private void updateContractTask(ContractTaskEntity contractTask, ContractUpdateTaskRequest request) {
        BigDecimal amount = request.getAmount();
        BigDecimal pricePerUnit = request.getPricePerUnit();

        contractTaskRepository.save(contractTask.toBuilder()
                .amount(amount)
                .pricePerUnit(pricePerUnit)
                .unitId(request.getUnitId())
                .price(calculatePrice(amount, pricePerUnit))
                .build());
    }

    private BigDecimal calculatePrice(BigDecimal amount, BigDecimal pricePerUnit) {
        if (amount != null && pricePerUnit != null) {
            return amount.multiply(pricePerUnit)
                    .setScale(ContractConstants.PRICE_SCALE, ContractConstants.ROUNDING_MODE_FOR_MULTIPLICATION);
        }
        return null;
    }

    private void updateTasksStatusForEvent(List<Integer> taskIds, WorkLogEventType eventType) {
        taskIds.forEach(tId -> updateTaskStatusForEvent(tId, eventType));
    }

    private Optional<Integer> calculateContractIdForTask(Integer taskId, List<ContractTaskEntity> contractTasks) {
        return contractTasks.stream()
                .filter(ct -> !contractEventLogService.getContractState(ct.getContractId()).isDeclined())
                .map(ContractTaskEntity::getContractId)
                .reduce((existing, replacement) -> {
                    throw ContractExceptions.taskIsPartOfMultipleNonDeclinedContracts(taskId);
                });
    }

    private void updateTaskStatusForEvent(Integer taskId, WorkLogEventType eventType) {
        taskWorkLogWriterService.createMainTaskWorkLog(
                eventType,
                securityHelper.currentUserId(),
                TaskWorklogRequest
                        .builder()
                        .mainTaskId(taskId)
                        .build());
    }

    private void setContractToCompletedIfAllTasksAreCompleted(Integer contractId) {
        if(allTasksOfContractAreCompleted(contractId)) {
            contractEventLogService.completeContract(contractId);
        }
    }

    private boolean allTasksOfContractAreCompleted(Integer contractId) {
        return contractTaskRepository
                .findByContract(contractId)
                .stream()
                .allMatch(ct -> basicMainTaskService.getMainTask(ct.getTaskId()).getStatus()
                        .equals(TaskStatus.COMPLETED));
    }

}
