package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.auth.permission.PermissionName;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.company.CompanyService;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleService;
import com.siryus.swisscon.api.contract.dto.ContractDTO;
import com.siryus.swisscon.api.contract.dto.ContractOrderBy;
import com.siryus.swisscon.api.contract.dto.ContractState;
import com.siryus.swisscon.api.contract.dto.ContractSummaryDTO;
import com.siryus.swisscon.api.contract.dto.ContractTaskDTO;
import com.siryus.swisscon.api.contract.dto.CreateContractRequest;
import com.siryus.swisscon.api.contract.dto.ListContractsRequest;
import com.siryus.swisscon.api.contract.dto.UpdateContractRequest;
import com.siryus.swisscon.api.contract.repos.ContractEntity;
import com.siryus.swisscon.api.contract.repos.ContractRepository;
import com.siryus.swisscon.api.event.ContractStateChangeEvent;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.mediawidget.MediaWidgetService;
import com.siryus.swisscon.api.tasks.dto.TaskStatus;
import com.siryus.swisscon.api.util.ValidationUtils;
import com.siryus.swisscon.api.util.counter.ReferenceBasedCounterFactory;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.api.util.validator.Reference;
import com.siryus.swisscon.soa.EventsEmitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Validated
class ContractBaseService implements ContractPublicService {
    private final ContractRepository contractRepository;
    private final ReferenceBasedCounterFactory counterFactory;

    private final ContractReader reader;
    private final ContractTasksReader tasksReader;
    private final ContractEventLogService eventLogService;

    private final CompanyService companyService;
    private final CompanyUserRoleService companyUserRoleService;
    private final MediaWidgetService mediaWidgetService;

    private final SecurityHelper securityHelper;

    private final EventsEmitter eventsEmitter;

    @Autowired
    public ContractBaseService(
            ContractRepository contractRepository,
            ReferenceBasedCounterFactory counterFactory,
            ContractReader reader,
            ContractTasksReader tasksReader,
            ContractEventLogService eventLogService,
            CompanyService companyService,
            CompanyUserRoleService companyUserRoleService,
            MediaWidgetService mediaWidgetService,
            SecurityHelper securityHelper,
            EventsEmitter eventsEmitter) {
        this.contractRepository = contractRepository;
        this.counterFactory = counterFactory;
        this.reader = reader;
        this.tasksReader = tasksReader;
        this.eventLogService = eventLogService;
        this.companyService = companyService;
        this.companyUserRoleService = companyUserRoleService;
        this.mediaWidgetService = mediaWidgetService;
        this.securityHelper = securityHelper;
        this.eventsEmitter = eventsEmitter;
    }

    @Override
    public ContractDTO getContract(@Reference(ReferenceType.CONTRACT) Integer contractId) {
        return ContractDTO.from(
                reader.getValidContract(contractId),
                this::calculateContractorSigners,
                this::calculateCustomerSigners,
                e -> isContractMutable(e.getId()),
                e -> eventLogService.getContractState(e.getId())
        );
    }

    @Override
    public List<ContractSummaryDTO> listPrimaryContracts(@Valid ListContractsRequest request) {
        var restrictedRequest = restrictRequest(request);

        return contractRepository.findPrimaryContractsInProject(restrictedRequest.getProjectId()).stream()
                .filter( e -> nullOrEqual(
                        restrictedRequest.getContractorIdFilter(),
                        Optional.ofNullable(e.getContractorId()).orElse(null)
                ))
                .sorted(createComparator(restrictedRequest))
                .map((ContractEntity contractEntity) -> toSummaryDTO(contractEntity, listPrimaryContractTasks(contractEntity.getId())))
                .filter(d -> nullOrEqual(restrictedRequest.getContractStateFilter(), d.getContractState()))
                .collect(Collectors.toList());
    }

    List<ContractSummaryDTO> listAllContractsInProject(@Reference(ReferenceType.PROJECT) Integer projectId) {
        return contractRepository.findAllContractsInProject(projectId).stream()
                .map((ContractEntity contractEntity) -> toSummaryDTO(contractEntity, listPrimaryContractTasks(contractEntity.getId())))
                .collect(Collectors.toList());
    }

    public List<ContractSummaryDTO> listPrimaryContractExtensions(@Reference(ReferenceType.CONTRACT) Integer primaryContractId) {
        return contractRepository.findAllPrimaryContractExtensions(primaryContractId).stream()
                .map((ContractEntity contractEntity) -> toSummaryDTO(contractEntity, listContractTasks(contractEntity.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public boolean projectCustomerIsReassignable(Integer projectId) {
        return contractRepository.findPrimaryContractsInProject(projectId)
                .stream()
                .allMatch(c-> eventLogService.getContractState(c.getId()).isCustomerReassignable());
    }

    @Transactional
    public ContractDTO createContract(@Valid CreateContractRequest request) {
        validateNameIsUniq(request.getProjectId(), request.getName());
        var primaryContract = validatePrimaryContract(request);

        return ContractDTO.from(
                finalizeContractCreation(materializeContract(createContractEntity(request, primaryContract))),
                this::calculateContractorSigners,
                this::calculateCustomerSigners,
                e -> true,
                e -> ContractState.CONTRACT_DRAFT
        );
    }

    @Transactional
    public ContractDTO updateContract(
            @Reference(ReferenceType.CONTRACT) Integer contractId,
            @Valid UpdateContractRequest request
    ) {
        var existingContract = reader.getValidContract(contractId);

        validateNameIsUniqOrSame(existingContract.getProjectId(), request.getName(), existingContract);

        validateContractIsMutable(existingContract);

        return ContractDTO.from(
                materializeContract(updateContractEntity(existingContract, request)),
                this::calculateContractorSigners,
                this::calculateCustomerSigners,
                e -> true,
                e -> eventLogService.getContractState(e.getId())
        );
    }

    @Override
    @Transactional
    public void updateProjectContractsCustomers(
            @Reference(ReferenceType.PROJECT) Integer projectId,
            @Reference(ReferenceType.COMPANY) Integer customerCompanyId
    ) {
        contractRepository.findPrimaryContractsInProject(projectId).stream()
            .filter( c -> eventLogService.getContractState(c.getId()).equals(ContractState.CONTRACT_DRAFT))
            .forEach( c -> c.setCustomerId(customerCompanyId));
    }

    private List<TeamUserDTO> calculateCustomerSigners(ContractEntity contractEntity) {
        if (contractEntity.getCustomerId() == null) {
            return Collections.emptyList();
        }
        return securityHelper.getProjectMembersFromCompanyWithPermissions(
                contractEntity.getProjectId(),
                contractEntity.getCustomerId(),
                Collections.singletonList(PermissionName.CONTRACT_ACCEPT_DECLINE_OFFER)
        );
    }

    private List<TeamUserDTO> calculateContractorSigners(ContractEntity contractEntity) {
        if (contractEntity.getContractorId() == null) {
            return Collections.emptyList();
        }
        return securityHelper.getProjectMembersFromCompanyWithPermissions(
                contractEntity.getProjectId(),
                contractEntity.getContractorId(),
                Collections.singletonList(PermissionName.CONTRACT_ACCEPT_DECLINE_INVITATION)
        );
    }

    private ListContractsRequest restrictRequest(ListContractsRequest request) {
        if (isCurrentUserCustomer(request.getProjectId())) {
            return request;
        } else {
            return request.toBuilder()
                    .contractorIdFilter(
                            Optional.ofNullable(calculateContractor(null, request.getProjectId()))
                                    .orElseThrow(
                                            () -> ContractExceptions.currentUserNeitherCustomerNotContractor(request.getProjectId())
                                    )
                    )
                    .build();
        }
    }

    private ContractSummaryDTO toSummaryDTO(ContractEntity contractEntity, List<ContractTaskDTO> tasks) {
        return ContractSummaryDTO.from(
                contractEntity,
                eventLogService.getContractState(contractEntity.getId()),
                countTotalTasks(tasks),
                countCompletedTasks(tasks),
                countTotalAmount(tasks),
                getDateSigned(contractEntity),
                doesContractHaveExtensions(contractEntity)
        );
    }

    private boolean doesContractHaveExtensions(ContractEntity contractEntity) {
        if (!contractEntity.isPrimaryContract()) {
            return false;
        }

        return contractRepository.findAllPrimaryContractExtensions(contractEntity.getId()).size() > 1;
    }

    private Integer countTotalTasks(List<ContractTaskDTO> tasks) {
        return tasks.stream()
                .map( t -> t.getNegatedContractTaskId() == null ? 1 : -1)
                .reduce(Integer::sum)
                .orElse(0);
    }

    private BigDecimal countTotalAmount(List<ContractTaskDTO> tasks) {
        return tasks.stream()
                .map(t -> Optional.ofNullable(t.getPrice()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private Integer countCompletedTasks(List<ContractTaskDTO> tasks) {
        return Math.toIntExact(
            tasks.stream().filter(this::isTaskComplete).count()
        );
    }

    private boolean isTaskComplete(ContractTaskDTO contractTask) {
        return contractTask.getTask().getStatus().equals(TaskStatus.COMPLETED);
    }

    private List<ContractTaskDTO> listContractTasks(Integer contractId) {
        return tasksReader.listTasks(contractId);
    }

    private List<ContractTaskDTO> listPrimaryContractTasks(Integer primaryContractId) {
        return tasksReader.listTasksByPrimaryContract(primaryContractId);
    }

    private <T> boolean nullOrEqual(T expected, T actual) {
        return expected == null || Objects.equals(expected,actual);
    }

    private void validateNameIsUniqOrSame(Integer projectId, String newContractName, ContractEntity existingContract) {
        contractRepository.findContractByName(
                projectId, newContractName
        ).ifPresent( e -> {
            if (!e.getId().equals(existingContract.getId())) {
                throw ContractExceptions.nonUniqueContractName(projectId, newContractName);
            }
        });
    }

    private void validateNameIsUniq(Integer projectId, String contractName) {
        contractRepository.findContractByName(projectId, contractName)
                .ifPresent( e -> { throw ContractExceptions.nonUniqueContractName(projectId, contractName);});
    }

    private ContractEntity validatePrimaryContract(CreateContractRequest request) {
        if (request.getPrimaryContractId() == null) {
            return null;
        }

        var primaryContract = reader.getValidContract(request.getPrimaryContractId());
        if (!primaryContract.isPrimaryContract()) {
            throw ContractExceptions.primaryContractCanNotBeSubContract();
        }

        if (!primaryContract.getProjectId().equals(request.getProjectId())) {
            throw ContractExceptions.primaryContractBelongsToOtherProject();
        }

        Stream.concat(
                Stream.of(primaryContract.getId()),
                reader.getValidContractExtensions(primaryContract.getId()).stream().map(ContractEntity::getId)
        ).map(this::getContractState)
            .filter(s -> ! (s.equals(ContractState.CONTRACT_ACCEPTED) || s.equals(ContractState.CONTRACT_IN_PROGRESS)))
            .findFirst()
            .ifPresent((s) -> { throw ContractExceptions.primaryContractIsInIncorrectState(s); });

        return primaryContract;
    }

    private boolean isContractMutable(Integer contractId) {
        return eventLogService.getContractState(contractId).isMutable();
    }

    private void validateContractIsMutable(ContractEntity existingContract) {
        var contractState = eventLogService.getContractState(existingContract.getId());
        ValidationUtils.throwIfNot(
                contractState.isMutable(),
                () -> ContractExceptions.contractIsInImmutableState(existingContract.getId(), existingContract.getProjectId(),contractState)
        );
    }

    private ContractEntity finalizeContractCreation(ContractEntity entity) {
        mediaWidgetService.createSystemFolders(ReferenceType.CONTRACT, entity.getId());
        eventLogService.logContractDraftedEvent(entity.getProjectId(), entity.getPrimaryContractId(), entity.getId());

        return entity;
    }

    private ContractEntity updateContractEntity(ContractEntity existingContract, UpdateContractRequest request) {
        existingContract.setName(request.getName());
        existingContract.setDescription(request.getDescription());
        existingContract.setDeadline(request.getDeadline());
        eventsEmitter.emitCacheUpdate(ReferenceType.CONTRACT, existingContract.getId());

        return existingContract;
    }

    private ContractEntity createContractEntity(CreateContractRequest request, ContractEntity primaryContract) {
        return ContractEntity.builder()
                .projectId(request.getProjectId())
                .primaryContractId(request.getPrimaryContractId())
                .contractNumber(nextContractNumber(request.getProjectId()))
                .name(request.getName())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .contractorId(calculateContractor(primaryContract, request.getProjectId()))
                .customerId(calculateCustomer(primaryContract, request.getProjectId()))
            .build();
    }

    private boolean isCurrentUserCustomer(Integer projectId) {
        return securityHelper.checkIfUserHasProjectPermission(securityHelper.currentUserId(), projectId, PermissionName.CONTRACT_SEND_INVITATION);
    }

    private Integer calculateCustomer(ContractEntity primaryContract, Integer projectId) {
        if (primaryContract != null) {
            return primaryContract.getCustomerId();
        }

        return ValidationUtils.throwIfNull(
                securityHelper.getProjectCustomerCompanyId(projectId),
                () -> ContractExceptions.projectHasNoCustomerSet(projectId)
        );
    }

    private Integer calculateContractor(ContractEntity primaryContract, Integer projectId) {
        if (primaryContract != null) {
            return primaryContract.getContractorId();
        }

        else if(!isCurrentUserCustomer(projectId)) {
            return companyUserRoleService.findCompanyRoleByUser(securityHelper.currentUserId()).getCompany().getId();
        }
        else {
            return null;
        }
    }

    private ContractEntity materializeContract(ContractEntity contractEntity) {
        return contractRepository.save(contractEntity);
    }

    private Integer nextContractNumber(Integer projectId) {
        String CONTRACT_COUNTER = "CONTRACT-COUNTER";
        return counterFactory.counter(ReferenceType.PROJECT, projectId, CONTRACT_COUNTER).getNextValue();
    }

    private Comparator<ContractEntity> createComparator(ListContractsRequest request) {
        return ascendingOrNotComparator(request.isOrderAscending(), orderByComparator(request.getOrderBy()));
    }

    private Comparator<ContractEntity> orderByComparator(ContractOrderBy orderBy) {
        if (orderBy == null) {
            return Comparator.comparing(ContractEntity::getContractNumber);
        }
        switch (orderBy) {
            case CONTRACTOR: return Comparator.comparing(this::getContractorName);
            case CUSTOMER: return Comparator.comparing(this::getCustomerName);
            case DATE_SIGNED: return Comparator.comparing(this::getDateSigned);
            case PROGRESS: return Comparator.comparing(this::getContractProgress);
            case NUMBER_OF_TASKS: return Comparator.comparing(this::getNumberOfTasks);
            case TOTAL_AMOUNT: return Comparator.comparing(this::getTotalAmount);
            case STATE: return Comparator.comparing(this::getContractState);
            case CONTRACT_NUMBER:
            default:
                return Comparator.comparing(ContractEntity::getContractNumber);
        }
    }

    private Comparator<ContractEntity> ascendingOrNotComparator(boolean isOrderAscending, Comparator<ContractEntity> orderByComparator) {
        return isOrderAscending ? orderByComparator : (o1, o2) -> orderByComparator.compare(o2, o1);
    }

    private String getContractorName(ContractEntity contractEntity) {
        return Optional.ofNullable(contractEntity.getContractorId())
                .map(companyService::getValidCompany)
                .map(Company::getName)
                .orElse("");
    }

    private String getCustomerName(ContractEntity contractEntity) {
        return Optional.ofNullable(contractEntity.getCustomerId())
                .map(companyService::getValidCompany)
                .map(Company::getName)
                .orElse("");
    }

    private LocalDateTime getDateSigned(ContractEntity contractEntity) {
        return eventLogService.getSignDate(contractEntity.getId());
    }

    private Integer getContractProgress(ContractEntity contractEntity) {
        List<ContractTaskDTO> tasks = listContractTasks(contractEntity.getId());

        return tasks.isEmpty() ? 0 : countCompletedTasks(tasks) * 100 / tasks.size();
    }

    private Integer getNumberOfTasks(ContractEntity contractEntity) {
        return listContractTasks(contractEntity.getId()).size();
    }

    private BigDecimal getTotalAmount(ContractEntity contractEntity) {
        List<ContractTaskDTO> tasks = listContractTasks(contractEntity.getId());
        return countTotalAmount(tasks);
    }

    private ContractState getContractState(ContractEntity contractEntity) {
        return getContractState(contractEntity.getId());
    }

    private ContractState getContractState(Integer contractId) {
        return eventLogService.getContractState(contractId);
    }

    // Events

    @EventListener
    @Transactional
    public void onApplicationEvent(ContractStateChangeEvent event) {
        switch(event.getEvent()) {
            case OFFER_SELF_ACCEPTED:
            case OFFER_MADE:
                setCustomerId(event.getContractId(), event.getRecipientCompanyId()); break;
            case INVITATION_SENT:
                setContractorId(event.getContractId(), event.getRecipientCompanyId()); break;
            default:
                // do nothing
        }
    }

    private void setCustomerId(Integer contractId, Integer customerId) {
        var existingContract = reader.getValidContract(contractId);
        if (existingContract.getCustomerId() == null) {
            existingContract.setCustomerId(customerId);
        }
        else if (! Objects.equals(existingContract.getCustomerId(), customerId)) {
            throw ContractExceptions.contractCustomerAlreadySet(existingContract.getCustomerId(), customerId);
        }
    }

    private void setContractorId(Integer contractId, Integer contractorId) {
        var existingContract = reader.getValidContract(contractId);
        if (existingContract.getContractorId() == null) {
            existingContract.setContractorId(contractorId);
        }
        else if (! Objects.equals(existingContract.getContractorId(), contractorId)) {
            throw ContractExceptions.contractCustomerAlreadySet(existingContract.getContractorId(), contractorId);
        }
    }
}
