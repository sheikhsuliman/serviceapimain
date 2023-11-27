package com.siryus.swisscon.api.location.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {

    @Query("select location from Location location where location.project.id = :projectId and location.parentId = null and location.disabled = null")
    Optional<Location> findRootByProject(@Param("projectId") Integer projectId);

    @Query("select location.id from Location location where location.parentId = :parentId and location.disabled = null order by location.order asc")
    List<Integer> findChildrenIdsByParent(@Param("parentId") Integer parentId);

    @Query("select location from Location location where location.id in :ids order by location.order asc")
    List<Location> findAllByIdOrderByOrder(@Param("ids") Iterable<Integer> ids);

    @Transactional
    @Modifying
    @Query("update Location location set location.disabled = current_timestamp where location.id = :locationId")
    void disable(@Param("locationId") Integer locationId);

}
