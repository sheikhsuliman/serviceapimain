package com.siryus.swisscon.api.file.file;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface FileRepository extends JpaRepository<File, Integer> {
    @Modifying
    @Query("UPDATE File SET referenceId = :referenceId, referenceType = :referenceType WHERE id in (:ids)")
    void updateFileReferences(@Param("ids") Set<Integer> ids, @Param("referenceId") Integer referenceId, @Param("referenceType") String referenceType);

    @Modifying
    @Query("DELETE FROM File WHERE id IN (:ids)")
    void deleteFilesById(@Param("ids") Set<Integer> ids);
    
    @Query("SELECT id FROM File WHERE referenceType = :referenceType AND referenceId = :referenceId")
    List<Integer> getFileResourcesIds(@Param("referenceType") String referenceType, @Param("referenceId") Integer referenceId);
    
    @Query("select file from File file where file.referenceType = 'TEMPORARY' and file.lastModifiedDate < :date")
    List<File> findTemporaryFilesOlderThan(@Param("date") LocalDateTime date);

    @Transactional
    @Modifying
    @Query("update File file set file.disabled = current_timestamp where file.id = :fileId")
    void disable(@Param("fileId") Integer fileId);

}
