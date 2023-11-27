package com.siryus.swisscon.api.util.counter;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TableBasedReferenceBasedCounterRepository extends JpaRepository<TableBasedReferenceBasedCounterEntity, Integer> {
    @Query("SELECT e FROM TableBasedReferenceBasedCounterEntity e WHERE e.referenceType = :referenceType AND e.referenceId = :referenceId AND e.counterName = :counterName")
    Optional<TableBasedReferenceBasedCounterEntity> findFirstByReferenceTypeAndReferenceIdAndCounterName(@Param("referenceType")ReferenceType referenceType, @Param("referenceId") Integer referenceId, @Param("counterName") String counterName);

    @Query("SELECT e.lastValue FROM TableBasedReferenceBasedCounterEntity e WHERE e.referenceType = :referenceType AND e.referenceId = :referenceId AND e.counterName = :counterName")
    Integer getLastValue(@Param("referenceType")ReferenceType referenceType, @Param("referenceId") Integer referenceId, @Param("counterName") String counterName);

    @Modifying
    @Query("UPDATE TableBasedReferenceBasedCounterEntity e SET e.lastValue = e.lastValue + 1  WHERE e.referenceType = :referenceType AND e.referenceId = :referenceId AND e.counterName = :counterName")
    void incLastValue(@Param("referenceType")ReferenceType referenceType, @Param("referenceId") Integer referenceId, @Param("counterName") String counterName);
}
