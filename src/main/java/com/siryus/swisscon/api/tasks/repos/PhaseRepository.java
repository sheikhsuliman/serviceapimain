package com.siryus.swisscon.api.tasks.repos;

import com.siryus.swisscon.api.tasks.entity.PhaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhaseRepository extends JpaRepository<PhaseEntity, Integer> {
}
