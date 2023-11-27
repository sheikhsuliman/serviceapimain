package com.siryus.swisscon.api.general.unit;

import java.lang.Integer;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Integer> {

    @Query("FROM Unit WHERE symbol = :symbol")
    Unit findBySymbolName(@Param("symbol") String symbol);

    @Query("FROM Unit WHERE symbol = :symbol")
    Optional<Unit> findUnitWithSymbolName(@Param("symbol") String symbol);
}
