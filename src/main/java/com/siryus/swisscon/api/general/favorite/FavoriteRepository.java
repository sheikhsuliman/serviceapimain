package com.siryus.swisscon.api.general.favorite;

import com.siryus.swisscon.api.project.project.Project;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

    @Modifying
    @Transactional
    @Query(value = "delete from Favorite fav where fav.referenceId=?1 and fav.referenceType = ?2")
    void removeForAllUsers(Integer referenceId, String referenceType);

    @Modifying
    @Transactional
    @Query(
        value = "DELETE FROM favorite fav WHERE fav.user_id = :userId AND fav.reference_id=:referenceId AND fav.reference_type = :referenceType",
        nativeQuery = true
    )
    void removeForUser(@Param("userId") Integer userId, @Param("referenceId") Integer referenceId, @Param("referenceType") String referenceType);

    @Query(value = "select case when count(fav)> 0 then true else false end from Favorite fav where fav.user.id = ?1 and fav.referenceId=?2 and fav.referenceType = ?3")
    boolean exists(Integer userId, Integer referenceId, String referenceType);
    
    @Query(value = "select fav.referenceId from Favorite fav where fav.user.id = ?1 and fav.referenceType=?2")
    List<Integer> allFavouritesIds(Integer userId, String referenceType); 
    
    @Query("select p from Project p, Favorite f where p.id = f.referenceId and f.referenceType='PROJECT' and f.user.id = :userId and p.disabled is null")
    Page<Project> findFavoriteProjects(@Param("userId") Integer userId, Pageable pageable);
}
