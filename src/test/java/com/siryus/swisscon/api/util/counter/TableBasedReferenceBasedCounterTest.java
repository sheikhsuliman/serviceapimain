package com.siryus.swisscon.api.util.counter;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class TableBasedReferenceBasedCounterTest extends AbstractMvcTestBase {
    private static final String TEST_COUNTER = "TEST_COUNTER";

    @Autowired
    private ReferenceBasedCounterFactory factory;

    private ReferenceBasedCounter counter;

    @Test
    public void Given_newlyCreatedCounter_When_getNext_Then_return1() {
        counter = factory.counter(ReferenceType.PROJECT, 1, TEST_COUNTER);

        assertEquals(Integer.valueOf(1), counter.getNextValue());
        assertEquals(Integer.valueOf(2), counter.getNextValue());
        assertEquals(Integer.valueOf(3), counter.getNextValue());

        assertEquals(Integer.valueOf(3), counter.getLastValue());
    }

    @Test
    public void Given_sameReferenceTypeIdAndCounterName_When_factoryCounter_Then_alwaysReturnTheSameInstance() {
        counter = factory.counter(ReferenceType.PROJECT, 1, TEST_COUNTER);
        ReferenceBasedCounter otherCounter = factory.counter(ReferenceType.PROJECT, 1, TEST_COUNTER);

        assertEquals(counter, otherCounter);

        ReferenceBasedCounter yetAnotherCounter = factory.counter(ReferenceType.PROJECT, 2, TEST_COUNTER);

        assertNotEquals(counter, yetAnotherCounter);
    }
}