package com.siryus.swisscon.api.tasks.repos;

import com.siryus.swisscon.api.tasks.entity.TaskLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskLinkRepository extends JpaRepository<TaskLinkEntity, Integer> {
    @Query(
            value = "SELECT tl FROM TaskLinkEntity tl WHERE tl.project.id = :projectId AND tl.disabled is null"
    )
    List<TaskLinkEntity> findTaskLinksInProject(@Param("projectId") Integer projectId );

    @Query(
            value = "SELECT tl FROM TaskLinkEntity tl WHERE tl.sourceTask.id = :srcTaskId AND tl.disabled is null"
    )
    List<TaskLinkEntity> findTaskLinksWithSourceTask(@Param("srcTaskId") Integer sourceTaskId );

    @Query(
            value = "SELECT tl FROM TaskLinkEntity tl WHERE tl.destinationTask.id = :dstTaskId AND tl.disabled is null"
    )
    List<TaskLinkEntity> findTaskLinksWithDestinationTask(@Param("dstTaskId") Integer destinationTaskId );

    @Modifying
    @Query(
            value = "UPDATE TaskLinkEntity tl SET tl.disabled = NOW() " +
                    "WHERE tl.disabled is null AND ( tl.sourceTask.id = :taskId OR  tl.destinationTask.id = :taskId )"
    )
    void disableAllLinksWithTask(@Param("taskId") Integer taskId);

    @Modifying
    @Query(
            value = "UPDATE TaskLinkEntity tl SET tl.disabled = NOW() " +
                    "WHERE tl.disabled is null AND tl.id = :id"
    )
    void disableTaskLink(@Param("id") Integer id);
}
