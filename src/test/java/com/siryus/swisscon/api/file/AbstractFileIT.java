package com.siryus.swisscon.api.file;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileController;
import com.siryus.swisscon.api.file.file.FileS3PersistenceService;
import com.siryus.swisscon.api.file.file.FileService;
import com.siryus.swisscon.api.file.mock.MockS3Client;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

import static com.siryus.swisscon.api.file.image.ImageThumbUtil.getMediumPath;
import static com.siryus.swisscon.api.file.image.ImageThumbUtil.getSmallPath;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AbstractFileIT extends AbstractMvcTestBase {

    @Value("${aws_namecard_bucket}")
    private String nameCardBucket;

    @Value("${aws_region}")
    private String awsRegion;


    // if you add a new file: add it to ALL_FILES for the cleanup
    protected static final String JPG_FILE = "FileImage.jpg";
    protected static final String JPG_FILE_2 = "high_res.jpg";
    protected static final String PNG_FILE = "UserTestImage.png";
    protected static final String PDF_FILE = "test.pdf";
    protected static final String AUDIO_FILE = "SampleAudio_0.4mb.mp3";
    protected static final String VIDEO_FILE = "SampleVideo_1280x720_1mb.mp4";
    protected static final String ZIP_FILE = "test.zip";
    protected static final String ICON_FILE = "icon.png";

    private static final List<String> ALL_FILES = Arrays.asList(JPG_FILE, JPG_FILE_2, PNG_FILE, PDF_FILE, ICON_FILE, AUDIO_FILE, VIDEO_FILE, ZIP_FILE);

    protected FileService fileService;
    protected FileS3PersistenceService persistenceService;

    public AbstractFileIT(FileService fileService, FileS3PersistenceService persistenceService) {
        this.fileService = fileService;
        this.persistenceService = persistenceService;
    }

    @BeforeAll
    public void initMockAndCleanup() {
        cleanAllFiles();
    }

    @AfterAll
    public void removeMockAndCleanup() {
        cleanAllFiles();
    }

    protected File saveFile(String filename, String mimeType) throws Exception {
        RequestSpecification specification = loginSpec();

        // Obtain a file
        final byte[] resourceFile = IOUtils.toByteArray(getClass().getResourceAsStream("/docs/" + filename));

        File resource;

        // POST
        resource = given().spec(specification).contentType("multipart/form-data")
                .multiPart(new MultiPartSpecBuilder(resourceFile)
                        .fileName(filename)
                        .controlName(FileController.MULTIFORM_FILE_IDENTIFIER)
                        .mimeType(mimeType).build())
                .multiPart("referenceType", ReferenceType.PROJECT.toString())
                .multiPart("referenceId", "1")
                .when().post("/api/rest/files").then().assertThat().statusCode(HttpStatus.CREATED.value()).extract()
                .as(File.class);

        // GET
        resource = given().spec(specification)
                .get("/api/rest/files/" + resource.getId()).then()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract().as(File.class);

        assertEquals(mimeType, resource.getMimeType());
        assertNotNull(resource.getLength());
        return resource;
    }

    private String getExpectedFilePath(Integer id, String filename, boolean localExecution) {
        if(localExecution) {
            return MockS3Client.TEMP_FOLDER + "/" + fileService.getBasePath(id) + filename;
        }
        return "https://" +
                nameCardBucket +
                ".s3." +
                awsRegion +
                ".amazonaws.com/" +
                fileService.getBasePath(id) +
                filename;
    }

    protected void assertSavedFile(String filename, String mimeType, File file, boolean hasPreview) {
        assertSavedFile(filename, mimeType, file, hasPreview, true);
    }

    protected void assertSavedFile(String filename, String mimeType, File file, boolean hasPreview, boolean localExecution) {
        assertEquals(filename, file.getFilename(), "Stored filename should match");
        assertEquals(mimeType, file.getMimeType(), "Stored mime type should match");
        assertEquals(Integer.valueOf(1), file.getReferenceId(), "Stored reference id should match");
        assertEquals(ReferenceType.PROJECT.toString(), file.getReferenceType(), "Stored reference type should match");

        String expectedFilePath = getExpectedFilePath(file.getId(), filename, localExecution);
        assertThat(file.getUrl(), CoreMatchers.containsString(expectedFilePath));

        if (hasPreview) {
            assertThat(file.getUrlSmall(), CoreMatchers.containsString(getSmallPath(expectedFilePath)));
            assertThat(file.getUrlMedium(), CoreMatchers.containsString(getMediumPath(expectedFilePath)));
        } else {
            assertNull(file.getUrlSmall(), "There should not be a small url");
            assertNull(file.getUrlSmall(), "There should not be a medium url");
        }
    }

    protected void cleanupAndCheckS3(File resource, String filename) {
        assertS3FileExists(resource.getId(), filename);
        fileService.deletePermanently(resource.getId());
        assertS3FileIsDeleted(resource.getId(), filename);
    }

    private void assertS3FileExists(Integer id, String filename) {
        String path = fileService.getBasePath(id) + filename;
        assertTrue(persistenceService.fileWithPrefixExist(path), "File should exist on S3");
    }

    private void assertS3FileIsDeleted(Integer id, String filename) {
        String path = fileService.getBasePath(id) + filename;
        assertFalse(persistenceService.fileWithPrefixExist(path), "File on S3 should be deleted");
    }
    private void cleanAllFiles() {
        ALL_FILES.forEach(filename -> persistenceService.deleteFilesWhichContains(File.class.getSimpleName() + "/", filename));
    }


}
