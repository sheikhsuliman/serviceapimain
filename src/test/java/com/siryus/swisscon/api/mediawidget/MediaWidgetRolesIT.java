package com.siryus.swisscon.api.mediawidget;

import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestHelper.ExtendedTestProject;
import com.siryus.swisscon.api.customroles.dto.CustomPermissionDTO;
import com.siryus.swisscon.api.customroles.dto.CustomRoleDTO;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import io.restassured.specification.RequestSpecification;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static com.siryus.swisscon.api.base.TestBuilder.testCustomRole;

public class MediaWidgetRolesIT extends AbstractMvcTestBase {
    ExtendedTestProject testProject;        

    private CustomRoleDTO roleWithAllMediaPermissions;

    private Map<String, CustomPermissionDTO> mediaPermissions;

    private Integer parentFolderId;

    private RequestSpecification asCompanyBootstrap;
    
    private final String TEST_ROLE_NAME = "MW_TEST_ROLE";

    @BeforeAll
    public void setup() throws Exception {
        cleanDatabase();

        testProject = testHelper.createExtendedProject();
        
        parentFolderId = testHelper.createMediaWidgetFolder(
                testProject.contractorCompany.asWorker, ReferenceType.PROJECT, testProject.projectId, null, "TEST_PARENT_FOLDER",
                r -> { return r.assertThat().statusCode(HttpStatus.OK.value()).extract().as(MediaWidgetFileDTO.class); }
        ).getId();
        
        // Create a non-system role which has all media permissions
        mediaPermissions = Arrays.asList(testHelper.getPermissions(testProject.ownerCompany.asOwner))
                .stream()
                .filter(permission -> permission.getName().equals("MEDIA_CREATE") || 
                        permission.getName().equals("MEDIA_UPDATE") || 
                        permission.getName().equals("MEDIA_ARCHIVE"))
                .collect(Collectors.toMap(CustomPermissionDTO::getName, Function.identity()));

        roleWithAllMediaPermissions = testCustomRole(TEST_ROLE_NAME, 
                new ArrayList<>(mediaPermissions.values()), 
                r -> r.toBuilder().projectRole(true).build());
        
        // We need this spec for updating roles
        asCompanyBootstrap = testHelper.login("david.henkel@dt-systems.net", "cocoTest6");
        
        roleWithAllMediaPermissions = testHelper.createNewRole(asCompanyBootstrap, roleWithAllMediaPermissions);
        
        // Change worker role to this new role
        testHelper.changeProjectNonAdminRole(testProject.contractorCompany.asOwner, testProject.projectId, testProject.contractorCompany.workerId, roleWithAllMediaPermissions.getId());
    }

    @Test
    void Given_userInProject_When_hasCreatePermission_Then_createFileAndFolderSucceeds() {
        testHelper.updateRole(asCompanyBootstrap, roleWithAllMediaPermissions);        
        
        testHelper.createMediaWidgetFolder(
                testProject.contractorCompany.asWorker, ReferenceType.PROJECT, testProject.projectId, null, "TEST_FOLDER",
                r -> { r.assertThat().statusCode(HttpStatus.OK.value()); return null;} 
        );

        testHelper.createMediaWidgetFile(
                testProject.contractorCompany.asWorker, ReferenceType.PROJECT, testProject.projectId, parentFolderId, "TEST_FILE",
                r -> { r.assertThat().statusCode(HttpStatus.OK.value()); return null;} 
        );        
    }

    @Test
    void Given_userInProject_When_doesNotHaveCreatePermission_Then_createFileAndFolderFails() {               
        testHelper.updateRole(asCompanyBootstrap, roleWithoutPermission(roleWithAllMediaPermissions, mediaPermissions.get("MEDIA_CREATE")));

        testHelper.createMediaWidgetFolder(
                testProject.contractorCompany.asWorker, ReferenceType.PROJECT, testProject.projectId, null, "TEST_FOLDER_1",
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null;} 
        );

        testHelper.createMediaWidgetFile(
                testProject.contractorCompany.asWorker, ReferenceType.PROJECT, testProject.projectId, parentFolderId, "TEST_FILE_1",
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null;} 
        );      
    }

    @Test
    void Given_userInProject_When_hasUpdatePermission_Then_updateFileAndFolderSuccess() {
        testHelper.updateRole(asCompanyBootstrap, roleWithAllMediaPermissions);

        MediaWidgetFileDTO folder = testHelper.createMediaWidgetFolder(
                testProject.contractorCompany.asWorker, ReferenceType.PROJECT, testProject.projectId, null, "TEST_FOLDER_2",
                r -> { return r.assertThat().statusCode(HttpStatus.OK.value()).extract().as(MediaWidgetFileDTO.class); } 
        );

        MediaWidgetFileDTO file = testHelper.createMediaWidgetFile(
                testProject.contractorCompany.asWorker, ReferenceType.PROJECT, testProject.projectId, parentFolderId, "TEST_FILE_2",
                r -> { return r.assertThat().statusCode(HttpStatus.OK.value()).extract().as(MediaWidgetFileDTO.class); } 
        );      

        testHelper.updateMediaWidgetNode(testProject.contractorCompany.asWorker, folder.getId(), "TEST_FOLDER_TEST_UPDATE_2");

        testHelper.updateMediaWidgetNode(testProject.contractorCompany.asWorker, file.getId(), "TEST_FILE_TEST_UPDATE_2"); 
    }    

    @Test
    void Given_userInProject_When_doesNotHaveUpdatePermission_Then_updateFileAndFolderFails() {
        testHelper.updateRole(asCompanyBootstrap, roleWithAllMediaPermissions);

        MediaWidgetFileDTO folder = testHelper.createMediaWidgetFolder(
                testProject.contractorCompany.asWorker, ReferenceType.PROJECT, testProject.projectId, null, "TEST_FOLDER_3",
                r -> { return r.assertThat().statusCode(HttpStatus.OK.value()).extract().as(MediaWidgetFileDTO.class); } 
        );

        MediaWidgetFileDTO file = testHelper.createMediaWidgetFile(
                testProject.contractorCompany.asWorker, ReferenceType.PROJECT, testProject.projectId, parentFolderId, "TEST_FILE_3",
                r -> { return r.assertThat().statusCode(HttpStatus.OK.value()).extract().as(MediaWidgetFileDTO.class); } 
        );      

        testHelper.updateRole(asCompanyBootstrap, roleWithoutPermission(roleWithAllMediaPermissions, mediaPermissions.get("MEDIA_UPDATE")));

        testHelper.updateMediaWidgetNode(
                testProject.contractorCompany.asWorker, folder.getId(), "TEST_FOLDER_TEST_UPDATE_3",
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null;}
        );

        testHelper.updateMediaWidgetNode(
                testProject.contractorCompany.asWorker, file.getId(), "TEST_FILE_TEST_UPDATE_3",
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null;}                
        );
    }    

    @Test
    void Given_userInProject_When_hasDeletePermission_Then_deleteFileAndFolderSucceeds() {
        testHelper.updateRole(asCompanyBootstrap, roleWithAllMediaPermissions);

        MediaWidgetFileDTO folder = testHelper.createMediaWidgetFolder(
                testProject.contractorCompany.asWorker, ReferenceType.PROJECT, testProject.projectId, null, "TEST_FOLDER_4",
                r -> { return r.assertThat().statusCode(HttpStatus.OK.value()).extract().as(MediaWidgetFileDTO.class); } 
        );

        MediaWidgetFileDTO file = testHelper.createMediaWidgetFile(
                testProject.contractorCompany.asWorker, ReferenceType.PROJECT, testProject.projectId, parentFolderId, "TEST_FILE_4",
                r -> { return r.assertThat().statusCode(HttpStatus.OK.value()).extract().as(MediaWidgetFileDTO.class); } 
        );      

        testHelper.deleteMediaWidgetNode(testProject.contractorCompany.asWorker, folder.getId());

        testHelper.deleteMediaWidgetNode(testProject.contractorCompany.asWorker, file.getId());
    }

    @Test
    void Given_userInProject_When_doesNotHaveDeletePermission_Then_deleteFileAndFolderFails() {
        testHelper.updateRole(asCompanyBootstrap, roleWithAllMediaPermissions);

        MediaWidgetFileDTO folder = testHelper.createMediaWidgetFolder(
                testProject.contractorCompany.asWorker, ReferenceType.PROJECT, testProject.projectId, null, "TEST_FOLDER_5",
                r -> { return r.assertThat().statusCode(HttpStatus.OK.value()).extract().as(MediaWidgetFileDTO.class); } 
        );

        MediaWidgetFileDTO file = testHelper.createMediaWidgetFile(
                testProject.contractorCompany.asWorker, ReferenceType.PROJECT, testProject.projectId, parentFolderId, "TEST_FILE_5",
                r -> { return r.assertThat().statusCode(HttpStatus.OK.value()).extract().as(MediaWidgetFileDTO.class); } 
        );

        testHelper.updateRole(asCompanyBootstrap, roleWithoutPermission(roleWithAllMediaPermissions, mediaPermissions.get("MEDIA_ARCHIVE")));

        testHelper.deleteMediaWidgetNode(
                testProject.contractorCompany.asWorker, folder.getId(),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); }
        );

        testHelper.deleteMediaWidgetNode(
                testProject.contractorCompany.asWorker, file.getId(),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); }
        );
    } 

    private CustomRoleDTO roleWithoutPermission(CustomRoleDTO original, CustomPermissionDTO... permissionToRemove) {        
        Set<String> permissionNames = Arrays.asList(permissionToRemove).stream().map(element -> element.getName()).collect(Collectors.toSet());

        return CustomRoleDTO.builder()
            .adminRole(original.isAdminRole())
            .companyDefault(original.isCompanyDefault())
            .deprecated(original.isDeprecated())
            .description(original.getDescription())
            .id(original.getId())
            .memberDefault(original.isMemberDefault())
            .name(original.getName())
            .permissions(original.getPermissions().stream()
                    .filter(element -> !permissionNames.contains(element.getName()))
                    .collect(Collectors.toList()))
            .projectRole(original.isProjectRole())
            .systemRole(original.isSystemRole())
            .build();
    }
}