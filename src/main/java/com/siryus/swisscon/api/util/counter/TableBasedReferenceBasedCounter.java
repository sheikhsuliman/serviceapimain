package com.siryus.swisscon.api.util.counter;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component(ReferenceBasedCounter.REFERENCE_BASED_COUNTER)
public class TableBasedReferenceBasedCounter implements ReferenceBasedCounter {
    private final TableBasedReferenceBasedCounterRepository repository;
    private final ReferenceType referenceType;
    private final Integer referenceId;
    private final String counterName;

    TableBasedReferenceBasedCounter(
            TableBasedReferenceBasedCounterRepository repository,
            ReferenceType referenceType,
            Integer referenceId,
            String counterName
    ) {
        this.repository = repository;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.counterName = counterName;
    }

    @Transactional
    @Override
    public synchronized Integer getNextValue() {
        ensureCounter();

        repository.incLastValue(referenceType, referenceId, counterName);
        return getLastValue();
    }

    @Override
    public synchronized Integer getLastValue() {
        ensureCounter();

        return repository.getLastValue(referenceType, referenceId, counterName);
    }

    private void ensureCounter() {
        if (!repository.findFirstByReferenceTypeAndReferenceIdAndCounterName(referenceType, referenceId, counterName).isPresent()) {
            repository.save(new TableBasedReferenceBasedCounterEntity(referenceType, referenceId, counterName));
        }
    }
}
