package com.siryus.swisscon.api.file;

import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Repository for the {@link FileServiceTest}. The normal {@link FileService} cannot modify the
 * last modified date. For that we need a special update statement.
 */
public interface FileTestRepository extends JpaRepository<File, Integer> {

    @Modifying
    @Transactional
    @Query("update File file set file.lastModifiedDate = ?1 where file.id = ?2")
    void setLastModifiedDate(LocalDateTime date, Integer id);

    @Modifying
    @Transactional
    @Query("update File file set file.lastModifiedBy = ?1 where file.id = ?2")
    void setLastModifiedBy(Integer userId, Integer id);

}
