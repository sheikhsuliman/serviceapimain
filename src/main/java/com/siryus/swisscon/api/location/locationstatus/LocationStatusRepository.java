package com.siryus.swisscon.api.location.locationstatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationStatusRepository extends JpaRepository<LocationStatus, Integer> {
}
