package com.siryus.swisscon.api.util.counter;


import com.siryus.swisscon.api.general.reference.ReferenceType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.EnumType.STRING;

@Entity
@Table(name = "reference_based_counter")
public class TableBasedReferenceBasedCounterEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Integer id;

    @Column(name="reference_type")
    @Enumerated(STRING)
    private ReferenceType referenceType;

    @Column(name="reference_id")
    private Integer referenceId;

    @Column(name="counter_name")
    private String counterName;

    @Column(name="last_value")
    private Integer lastValue;

    public TableBasedReferenceBasedCounterEntity() {
    }

    public TableBasedReferenceBasedCounterEntity(ReferenceType referenceType, Integer referenceId, String counterName) {
       this(null, referenceType, referenceId, counterName, 0);
    }

    public TableBasedReferenceBasedCounterEntity(Integer id, ReferenceType referenceType, Integer referenceId, String counterName, Integer lastValue) {
        this.id = id;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.counterName = counterName;
        this.lastValue = lastValue;
    }

    public Integer getId() {
        return id;
    }

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public Integer getReferenceId() {
        return referenceId;
    }

    public String getCounterName() {
        return counterName;
    }

    public Integer getLastValue() {
        return lastValue;
    }
}
