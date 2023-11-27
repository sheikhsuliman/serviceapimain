package com.siryus.swisscon.api.project.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {

    @Query("SELECT p FROM Project p WHERE p.id = :projectId and p.disabled = null")
    Optional<Project> findActiveProjectById(@Param("projectId") Integer projectId );

    @Transactional
    @Modifying
    @Query("update Project project set project.disabled = current_timestamp where project.id = :projectId")
    void archiveProject(@Param("projectId") Integer projectId);

    @Transactional
    @Modifying
    @Query("update Project project set project.disabled = null where project.id = :projectId")
    void restoreProject(@Param("projectId") Integer projectId);
}
