package com.siryus.swisscon.api.tasks.repos;

import com.siryus.swisscon.api.tasks.entity.SubTaskCheckListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubTaskCheckListRepository extends JpaRepository<SubTaskCheckListEntity, Integer> {

    @Query( value = "SELECT l FROM SubTaskCheckListEntity l WHERE l.subTaskId = :subTaskId ORDER BY l.id ASC")
    List<SubTaskCheckListEntity> findAllBySubTaskId(@Param("subTaskId") Integer subTaskId);
}
