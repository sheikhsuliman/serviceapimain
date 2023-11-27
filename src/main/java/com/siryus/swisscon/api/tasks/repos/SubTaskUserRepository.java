package com.siryus.swisscon.api.tasks.repos;

import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.tasks.entity.SubTaskUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubTaskUserRepository extends JpaRepository<SubTaskUserEntity, Integer> {

    @Query("select stu from SubTaskUserEntity stu " +
            "join SubTaskEntity st on st.id = stu.subTask.id " +
            "join MainTaskEntity mt on st.mainTask.id = mt.id " +
            "where stu.user.id = :userId and mt.projectId = :projectId")
    List<SubTaskUserEntity> findByUserAndProject(@Param("userId") Integer userId, @Param("projectId") Integer projectId);

    @Query("select subTaskUser from SubTaskUserEntity subTaskUser where subTaskUser.user.id = :userId and subTaskUser.subTask.id = :subTaskId")
    Optional<SubTaskUserEntity> findByUserAndSubTask(@Param("userId") Integer userId, @Param("subTaskId") Integer subTaskId);

    @Query("select distinct subTaskUser.user from SubTaskUserEntity subTaskUser where subTaskUser.subTask.id = :subTaskId")
    List<User> findUsersBySubTask(@Param("subTaskId") Integer subTaskId);

    @Modifying
    @Query("delete from SubTaskUserEntity where subTask.id = :subTaskId")
    void deleteAllUsersBySubTask(@Param("subTaskId") Integer subTaskId);

    @Query("select distinct subTaskUser.user from SubTaskUserEntity subTaskUser where subTaskUser.subTask.id in :subTaskIds")
    List<User> findUsersBySubTasks(@Param("subTaskIds") List<Integer> subTaskIds);
}
