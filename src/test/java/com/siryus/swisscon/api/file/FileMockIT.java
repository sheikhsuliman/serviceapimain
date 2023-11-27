package com.siryus.swisscon.api.file;

import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileController;
import com.siryus.swisscon.api.file.file.FileS3PersistenceService;
import com.siryus.swisscon.api.file.file.FileService;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.siryus.swisscon.api.file.image.ImageThumbUtil.MAX_HEIGHT;
import static com.siryus.swisscon.api.file.image.ImageThumbUtil.MAX_WIDTH;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileMockIT extends AbstractFileIT {

    @Autowired
    public FileMockIT(FileService fileService, FileS3PersistenceService persistenceService) {
        super(fileService, persistenceService);
    }

    @Test @Disabled
    public void testJpgFile() throws Exception {

        // store file
        File resource = this.saveFile(JPG_FILE, "image/jpeg");

        // load file and proof properties
        File file = fileService.findById(resource.getId());
        assertSavedFile(JPG_FILE, "image/jpeg", file, true);

        cleanupAndCheckS3(resource, JPG_FILE);
    }

    @Test @Disabled
    public void testHighResImageIsScaled() throws Exception {
        // store file
        File resource = this.saveFile(JPG_FILE_2, "image/jpeg");

        // load file and proof properties
        File file = fileService.findById(resource.getId());

        // check that jpg is scaled
        BufferedImage image = getBufferedImage(file);

        assertTrue(image.getHeight() <= MAX_HEIGHT);
        assertTrue(image.getWidth() <= MAX_WIDTH);

        cleanupAndCheckS3(resource, JPG_FILE_2);
    }

    @Test @Disabled
    public void testLowResImageIsNotScaled() throws Exception {
        // store file
        File resource = this.saveFile(PNG_FILE, "image/png");

        // load file and proof properties
        File file = fileService.findById(resource.getId());

        // check that jpg is scaled
        BufferedImage image = getBufferedImage(file);

        assertEquals(107, image.getHeight());
        assertEquals(197, image.getWidth());

        cleanupAndCheckS3(resource, PNG_FILE);
    }

    private BufferedImage getBufferedImage(File file) throws IOException {
        byte[] bytes;
        if (file.getUrl().startsWith("http")) {
            bytes = Jsoup.connect(file.getUrl()).ignoreContentType(true).execute().bodyAsBytes();

        } else {
            Path path = Paths.get(FileUtils.toFile(new URL(file.getUrl())).getPath());
            bytes = Files.readAllBytes(path);
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        return ImageIO.read(bis);
    }

    @Test 
    @Disabled("See https://dtsystems.atlassian.net/browse/SI-195")
    public void testPdfFile() throws Exception {
        // store file
        File resource = this.saveFile(PDF_FILE, "application/pdf");

        // load file and proof properties
        File file = fileService.findById(resource.getId());
        assertSavedFile(PDF_FILE, "application/pdf", file, true);

        cleanupAndCheckS3(resource, PDF_FILE);
    }

    @Test @Disabled
    public void testMp3File() throws Exception {
        // store file
        File resource = this.saveFile(AUDIO_FILE, "audio/mpeg");

        // load file and proof properties
        File file = fileService.findById(resource.getId());
        assertSavedFile(AUDIO_FILE, "audio/mpeg", file, false);

        cleanupAndCheckS3(resource, AUDIO_FILE);
    }

    @Test @Disabled
    public void testMp4File() throws Exception {
        // store file
        File resource = this.saveFile(VIDEO_FILE, "video/mp4");

        // load file and proof properties
        File file = fileService.findById(resource.getId());
        assertSavedFile(VIDEO_FILE, "video/mp4", file, false);

        cleanupAndCheckS3(resource, VIDEO_FILE);
    }

    @Test @Disabled
    public void testZipFile() throws Exception {
        File resource = this.saveFile(ZIP_FILE, "application/zip");

        // load file and proof properties
        File file = fileService.findById(resource.getId());
        assertSavedFile(ZIP_FILE, "application/zip", file, false);

        cleanupAndCheckS3(resource, ZIP_FILE);
    }

    @Test @Disabled
    void testFileWithWrongForeignKey() throws IOException {
        RequestSpecification specification = loginSpec();

        // Obtain a file
        final byte[] resourceFile = IOUtils.toByteArray(getClass().getResourceAsStream("/docs/" + JPG_FILE));

        // POST
        given().spec(specification).contentType("multipart/form-data")
                .multiPart(new MultiPartSpecBuilder(resourceFile)
                        .fileName(JPG_FILE)
                        .controlName(FileController.MULTIFORM_FILE_IDENTIFIER)
                        .mimeType("image/jpeg").build())
                .multiPart("referenceType", ReferenceType.PROJECT.toString())
                .multiPart("referenceId", "999999999")
                .when().post("/api/rest/files").then().assertThat().statusCode(500);
    }

    @Test @Disabled
    void testTemporaryFile() throws IOException {
        RequestSpecification specification = loginSpec();

        // Obtain a file
        final byte[] resourceFile = IOUtils.toByteArray(getClass().getResourceAsStream("/docs/" + JPG_FILE));

        // POST
        File file = given().spec(specification).contentType("multipart/form-data")
                .multiPart(new MultiPartSpecBuilder(resourceFile)
                        .fileName(JPG_FILE)
                        .controlName(FileController.MULTIFORM_FILE_IDENTIFIER)
                        .mimeType("image/jpeg").build())
                .multiPart("referenceType", ReferenceType.TEMPORARY.toString())
                .when().post("/api/rest/files").then().assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(File.class);

        assertEquals(ReferenceType.TEMPORARY, ReferenceType.valueOf(file.getReferenceType()));

        cleanupAndCheckS3(file, JPG_FILE);
    }

    @Test @Disabled
    void testWithNonNumericNumber() throws IOException {
        RequestSpecification specification = loginSpec();

        // Obtain a file
        final byte[] resourceFile = IOUtils.toByteArray(getClass().getResourceAsStream("/docs/" + JPG_FILE));

        // POST
        given().spec(specification).contentType("multipart/form-data")
                .multiPart(new MultiPartSpecBuilder(resourceFile)
                        .fileName(JPG_FILE)
                        .controlName(FileController.MULTIFORM_FILE_IDENTIFIER)
                        .mimeType("image/jpeg").build())
                .multiPart("referenceType", ReferenceType.PROJECT.toString())
                .multiPart("referenceId", "string")
                .when().post("/api/rest/files").then().assertThat().statusCode(400);
    }

    @Test @Disabled
    void testWithWrongReferenceType() throws IOException {
        RequestSpecification specification = loginSpec();

        // Obtain a file
        final byte[] resourceFile = IOUtils.toByteArray(getClass().getResourceAsStream("/docs/" + JPG_FILE));

        // POST
        given().spec(specification).contentType("multipart/form-data")
                .multiPart(new MultiPartSpecBuilder(resourceFile)
                        .fileName(JPG_FILE)
                        .controlName(FileController.MULTIFORM_FILE_IDENTIFIER)
                        .mimeType("image/jpeg").build())
                .multiPart("referenceType", "wrong reference type")
                .multiPart("referenceId", "1")
                .when().post("/api/rest/files").then().assertThat().statusCode(400);
    }

    @Test @Disabled
    void testFileWithoutForeignKey() throws IOException {
        RequestSpecification specification = loginSpec();

        // Obtain a file
        final byte[] resourceFile = IOUtils.toByteArray(getClass().getResourceAsStream("/docs/" + JPG_FILE));

        // POST
        given().spec(specification).contentType("multipart/form-data")
                .multiPart(new MultiPartSpecBuilder(resourceFile)
                        .fileName(JPG_FILE)
                        .controlName(FileController.MULTIFORM_FILE_IDENTIFIER)
                        .mimeType("image/jpeg").build())
                .multiPart("referenceType", ReferenceType.PROJECT.toString())
                .when().post("/api/rest/files").then().assertThat().statusCode(400);
    }

}
