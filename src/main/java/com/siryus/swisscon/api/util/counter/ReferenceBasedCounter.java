package com.siryus.swisscon.api.util.counter;

public interface ReferenceBasedCounter {
    String REFERENCE_BASED_COUNTER = "referenceBasedCounter";

    Integer getNextValue();
    Integer getLastValue();
}
