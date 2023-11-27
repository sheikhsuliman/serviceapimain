package com.siryus.swisscon.api.tasks.repos;

import com.siryus.swisscon.api.tasks.entity.SubTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubTaskRepository extends JpaRepository<SubTaskEntity, Integer> {

    @Query( value = "SELECT subTask FROM SubTaskEntity subTask WHERE subTask.mainTask.id = :mainTaskId AND subTask.disabled is NULL")
    List<SubTaskEntity> listSubTasksForMainTask(@Param("mainTaskId") Integer mainTaskId);

    @Query( value = "SELECT subTask FROM SubTaskEntity subTask WHERE subTask.mainTask.id in (:mainTaskIds) AND subTask.disabled is NULL")
    List<SubTaskEntity> listSubTasksInMainTasks(@Param("mainTaskIds") List<Integer> mainTaskIds);
    
    @Query( value = "SELECT subTask FROM SubTaskEntity subTask WHERE subTask.subTaskNumber = 0 AND subTask.mainTask.id = :mainTaskId")
    Optional<SubTaskEntity> getDefaultSubTask(@Param("mainTaskId") Integer mainTaskId);

    @Query(value = "SELECT MAX(sub_task_number) FROM sub_task subTask WHERE subTask.main_task_id = :mainTaskId", nativeQuery =  true)
    Integer getMaxSubTaskNumber(@Param("mainTaskId") Integer mainTaskId);

    @Transactional
    @Modifying
    @Query("update SubTaskEntity subTask set subTask.disabled = current_timestamp where subTask.id = :subTaskId")
    void archive(@Param("subTaskId") Integer subTaskId);
}
