package com.siryus.swisscon.api.taskworklog.repos;

import com.siryus.swisscon.api.taskworklog.entity.TaskWorklogAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskWorklogAttachmentRepository extends JpaRepository<TaskWorklogAttachmentEntity,Integer> {
}
