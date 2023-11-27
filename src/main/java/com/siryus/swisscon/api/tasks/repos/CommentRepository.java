/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.tasks.repos;

import com.siryus.swisscon.api.tasks.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Integer>, JpaSpecificationExecutor<CommentEntity> {
    @Query(value = "SELECT comment FROM CommentEntity comment WHERE comment.subTaskId = :subTaskId and comment.disabled is null order by comment.id asc")
    List<CommentEntity> findBySubTaskId(@Param("subTaskId") Integer subTaskId);
    
}
