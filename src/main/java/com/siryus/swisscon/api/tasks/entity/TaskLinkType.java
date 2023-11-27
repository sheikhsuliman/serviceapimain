package com.siryus.swisscon.api.tasks.entity;

public enum TaskLinkType {
    DUPLICATES(CounterType.IS_DUPLICATED_BY),
    BLOCKS(CounterType.IS_BLOCKED_BY),
    RELATES_TO(CounterType.RELATES_TO);

    CounterType counterType;

    TaskLinkType(CounterType counterType) {
        this.counterType = counterType;
    }

    public CounterType getCounterType() {
        return counterType;
    }

    public enum CounterType {
        IS_DUPLICATED_BY,
        IS_BLOCKED_BY,
        RELATES_TO
    }
}
