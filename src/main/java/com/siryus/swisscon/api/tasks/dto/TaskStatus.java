package com.siryus.swisscon.api.tasks.dto;

public enum TaskStatus {
    DRAFT(true, true, false, false),
    OPEN(true, false, false, false),
    ASSIGNED(true, false, false, false),
    ACCEPTED(false, false, true, true),
    IN_PROGRESS,
    PAUSED,
    IN_CONTRACTOR_REVIEW,
    IN_REVIEW,
    REJECTED,
    COMPLETED,
    SUB_TASK_COMPLETED,

    ANY;

    /**
     * If true, task can be added to mutable contract
     */
    private final boolean addableToContract;
    /**
     * If true, task can be deleted from mutable contract
     */
    private final boolean removableFromContract;
    /**
     * If true, task can not be mutated
     */
    private final boolean immutable;
    /**
     * If true, task can be negated via sub-contract
     */
    private final boolean negateable;

    TaskStatus(boolean addableToContract, boolean removableFromContract, boolean immutable, boolean negateable) {
        this.addableToContract = addableToContract;
        this.removableFromContract = removableFromContract;
        this.immutable = immutable;
        this.negateable = negateable;
    }

    TaskStatus() {
        this(false, false, true, false);
    }

    public boolean isAddableToContract() {
        return addableToContract;
    }

    public boolean isRemovableFromContract() {
        return removableFromContract;
    }

    public boolean isImmutable() {
        return immutable;
    }

    public boolean isNegateable() {
        return negateable;
    }
}
