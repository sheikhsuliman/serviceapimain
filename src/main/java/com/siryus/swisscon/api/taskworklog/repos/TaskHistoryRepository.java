package com.siryus.swisscon.api.taskworklog.repos;

import com.google.common.collect.ImmutableSet;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventType;
import com.siryus.swisscon.api.taskworklog.entity.TaskWorklogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Set;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskWorklogEntity,Integer>
{
    Set<String> MAIN_TASK_HISTORY_EVENTS = ImmutableSet.of(WorkLogEventType.START_TASK.name(),
            WorkLogEventType.COMPLETE_TASK.name(),
            WorkLogEventType.REJECT_TASK.name(),
            WorkLogEventType.APPROVE_TASK.name(),
            WorkLogEventType.START_TIMER.name());

    @Query(value = "select twl.* from task_work_log twl where " +
            "twl.main_task_id= :mainTaskId and " +
            "(twl.event in ( :events ) ) " +
            "order by twl.timestamp desc",nativeQuery = true)
    Page<TaskWorklogEntity> findByMainTask(@Param("mainTaskId") Integer mainTaskId, @Param("events") Set<String> events, Pageable pageable);

    @Query(value = "select twlStart.id, extract(epoch from min(twlEnd.timestamp - twlStart.timestamp)) " + 
            "from task_work_log twlEnd, task_work_log twlStart where " +
            "( twlStart.id in ( :ids ) ) and twlStart.main_task_id = twlEnd.main_task_id and " +
            "twlStart.worker_id = twlEnd.worker_id and twlEnd.event = 'STOP_TIMER' and " +
            "twlEnd.timestamp > twlStart.timestamp " +
            "group by twlStart.id", nativeQuery = true)
    List<Tuple> findDurationsOfStartEvents(@Param("ids") Set<Integer> ids);    
}
