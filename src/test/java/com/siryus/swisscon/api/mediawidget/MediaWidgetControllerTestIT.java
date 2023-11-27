package com.siryus.swisscon.api.mediawidget;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestHelper.TestProject;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static io.restassured.RestAssured.given;
import io.restassured.specification.RequestSpecification;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MediaWidgetControllerTestIT extends AbstractMvcTestBase {

    private static TestProject testProject;

    private static final String PATH = BASE_PATH + "/media/";

    @BeforeAll
    public void initTest() {
        testProject = testHelper.createProject();
    }

    @Test
    void Given_rootFolderWithDuplicatedName_When_createNewFolder_Then_addIncrementalNumberForFolderName() {
        MediaWidgetFileDTO folderA = createFolder(testProject.ownerCompany.asOwner, ReferenceType.PROJECT, testProject.ownerCompany.projectId, null,"unique-folder");
        MediaWidgetFileDTO folderA1 = createFolder(testProject.ownerCompany.asOwner, ReferenceType.PROJECT, testProject.ownerCompany.projectId, null,"unique-folder");

        assertEquals("unique-folder", folderA.getFilename());
        assertEquals("unique-folder(1)", folderA1.getFilename());
    }

    @Test
    void Given_rootFileWithDuplicatedName_When_uploadFile_Then_addIncrementalNumberForFolderName() {
        MediaWidgetFileDTO file1 = createFile(testProject.ownerCompany.asOwner, ReferenceType.PROJECT, testProject.ownerCompany.projectId, null, "root file abc.pdf");
        MediaWidgetFileDTO file2 = createFile(testProject.ownerCompany.asOwner, ReferenceType.PROJECT, testProject.ownerCompany.projectId, null, "root file abc.pdf");

        assertEquals("root file abc.pdf", file1.getFilename());
        assertEquals("root file abc(1).pdf", file2.getFilename());
    }
    
    @Test
    void Given_project_When_createAndListFolders_Then_resultsAreCorrect() {
        List<MediaWidgetFileDTO> r0 = listNodes(ReferenceType.PROJECT, testProject.ownerCompany.projectId, null);
        assertNotNull(r0);

        createFolder(testProject.ownerCompany.asOwner, ReferenceType.PROJECT, testProject.ownerCompany.projectId, null,"folder-a");
        List<MediaWidgetFileDTO> r1_1 = listNodes(ReferenceType.PROJECT, testProject.ownerCompany.projectId, null);
        MediaWidgetFileDTO folderA = findFile("folder-a", r1_1);

        assertMediaWidgetFile(folderA, ReferenceType.PROJECT, testProject.ownerCompany.projectId, "folder-a", true, true );

        createFolder(testProject.ownerCompany.asOwner, ReferenceType.PROJECT, testProject.ownerCompany.projectId, null,"folder-b");
        List<MediaWidgetFileDTO> r2 = listNodes(ReferenceType.PROJECT, testProject.ownerCompany.projectId, null);
        MediaWidgetFileDTO folderB = findFile("folder-b", r2);

        MediaWidgetFileDTO folderA1 = createFolder(testProject.ownerCompany.asOwner, ReferenceType.PROJECT, testProject.ownerCompany.projectId, folderA.getId()    , "folder-a-1");
        List<MediaWidgetFileDTO> r3 = listNodes(ReferenceType.PROJECT, testProject.ownerCompany.projectId, null);

        MediaWidgetFileDTO folderAFromR3 = findFile("folder-a", r3);
        MediaWidgetFileDTO folderBFromR3 = findFile("folder-b", r3);

        assertMediaWidgetFile(folderAFromR3, ReferenceType.PROJECT, testProject.ownerCompany.projectId, "folder-a", false, false );
        assertMediaWidgetFile(folderBFromR3, ReferenceType.PROJECT, testProject.ownerCompany.projectId, "folder-b", true, true );

        List<MediaWidgetFileDTO> r3_1 = listNodes(ReferenceType.PROJECT, testProject.ownerCompany.projectId, folderA.getId());
        assertEquals(1, r3_1.size());

        assertMediaWidgetFile(r3_1.get(0), ReferenceType.PROJECT, testProject.ownerCompany.projectId, "folder-a-1", true, true );

        updateFolder(folderA1.getId(), "folder-a-1-1");

        List<MediaWidgetFileDTO> r4 = listNodes(ReferenceType.PROJECT, testProject.ownerCompany.projectId, folderA.getId());
        assertEquals(1, r4.size());

        assertMediaWidgetFile(r4.get(0), ReferenceType.PROJECT, testProject.ownerCompany.projectId, "folder-a-1-1", true, true );

        deleteFolder(folderA.getId(), HttpStatus.CONFLICT);
        deleteFolder(folderB.getId(), HttpStatus.OK);
        deleteFolder(folderA1.getId(), HttpStatus.OK);
        deleteFolder(folderA.getId(), HttpStatus.OK);
    }
    
    private MediaWidgetFileDTO createFolder(RequestSpecification spec, ReferenceType referenceType, Integer referenceId, Integer parentNodeId, String name) {
        return testHelper.createMediaWidgetFolder(spec, referenceType, referenceId, parentNodeId, name, 
                r -> r.assertThat().statusCode(HttpStatus.OK.value()).extract().as(MediaWidgetFileDTO.class)
            );
    }
    
    private MediaWidgetFileDTO createFile(RequestSpecification spec, ReferenceType referenceType, Integer referenceId, Integer parentNodeId, String name) {
        return testHelper.createMediaWidgetFile(spec, referenceType, referenceId, parentNodeId, name, 
                r -> r.assertThat().statusCode(HttpStatus.OK.value()).extract().as(MediaWidgetFileDTO.class)
            );
    }
        
    private MediaWidgetFileDTO updateFolder(Integer nodeId, String name) {
        UpdateFileRequest request = new UpdateFileRequest(nodeId, name);
        return getResponse(testProject.ownerCompany.asOwner, PATH + "folder/update", request)
                .as(MediaWidgetFileDTO.class);
    }

    private void deleteFolder(Integer nodeId, HttpStatus expectedStatus) {
        DeleteFileRequest request = new DeleteFileRequest(nodeId);
        given().spec(testProject.ownerCompany.asOwner)
                .body(request)
                .post(PATH + "folder/delete" )
                .then()
                .assertThat()
                .statusCode(equalTo(expectedStatus.value()));
    }

    private MediaWidgetFileDTO findFile(String filename, List<MediaWidgetFileDTO> result) {
        return result.stream().filter(dto -> dto.getFilename().equals(filename))
                .findFirst().orElseThrow();
    }

    private List<MediaWidgetFileDTO> listNodes(ReferenceType referenceType, Integer referenceId, Integer parentNodeId) {
        return getResponse(testProject.ownerCompany.asOwner, PATH + referenceType.name() + "/" + referenceId + (parentNodeId == null ? "" : "?parentNodeId=" + parentNodeId))
                    .body()
                    .jsonPath().getList("$", MediaWidgetFileDTO.class);
    }

    private void assertMediaWidgetFile(MediaWidgetFileDTO file, ReferenceType referenceType, Integer referenceId, String name, boolean leaf, boolean empty) {
        assertEquals(referenceType.name(), file.getReferenceType().name());
        assertEquals(referenceId, file.getReferenceId());
        assertEquals(name, file.getFilename());
        assertEquals(leaf, file.isLeaf());
        assertEquals(empty, file.isEmpty());
    }

}
