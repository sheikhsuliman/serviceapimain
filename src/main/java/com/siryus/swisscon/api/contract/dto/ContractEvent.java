package com.siryus.swisscon.api.contract.dto;

import com.siryus.swisscon.soa.notification.contract.NotificationType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ContractEvent {
    DRAFT_CREATED(null, ContractState.CONTRACT_DRAFT),
    INVITATION_SENT(ContractState.CONTRACT_DRAFT, ContractState.CONTRACT_INVITATION_SENT),
    OFFER_MADE(Arrays.asList(
            Transition.t(ContractState.CONTRACT_DRAFT, ContractState.CONTRACT_OFFER_MADE),
            Transition.t(ContractState.CONTRACT_INVITATION_ACCEPTED, ContractState.CONTRACT_OFFER_MADE)
    )),
    INVITATION_DECLINED(ContractState.CONTRACT_INVITATION_SENT, ContractState.CONTRACT_INVITATION_DECLINED),
    INVITATION_ACCEPTED(ContractState.CONTRACT_INVITATION_SENT, ContractState.CONTRACT_INVITATION_ACCEPTED),
    OFFER_DECLINED(ContractState.CONTRACT_OFFER_MADE, ContractState.CONTRACT_DECLINED),
    OFFER_ACCEPTED(ContractState.CONTRACT_OFFER_MADE, ContractState.CONTRACT_ACCEPTED),
    OFFER_SELF_ACCEPTED(ContractState.CONTRACT_DRAFT, ContractState.CONTRACT_ACCEPTED),
    WORK_STARTED(ContractState.CONTRACT_ACCEPTED, ContractState.CONTRACT_IN_PROGRESS),
    CONTRACT_COMPLETED(ContractState.CONTRACT_IN_PROGRESS, ContractState.CONTRACT_COMPLETED);

    private final List<Transition> transitions =  new ArrayList<>();

    ContractEvent(ContractState fromState, ContractState toState) {
        this(Collections.singletonList(new Transition(fromState, toState)));
    }

    ContractEvent(List<Transition> transitions) {
        this.transitions.addAll(transitions);
    }

    public boolean validFromState(ContractState fromState) {
        return transitions.stream().anyMatch( t -> t.getFromState().equals(fromState));
    }

    public ContractState getToState(ContractState fromState) {
        return transitions.stream().filter(t -> t.getFromState().equals(fromState))
                .findFirst()
                .map(Transition::getToState)
                .orElse(fromState);
    }
    
    public NotificationType asNotificationEventType() {
        switch (this) {
            case INVITATION_SENT: return NotificationType.CONTRACT_INVITATION_SENT;
            case INVITATION_ACCEPTED: return NotificationType.CONTRACT_INVITATION_ACCEPTED;
            case INVITATION_DECLINED: return NotificationType.CONTRACT_INVITATION_DECLINED;

            case OFFER_MADE: return NotificationType.CONTRACT_OFFER_MADE;
            case OFFER_ACCEPTED: return NotificationType.CONTRACT_OFFER_ACCEPTED;
            case OFFER_DECLINED: return NotificationType.CONTRACT_OFFER_DECLINED;

            default: return null;
        }
    }
}

class Transition {
    private final ContractState fromState;
    private final ContractState toState;

    public Transition(ContractState fromState, ContractState toState) {
        this.fromState = fromState;
        this.toState = toState;
    }

    public static Transition t(ContractState fromState, ContractState toState) {
        return new Transition(fromState, toState);
    }

    public ContractState getFromState() {
        return fromState;
    }

    public ContractState getToState() {
        return toState;
    }
}