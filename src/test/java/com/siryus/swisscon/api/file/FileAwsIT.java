package com.siryus.swisscon.api.file;

import com.amazonaws.services.s3.AmazonS3;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileS3PersistenceService;
import com.siryus.swisscon.api.file.file.FileService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * These tests only run on postgresql spring profile
 * It's to prevent the necessity of an internet connection during local test execution
 */
@EnabledIfSystemProperty(named = "spring.profiles.active", matches = "postgresql")
public class FileAwsIT extends AbstractFileIT {

    private final AmazonS3 amazonS3Client;

    @Autowired
    public FileAwsIT(FileService fileService, FileS3PersistenceService persistenceService, AmazonS3 amazonS3Client) {
        super(fileService, persistenceService);
        this.amazonS3Client = amazonS3Client;
    }

    /**
     * Sets the real connected AWS S3 Client after the {@link AbstractMvcTestBase#baseSetUp()} is executed
     */
    @BeforeAll
    public void setRealAmazonS3Client() {
        persistenceService.setS3Client(amazonS3Client);
    }

    @Test
    public void testPngIconFile() throws Exception {
        // store file
        File resource = this.saveFile(ICON_FILE, "image/png");

        // load file and proof properties
        File file = fileService.findById(resource.getId());
        assertSavedFile(ICON_FILE, "image/png", file, true, false);

        cleanupAndCheckS3(resource, ICON_FILE);
    }

}
