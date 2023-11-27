package com.siryus.swisscon.api.tasks.repos;

import com.siryus.swisscon.api.tasks.entity.MainTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MainTaskRepository extends JpaRepository<MainTaskEntity, Integer>, JpaSpecificationExecutor<MainTaskEntity> {

    @Query("SELECT mainTask FROM MainTaskEntity mainTask WHERE mainTask.locationId IN (:locationIDs) AND mainTask.disabled is NULL")
    List<MainTaskEntity>  listTasksForLocations(@Param("locationIDs") List<Integer> locationIDs);

    @Query("SELECT mainTask FROM MainTaskEntity mainTask WHERE mainTask.projectId = :projectId AND (mainTask.companyId = :companyId OR mainTask.companyId is NULL AND mainTask.disabled is NULL)")
    List<MainTaskEntity> listTasksForProjectAndCompanyOrUnassigned(@Param("projectId") Integer projectID, @Param("companyId") Integer companyId);

    @Query("SELECT mainTask FROM MainTaskEntity mainTask WHERE mainTask.projectId = :projectId AND mainTask.template = true")
    List<MainTaskEntity> listTemplatesInProject(@Param("projectId") Integer projectID);

    @Query("SELECT projectId FROM MainTaskEntity where id = :taskId")
    Optional<Integer> getProjectIdForTask(@Param("taskId") Integer taskId );

    @Transactional
    @Modifying
    @Query("update MainTaskEntity mainTask set mainTask.disabled = current_timestamp where mainTask.id = :mainTaskId")
    void archive(@Param("mainTaskId") Integer mainTaskId);
}
