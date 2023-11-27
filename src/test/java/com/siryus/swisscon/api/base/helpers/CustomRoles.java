package com.siryus.swisscon.api.base.helpers;

import com.siryus.swisscon.api.customroles.dto.CustomPermissionDTO;
import com.siryus.swisscon.api.customroles.dto.CustomRoleDTO;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.springframework.http.HttpStatus;

import java.util.function.Function;

import static com.siryus.swisscon.api.base.AbstractMvcTestBase.endPoint;
import static io.restassured.RestAssured.given;

public class CustomRoles {
    public CustomRoleDTO[] getRoles(RequestSpecification spec) {
        return getRoles(
                spec,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .as(CustomRoleDTO[].class)
        );
    }

    public CustomRoleDTO[] getRoles(
            RequestSpecification spec,
            Function<ValidatableResponse, CustomRoleDTO[]> responseValidator
    ) {
        return responseValidator.apply(
                given().spec(spec)
                        .when()
                        .get(endPoint("/custom-roles/roles"))
                        .then()
        );
    }

    public CustomPermissionDTO[] getPermissions(RequestSpecification spec) {
        return getPermissions(
                spec,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .as(CustomPermissionDTO[].class)
        );
    }

    public CustomPermissionDTO[] getPermissions(
            RequestSpecification spec,
            Function<ValidatableResponse, CustomPermissionDTO[]> responseValidator
    ) {
        return responseValidator.apply(
                given().spec(spec)
                        .when()
                        .get(endPoint("/custom-roles/permissions"))
                        .then()
        );
    }

    public CustomRoleDTO getRole(RequestSpecification spec, String roleName) {
        return getRole(spec, roleName,
                       r -> r.assertThat()
                               .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                               .extract()
                               .as(CustomRoleDTO.class)
        );
    }

    public CustomRoleDTO getRole(
            RequestSpecification spec,
            String roleName,
            Function<ValidatableResponse, CustomRoleDTO> responseValidator
    ) {
        return responseValidator.apply(
                given().spec(spec)
                        .when()
                        .pathParam("roleName", roleName)
                        .get(endPoint("/custom-roles/roles/{roleName}"))
                        .then()
        );
    }

    public CustomRoleDTO getRole(RequestSpecification spec, Integer roleId) {
        return getRole(spec, roleId,
                       r -> r.assertThat()
                               .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                               .extract()
                               .as(CustomRoleDTO.class)
        );
    }

    public CustomRoleDTO getRole(
            RequestSpecification spec,
            Integer roleId,
            Function<ValidatableResponse, CustomRoleDTO> responseValidator
    ) {
        return responseValidator.apply(
                given().spec(spec)
                        .when()
                        .pathParam("roleId", roleId)
                        .get(endPoint("/custom-roles/role/{roleId}"))
                        .then()
        );
    }

    public CustomRoleDTO createNewRole(RequestSpecification spec, CustomRoleDTO role) {
        return createNewRole(spec, role,
                             r -> r.assertThat()
                                     .statusCode(HttpStatus.OK.value())
                                     .extract()
                                     .as(CustomRoleDTO.class)
        );
    }

    public CustomRoleDTO createNewRole(
            RequestSpecification spec,
            CustomRoleDTO role,
            Function<ValidatableResponse, CustomRoleDTO> responseValidator
    ) {
        return responseValidator.apply(
                given().spec(spec)
                        .when()
                        .body(role)
                        .post(endPoint("/custom-roles/roles/new"))
                        .then()
        );
    }

    public CustomRoleDTO updateRole(RequestSpecification spec, CustomRoleDTO role) {
        return updateRole(spec, role,
                             r -> r.assertThat()
                                     .statusCode(HttpStatus.OK.value())
                                     .extract()
                                     .as(CustomRoleDTO.class)
        );
    }

    public CustomRoleDTO updateRole(
            RequestSpecification spec,
            CustomRoleDTO role,
            Function<ValidatableResponse, CustomRoleDTO> responseValidator
    ) {
        return responseValidator.apply(
                given().spec(spec)
                        .when()
                        .body(role)
                        .post(endPoint("/custom-roles/roles/update"))
                        .then()
        );
    }
    
    public CustomRoleDTO[] getUserProjectRoles(RequestSpecification spec, Integer projectId, Integer userId) {
        return getUserProjectRoles(
                spec,
                projectId, 
                userId,
                r -> r.assertThat()
                .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                .extract()
                .as(CustomRoleDTO[].class)
        );
    }

    public CustomRoleDTO[] getUserProjectRoles(RequestSpecification spec, Integer projectId, Integer userId, Function<ValidatableResponse, CustomRoleDTO[]> responseValidator) {
        return responseValidator.apply(
                given().spec(spec)
                        .when()
                        .pathParam("projectId", projectId)
                        .pathParam("userId", userId)
                        .get(endPoint("/custom-roles/user/{userId}/project/{projectId}/roles"))
                        .then()
        );
    }    
}
