package com.siryus.swisscon.api.file;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileService;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileServiceTest extends AbstractMvcTestBase {

    private final FileService fileService;
    private final FileTestRepository fileTestRepository;

    private static File fileToDelete;
    private static File fileToStay;
    private static File fileNonTemporary;

    @Autowired
    public FileServiceTest(FileService fileService, FileTestRepository fileTestRepository) {
        this.fileService = fileService;
        this.fileTestRepository = fileTestRepository;
    }

    @Test
    public void testLoadFiles() {

        File file = fileService.findById(1);

        assertEquals("testfile_location_1", file.getFilename());
        assertEquals(ReferenceType.LOCATION.toString(), file.getReferenceType());
        assertEquals(Integer.valueOf(1), file.getReferenceId());
        assertTrue(file.getUrl().contains("url"));
        assertTrue(file.getUrlMedium().contains("url_800x800"));
        assertTrue(file.getUrlSmall().contains("url_128x128"));
    }

    @Test
    public void testCleanupTemporaryFiles() {
        fileService.cleanupTemporaryFiles();

        assertNull(fileService.findById(fileToDelete.getId()), "File which is temporary and older than one day should be deleted");
        assertNull(fileService.findById(fileToStay.getId()).getDisabled(), "File which is temporary and newer than one day should still exist");
        assertNull(fileService.findById(fileNonTemporary.getId()).getDisabled(), "File which is not temporary should still exist");
    }

    @BeforeEach
    public void createTestFiles() {
        // create file with last modified date -25 hours
        File unsavedFileToDelete = File.builder().referenceType(ReferenceType.TEMPORARY.toString()).isSystemFile(Boolean.FALSE).build();
        fileToDelete = fileService.create(unsavedFileToDelete);
        LocalDateTime minus25 = LocalDateTime.now().minusHours(25);
        fileTestRepository.setLastModifiedDate(minus25, fileToDelete.getId());

        // create file with last modified date -23 hours
        File unsavedFileToStay = File.builder().referenceType(ReferenceType.TEMPORARY.toString()).isSystemFile(Boolean.FALSE).build();
        fileToStay = fileService.create(unsavedFileToStay);
        LocalDateTime minus23 = LocalDateTime.now().minusHours(23);
        fileTestRepository.setLastModifiedDate(minus23, fileToStay.getId());

        // create file which is not temporary
        File nonTemporaryFile = File.builder().referenceType(ReferenceType.PROJECT.toString()).referenceId(1).isSystemFile(Boolean.FALSE).build();
        fileNonTemporary = fileService.create(nonTemporaryFile);
    }
}
