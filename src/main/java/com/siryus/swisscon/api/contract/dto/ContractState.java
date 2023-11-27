package com.siryus.swisscon.api.contract.dto;

public enum ContractState {
    CONTRACT_DRAFT(true, false, true),
    CONTRACT_INVITATION_SENT(false, false, false),
    CONTRACT_OFFER_MADE(false, false, false),
    CONTRACT_INVITATION_ACCEPTED(true, false, false),
    CONTRACT_INVITATION_DECLINED(false, true, true),
    CONTRACT_ACCEPTED(false, false, false),
    CONTRACT_DECLINED(false, true, true),
    CONTRACT_IN_PROGRESS(false, false, false),
    CONTRACT_COMPLETED(false, false, false);

    private final boolean mutable;
    private final boolean declined;
    private final boolean customerReassignable;

    ContractState(boolean mutable, boolean declined, boolean customerReassignable) {
        this.mutable = mutable;
        this.declined = declined;
        this.customerReassignable = customerReassignable;
    }

    public boolean isMutable() {
        return mutable;
    }

    public boolean isDeclined() {
        return declined;
    }

    public boolean isCustomerReassignable() { return customerReassignable; }
}
