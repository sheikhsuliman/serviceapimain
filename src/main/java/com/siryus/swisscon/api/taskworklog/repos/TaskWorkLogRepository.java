package com.siryus.swisscon.api.taskworklog.repos;

import com.google.common.collect.ImmutableSet;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventCounter;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventPerSubTaskCounter;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventType;
import com.siryus.swisscon.api.taskworklog.entity.TaskWorklogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TaskWorkLogRepository extends JpaRepository<TaskWorklogEntity,Integer> {
    Set<String> TIMER_EVENTS = ImmutableSet.of(WorkLogEventType.START_TIMER.name(),WorkLogEventType.STOP_TIMER.name(), WorkLogEventType.CANCEL_TIMER.name());

    @Query(value = "select twl.* from task_work_log twl where " +
            "twl.main_task_id= :mainTaskId and " +
            "(twl.event in ( :events ) )" +
            "order by timestamp desc LIMIT 1 ", nativeQuery = true)
    Optional<TaskWorklogEntity> getLastMainTaskWorkLog(@Param("mainTaskId") Integer mainTaskId, @Param("events") Set<String> events);

    @Query(value = "select twl.* from task_work_log twl where " +
            "twl.worker_id = :userId and twl.Sub_Task_Id = :subTaskId and " +
            "twl.event in ( :events ) " +
            "order by twl.timestamp", nativeQuery = true)
    List<TaskWorklogEntity> findByWorkerAndSubTask(@Param("userId") Integer userId, @Param("subTaskId") Integer subTaskId, @Param("events") Set<String> events);

    @Query(value = "select twl.* from task_work_log twl where " +
            "twl.Sub_Task_Id= :subTaskId and " +
            "(twl.event in ( :events )) " +
            "order by twl.id asc ", nativeQuery = true)
    List<TaskWorklogEntity> findBySubTask(@Param("subTaskId") Integer subTaskId, @Param("events") Set<String> events);

    @Query(value = "select twl.* from task_work_log twl where " +
            "twl.main_task_id = :mainTaskId " +
            "and twl.event in ( :events ) " +
            "order by twl.id asc ", nativeQuery = true)
    List<TaskWorklogEntity> findByMainTask(@Param("mainTaskId") Integer mainTaskId, @Param("events") Set<String> events);

    @Query(nativeQuery = true)
    List<WorkLogEventPerSubTaskCounter> getEventsCountForWorkerBySubTask(@Param("workerId") Integer workerId);

    @Query(nativeQuery = true)
    List<WorkLogEventCounter> getEventsCountForWorker(@Param("workerId") Integer workerId);

    @Query(nativeQuery = true)
    List<WorkLogEventCounter> getEventsCountForSubTask(@Param("subTaskId") Integer subTaskId);

    @Query(nativeQuery = true)
    List<WorkLogEventCounter> getEventsCountForMainTask(@Param("mainTaskId") Integer mainTaskId);

    @Query(
            value = "SELECT twl.* FROM task_work_log twl WHERE twl.worker_id = :workerId AND twl.event IN ( :events ) " +
                    "ORDER BY twl.id DESC " +
                    "LIMIT 1",
            nativeQuery = true
    )
    Optional<TaskWorklogEntity> lastEventForWorker(@Param("workerId") Integer workerId, @Param("events") Set<String> events);

    @Query(
            value = "SELECT twl.* FROM task_work_log twl WHERE twl.worker_id = :workerId AND twl.sub_task_id = :subTaskId AND twl.event IN ( :events ) " +
                    "ORDER BY twl.id DESC " +
                    "LIMIT 1",
            nativeQuery = true
    )
    Optional<TaskWorklogEntity> lastEventForWorkerAndSubTask(
            @Param("workerId") Integer workerId,
            @Param("subTaskId") Integer subTaskId,
            @Param("events") Set<String> events
    );
}
