package com.siryus.swisscon.api.base.helpers;

import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileController;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.mediawidget.CreateFolderRequest;
import com.siryus.swisscon.api.mediawidget.MediaWidgetFileDTO;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventType;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.springframework.http.HttpStatus;

import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static com.siryus.swisscon.api.base.AbstractMvcTestBase.endPoint;
import com.siryus.swisscon.api.mediawidget.DeleteFileRequest;
import com.siryus.swisscon.api.mediawidget.UpdateFileRequest;
import static io.restassured.RestAssured.given;
import java.util.function.Consumer;
import static org.hamcrest.CoreMatchers.equalTo;

public class Media {
    public MediaWidgetFileDTO createWorklogAttachment(RequestSpecification spec, Integer mainTaskId, WorkLogEventType event) {
        if (!event.supportsAttachments()) {
            return null;
        }

        java.io.File tempFile;
        try {
            tempFile = java.io.File.createTempFile("test", "file", null);
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(new byte[] { 1 });
        } catch (Exception e) {
            return null;
        }

        return given().spec(spec)
                .contentType("multipart/form-data")
                .multiPart("file", tempFile)
                .multiPart("folder", event.attachmentSystemFolder())
                .pathParam("referenceType", ReferenceType.MAIN_TASK.name())
                .pathParam("referenceId", mainTaskId)
                .post(endPoint("/media/{referenceType}/{referenceId}/system-file"))
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.OK.value()))
                .extract().as(MediaWidgetFileDTO.class);
    }

    public List<MediaWidgetFileDTO> listFiles(RequestSpecification spec, ReferenceType referenceType, Integer referenceId) {
        return listFiles(spec, referenceType, referenceId,
                r -> Arrays.asList(r.assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .as(MediaWidgetFileDTO[].class))
        );
    }

    public List<MediaWidgetFileDTO> listFiles(RequestSpecification spec, ReferenceType referenceType, Integer referenceId, Function<ValidatableResponse, List<MediaWidgetFileDTO>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("referenceType", referenceType.toString())
                        .pathParam("referenceId", String.valueOf(referenceId))
                        .get(endPoint("/media/{referenceType}/{referenceId}"))
                        .then()
        );
    }

    public File fileUploadTemporary(RequestSpecification spec) {
        return fileUpload(spec, ReferenceType.TEMPORARY, null);
    }

    public File fileUploadTemporary(RequestSpecification spec, Function<ValidatableResponse, File> responseValidator) {
        return fileUpload(spec, ReferenceType.TEMPORARY, null, responseValidator);
    }

    public File fileUpload(RequestSpecification spec, ReferenceType referenceType, Integer referenceId) {
        return fileUpload(spec, referenceType, referenceId,
                r-> r.assertThat().statusCode(HttpStatus.CREATED.value()).extract()
                        .as(File.class));
    }

    public MediaWidgetFileDTO createMediaWidgetFolder(RequestSpecification spec, ReferenceType referenceType, Integer referenceId, Integer parentNodeId, String name, Function<ValidatableResponse, MediaWidgetFileDTO> responseValidator) {
        CreateFolderRequest request = new CreateFolderRequest(parentNodeId, name);

        return responseValidator.apply(given().spec(spec)
                .body(request)
                .pathParam("referenceType", referenceType)
                .pathParam("referenceId", referenceId)
                .post(endPoint("/media/{referenceType}/{referenceId}/folder"))
                .then()
        );
    }
    
    public MediaWidgetFileDTO updateMediaWidgetNode(RequestSpecification spec, Integer nodeId, String name) {
        return updateMediaWidgetNode(
                spec, nodeId, name, 
                r -> { return r.assertThat().statusCode(HttpStatus.OK.value()).extract().as(MediaWidgetFileDTO.class); }
        );
    }

    public MediaWidgetFileDTO updateMediaWidgetNode(RequestSpecification spec, Integer nodeId, String name, Function<ValidatableResponse, MediaWidgetFileDTO> responseValidator) {
        return responseValidator.apply(given().spec(spec)
                .body(new UpdateFileRequest(nodeId, name))
                .post(endPoint("/media/folder/update"))
                .then()
        );
    }

    public void deleteMediaWidgetNode(RequestSpecification spec, Integer nodeId) {
        deleteMediaWidgetNode(
            spec, nodeId, 
            r -> { r.assertThat().statusCode(HttpStatus.OK.value()); }
        );
    }

    public void deleteMediaWidgetNode(RequestSpecification spec, Integer nodeId, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(given().spec(spec)
                        .body(new DeleteFileRequest(nodeId))
                        .post(endPoint("/media/folder/delete"))
                        .then()
        );        
    }    

    public MediaWidgetFileDTO createMediaWidgetFile(RequestSpecification spec, ReferenceType referenceType, Integer referenceId, Integer parentNodeId, String name, Function<ValidatableResponse, MediaWidgetFileDTO> responseValidator) {
        String tempDir = System.getProperty("java.io.tmpdir");

        java.io.File tempFile;
        try {
            tempFile = new java.io.File(tempDir + "/" + name);
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(new byte[] { 1, 2, 3 });
        } catch (Exception e) {
            return null;
        }

        return responseValidator.apply(given().spec(spec)
                .contentType("multipart/form-data")
                .multiPart("file", tempFile)
                .multiPart("parentNodeId", parentNodeId != null ? parentNodeId : "")
                .pathParam("referenceType", referenceType)
                .pathParam("referenceId", referenceId)
                .post(endPoint("/media/{referenceType}/{referenceId}/file"))
                .then()
        );
    }


    public File fileUpload(RequestSpecification spec, ReferenceType referenceType, Integer referenceId, Function<ValidatableResponse, File> responseValidator) {
        final byte[] randomBytes = "random".getBytes();

        RequestSpecification requestSpecification = given().spec(spec)
                .contentType("multipart/form-data")
                .multiPart(new MultiPartSpecBuilder(randomBytes)
                        .fileName("test-file")
                        .controlName(FileController.MULTIFORM_FILE_IDENTIFIER)
                        .mimeType("text/plain").build())
                .multiPart("referenceType", referenceType);

        requestSpecification = referenceId != null ? requestSpecification.multiPart("referenceId", referenceId) : requestSpecification;

        return responseValidator.apply(requestSpecification
                .when().post("/api/rest/files").then());
    }
}
