package com.siryus.swisscon.api.location.locationsubtype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationSubTypeRepository extends JpaRepository<LocationSubType, Integer> {
}
