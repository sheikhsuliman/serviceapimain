package com.siryus.swisscon.api.util.counter;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ReferenceBasedCounterFactory implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    private final Map<Key, ReferenceBasedCounter> instances = new HashMap<>();

    @Autowired
    private TableBasedReferenceBasedCounterRepository repository;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ReferenceBasedCounter counter(ReferenceType referenceType, Integer referenceId, String counterName) {
        return instances.computeIfAbsent (
                new Key(referenceType, referenceId, counterName),
                (k) -> (ReferenceBasedCounter) applicationContext.getBean(
                        ReferenceBasedCounter.REFERENCE_BASED_COUNTER,
                        repository, k.referenceType, k.referenceId, k.counterName
                )
        );
    }
}

class Key {

    final ReferenceType referenceType;
    final Integer referenceId;
    final String counterName;

    public Key(ReferenceType referenceType, Integer referenceId, String counterName) {
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.counterName = counterName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Key)) return false;

        Key key = (Key) o;

        if (referenceType != key.referenceType) return false;
        if (!referenceId.equals(key.referenceId)) return false;
        return counterName.equals(key.counterName);
    }

    @Override
    public int hashCode() {
        int result = referenceType.hashCode();
        result = 31 * result + referenceId.hashCode();
        result = 31 * result + counterName.hashCode();
        return result;
    }
}