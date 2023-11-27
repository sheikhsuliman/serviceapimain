package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.auth.user.UserService;
import com.siryus.swisscon.api.contract.dto.ContractEvent;
import com.siryus.swisscon.api.contract.dto.ContractEventLogDTO;
import com.siryus.swisscon.api.contract.dto.ContractEventsDTO;
import com.siryus.swisscon.api.contract.dto.ContractState;
import com.siryus.swisscon.api.contract.dto.SendMessageRequest;
import com.siryus.swisscon.api.contract.repos.ContractEntity;
import com.siryus.swisscon.api.contract.repos.ContractEventLogEntity;
import com.siryus.swisscon.api.contract.repos.ContractLogRepository;
import com.siryus.swisscon.api.contract.repos.ContractTaskEntity;
import com.siryus.swisscon.api.event.ContractStateChangeEvent;
import com.siryus.swisscon.api.event.EventPublisher;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.ValidationUtils;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.api.util.validator.Reference;
import com.siryus.swisscon.soa.EventsEmitter;
import com.siryus.swisscon.soa.notification.contract.NotificationEvent;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Service
@Validated
class ContractEventLogService {

    private static final String MESSAGE_TEXT = "messageText";

    private final ContractLogRepository logRepository;

    private final ContractReader reader;
    private final ContractTasksReader tasksReader;

    private final UserService userService;
    private final EventPublisher eventPublisher;
    private final SecurityHelper securityHelper;
    private final EventsEmitter eventsEmitter;

    @Autowired
    ContractEventLogService(
            ContractLogRepository logRepository,
            ContractReader reader,
            ContractTasksReader tasksReader,
            UserService userService,
            EventPublisher eventPublisher,
            SecurityHelper securityHelper,
            EventsEmitter eventsEmitter
    ) {
        this.logRepository = logRepository;
        this.reader = reader;
        this.tasksReader = tasksReader;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
        this.securityHelper = securityHelper;
        this.eventsEmitter = eventsEmitter;
    }

    @PreAuthorize("hasPermission(#contractId, 'CONTRACT', 'CONTRACT_VIEW_DETAILS')")
    List<ContractEventLogDTO> listEvents(Integer contractId) {
        return logRepository.findAllEventsForContract(contractId).stream()
                .map(entity -> ContractEventLogDTO.from(entity, e -> userService.getAuthor(e.getCreatedBy())))
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasPermission(#primaryContractId, 'CONTRACT', 'CONTRACT_VIEW_DETAILS')")
    List<ContractEventLogDTO> listEventsForPrimaryContract(Integer primaryContractId) {
        return logRepository.findAllEventsForPrimaryContract(primaryContractId).stream()
                .map(entity -> ContractEventLogDTO.from(entity, e -> userService.getAuthor(e.getCreatedBy())))
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasPermission(#contractId, 'CONTRACT', 'CONTRACT_VIEW_DETAILS')")
    public ContractEventsDTO listContractEvents(Integer contractId) {
        return ContractEventsDTO.from(reader.getValidContract(contractId), listEvents(contractId));
    }

    ContractEventLogDTO logEvent(Integer contractId, ContractEvent event) {
        return logEvent(getProjectId(contractId), reader.getValidContract(contractId).getPrimaryContractId(), contractId, event);
    }

    @Transactional
    public ContractEventLogDTO logEvent(Integer projectId, Integer primaryContractId, Integer contractId, ContractEvent event) {
        var currentState = validateContractStateAllowsEvent(contractId,event);

        return ContractEventLogDTO.from(
                logRepository.save(
                        ContractEventLogEntity.builder()
                                .projectId(projectId)
                                .primaryContractId(primaryContractId)
                                .contractId(contractId)
                                .event(event)
                                .contractState(event.getToState(currentState))
                        .build()
                ),
                e -> userService.getAuthor(e.getCreatedBy())
        );
    }

    @Transactional
    public void logContractDraftedEvent(Integer projectId, Integer primaryContractId, Integer contractId) {
        logRepository.save(
                ContractEventLogEntity.builder()
                        .projectId(projectId)
                        .primaryContractId(primaryContractId)
                        .contractId(contractId)
                        .event(ContractEvent.DRAFT_CREATED)
                        .contractState(ContractState.CONTRACT_DRAFT)
                .build()
        );
    }

    ContractState getContractState(@Reference(ReferenceType.CONTRACT) Integer contractId) {
        return logRepository.findTopLogEntry(contractId)
                .orElseThrow(() -> ContractExceptions.contractDoesNotExist(contractId)).getContractState();
    }

    LocalDateTime getSignDate(@Reference(ReferenceType.CONTRACT) Integer contractId) {
        return logRepository.findAllEventsForContract(contractId).stream()
                .filter( e -> e.getEvent().equals(ContractEvent.OFFER_ACCEPTED))
                .findFirst()
                .map(ContractEventLogEntity::getCreatedDate)
                .orElse(null);
    }

    private Integer getProjectId(@Reference(ReferenceType.CONTRACT) Integer contractId) {
        return logRepository.findAllEventsForContract(contractId).stream()
                .findFirst()
                .map(ContractEventLogEntity::getProjectId)
                .orElseThrow(() -> ContractExceptions.contractDoesNotExist(contractId));
    }

    @PreAuthorize("hasPermission(#contractId, 'CONTRACT', 'CONTRACT_SEND_OFFER')")
    @Transactional
    public ContractEventLogDTO sendOffer(
            @Reference(ReferenceType.CONTRACT) Integer contractId,
            @Valid SendMessageRequest request
    ) {
        return notifyCompanyMembersWithPermissionAboutEvent(
                logEvent(contractId, ContractEvent.OFFER_MADE),
                calculateCustomerCompanyId(contractId, request.getRecipientCompanyId()),
                request.getMessageText(),
                this::validateTasksArePresentAndPriced
        );
    }

    @PreAuthorize("hasPermission(#contractId, 'CONTRACT', 'CONTRACT_ACCEPT_DECLINE_OFFER')")
    @Transactional
    public ContractEventLogDTO acceptOffer(Integer contractId) {
        return acceptOrDeclineOffer(contractId, ContractEvent.OFFER_ACCEPTED);
    }

    @PreAuthorize("hasPermission(#contractId, 'CONTRACT', 'CONTRACT_SELF_ACCEPT_OFFER')")
    @Transactional
    public ContractEventLogDTO selfAcceptOffer(
            @Reference(ReferenceType.CONTRACT) Integer contractId,
            @Valid SendMessageRequest request
    ) {
        return notifyCompanyMembersWithPermissionAboutEvent(
                logEvent(contractId, ContractEvent.OFFER_SELF_ACCEPTED),
                request.getRecipientCompanyId(),
                request.getMessageText(),
                this::validateTasksArePresentAndPriced
        );
    }

    @PreAuthorize("hasPermission(#contractId, 'CONTRACT', 'CONTRACT_ACCEPT_DECLINE_OFFER')")
    @Transactional
    public ContractEventLogDTO declineOffer(Integer contractId) {
        return acceptOrDeclineOffer(contractId, ContractEvent.OFFER_DECLINED);
    }

    @PreAuthorize("hasPermission(#contractId, 'CONTRACT', 'CONTRACT_SEND_INVITATION')")
    @Transactional
    public ContractEventLogDTO sendInvitation(
            @Reference(ReferenceType.CONTRACT) Integer contractId,
            @Valid SendMessageRequest request
    ) {
        return notifyCompanyMembersWithPermissionAboutEvent(
                logEvent(contractId, ContractEvent.INVITATION_SENT),
                request.getRecipientCompanyId(),
                request.getMessageText(),
                this::validateTasksArePresent
        );
    }

    @PreAuthorize("hasPermission(#contractId, 'CONTRACT', 'CONTRACT_ACCEPT_DECLINE_INVITATION')")
    @Transactional
    public ContractEventLogDTO acceptInvitation(Integer contractId) {
        return acceptOrDeclineInvitation(contractId, ContractEvent.INVITATION_ACCEPTED);
    }

    @PreAuthorize("hasPermission(#contractId, 'CONTRACT', 'CONTRACT_ACCEPT_DECLINE_INVITATION')")
    @Transactional
    public ContractEventLogDTO declineInvitation(Integer contractId) {
        return acceptOrDeclineInvitation(contractId, ContractEvent.INVITATION_DECLINED);
    }

    @Transactional
    public ContractEventLogDTO completeContract(Integer contractId) {
        return startOrCompleteContract(contractId, ContractEvent.CONTRACT_COMPLETED);
    }

    @Transactional
    public ContractEventLogDTO startContract(Integer contractId) {
        return startOrCompleteContract(contractId, ContractEvent.WORK_STARTED);
    }

    private ContractEventLogDTO startOrCompleteContract(Integer contractId, ContractEvent event) {
        var contract = reader.getValidContract(contractId);

        return notifyCompanyMembersWithPermissionAboutEvent(
                logEvent(contractId, event),
                contract.getCustomerId(),
                null,
                this::validateTasksArePresent
        );
    }

    private Integer calculateCustomerCompanyId(Integer contractId, Integer customerCompanyId) {
        var contractCustomerCompanyId = Optional.ofNullable(reader.getValidContract(contractId)).map(ContractEntity::getCustomerId).orElse(customerCompanyId);

        if (contractCustomerCompanyId == null) {
            throw ContractExceptions.contractCustomerNotSet(contractId);
        }

        if ( customerCompanyId != null && (!Objects.equals(contractCustomerCompanyId, customerCompanyId))) {
            throw ContractExceptions.contractCustomerAlreadySet(contractCustomerCompanyId, customerCompanyId);
        }

        return contractCustomerCompanyId;
    }

    private List<ContractTaskEntity> validateTasksArePresentAndPriced(Integer contractId) {
        return ValidationUtils.throwIf(
            validateTasksArePresent(contractId),
            l -> l.stream().anyMatch( ContractEventLogService::taskIsNotPriced ),
            l -> ContractExceptions.contractHasSomeUnPricedTasks(contractId)
        );
    }

    private static boolean taskIsNotPriced(ContractTaskEntity taskEntity) {
        return  taskEntity.getPrice() == null;
    }

    private List<ContractTaskEntity> validateTasksArePresent(Integer contractId) {
        return ValidationUtils.throwIfEmpty(
                reader.listContractTasks(contractId),
                () -> ContractExceptions.contractDoesNotHaveAnyTasks(contractId)
        );
    }

    private ContractEventLogDTO notifyCompanyMembersWithPermissionAboutEvent(
            ContractEventLogDTO eventLogDTO,
            Integer recipientCompanyId,
            String message,
            ContractTaskListSupplier validatedContractTasks
    ) {
        var contract = reader.getValidContract(eventLogDTO.getContractId());

        var contractTasks = validatedContractTasks.apply(contract.getId());

        return publishEvent(
                eventLogDTO,
                event -> event.toBuilder()
                        .eventInitiatorId(securityHelper.currentUserId())
                        .recipientCompanyId(recipientCompanyId)
                        .variables(Map.of(
                                MESSAGE_TEXT, Optional.ofNullable(message).orElse(StringUtils.EMPTY)
                        ))
                        .contractTaskIds(addedButNotNegatedTaskIds(contractTasks))
                        .negatedTaskIds(negatedButNotAddedTaskIds(contractTasks))
                .build()
        );
    }

    private ContractEventLogDTO acceptOrDeclineOffer(Integer contractId, ContractEvent acceptOrDecline) {
        ValidationUtils.throwIfNotOneOf(
                acceptOrDecline,
                Arrays.asList(ContractEvent.OFFER_ACCEPTED, ContractEvent.OFFER_DECLINED),
                ContractExceptions::methodCanNotBeUsed
        );

        var contract = reader.getValidContract(contractId);

        var contractTasks = reader.listContractTasks(contractId);

        return publishEvent(
                logEvent(contractId, acceptOrDecline),
                event -> event.toBuilder()
                        .eventInitiatorId(securityHelper.currentUserId())
                        .recipientCompanyId(contract.getContractorId())
                        .variables(Map.of())
                        .contractTaskIds(addedButNotNegatedTaskIds(contractTasks))
                        .negatedTaskIds(negatedButNotAddedTaskIds(contractTasks))
                .build()
        );
    }


    private Map<Integer, ContractTaskEntity> allAddedTasks(List<ContractTaskEntity> allTasks) {
        return allTasks.stream()
                .filter(t -> t.getNegatedContractTaskId() == null)
                .collect(Collectors.toMap(ContractTaskEntity::getId, t -> t ))
                ;
    }

    private Map<Integer, ContractTaskEntity> allNegatedTasks(List<ContractTaskEntity> allTasks) {
        return allTasks.stream()
                .filter(t -> t.getNegatedContractTaskId() != null)
                .collect(Collectors.toMap(ContractTaskEntity::getNegatedContractTaskId, t -> t ))
                ;
    }

    private List<Integer> addedButNotNegatedTaskIds(List<ContractTaskEntity> allTasks) {
        var allAddedTasks = allAddedTasks(allTasks);
        var allNegatedTasks = allNegatedTasks(allTasks);

        return allAddedTasks.values().stream()                                          // added
                .filter( t -> ! allNegatedTasks.containsKey(t.getId()))     // not negated
                .map(ContractTaskEntity::getTaskId)
                .collect(Collectors.toList());
    }

    private List<Integer> negatedButNotAddedTaskIds(List<ContractTaskEntity> allTasks) {
        var allAddedTasks = allAddedTasks(allTasks);
        var allNegatedTasks = allNegatedTasks(allTasks);

        return allNegatedTasks.values().stream()                                          // negated
                .filter( t -> ! allAddedTasks.containsKey(t.getNegatedContractTaskId()))  // not added
                .map(ContractTaskEntity::getTaskId)
                .collect(Collectors.toList());
    }

    private ContractEventLogDTO acceptOrDeclineInvitation(Integer contractId, ContractEvent acceptOrDecline) {
        ValidationUtils.throwIfNotOneOf(
                acceptOrDecline,
                Arrays.asList(ContractEvent.INVITATION_ACCEPTED, ContractEvent.INVITATION_DECLINED),
                ContractExceptions::methodCanNotBeUsed
        );

        var contract = reader.getValidContract(contractId);

        return publishEvent(
                logEvent(contractId, acceptOrDecline),
                event -> event.toBuilder()
                        .eventInitiatorId(securityHelper.currentUserId())
                        .recipientCompanyId(contract.getCustomerId())
                        .build()
        );
    }

    private ContractState validateContractStateAllowsEvent(Integer contractId, ContractEvent event) {
        ContractState contractState = getContractState(contractId);

        ValidationUtils.throwIfNot(event.validFromState(contractState),
                                   () -> ContractExceptions.contractDoesNotAllowEvent(contractState, event));

        return contractState;
    }

    private ContractEventLogDTO publishEvent(ContractEventLogDTO dto, UnaryOperator<ContractStateChangeEvent> addMetadata) {
        var contractStateChangeEvent = addMetadata.apply(ContractStateChangeEvent.from(dto));
        var notificationEventType = dto.getEvent().asNotificationEventType();
        if (notificationEventType != null) {
            eventsEmitter.emitNotification(
                    NotificationEvent.builder()
                            .companyId(null)
                            .projectId(contractStateChangeEvent.getProjectId())
                            .notificationType(contractStateChangeEvent.getEvent().asNotificationEventType())
                            .referenceId(contractStateChangeEvent.getContractId())
                            .senderId(contractStateChangeEvent.getEventInitiatorId())
                            .subjectId(contractStateChangeEvent.getRecipientCompanyId())
                            .variables(contractStateChangeEvent.getVariables())
                            .build()
            );
        }
        eventPublisher.publishEvent(contractStateChangeEvent);
        return dto;
    }

    private interface ContractTaskListSupplier extends Function<Integer, List<ContractTaskEntity>> {

    }
}
