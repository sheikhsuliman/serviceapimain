package com.siryus.swisscon.api.base.helpers;

import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.company.company.CompanyDirectoryDTO;
import com.siryus.swisscon.api.company.company.SimpleCompanyDTO;
import com.siryus.swisscon.api.project.project.EditProjectDTO;
import com.siryus.swisscon.api.project.project.NewProjectDTO;
import com.siryus.swisscon.api.project.project.ProjectBoardDTO;
import com.siryus.swisscon.api.project.project.ProjectDTO;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.siryus.swisscon.api.base.AbstractMvcTestBase.endPoint;
import static io.restassured.RestAssured.given;

public class Projects {
    private final AbstractMvcTestBase testBase;

    public Projects(AbstractMvcTestBase testBase) {
        this.testBase = testBase;
    }

    public ProjectBoardDTO createProject(RequestSpecification spec, NewProjectDTO request) {
        return createProject(spec, request,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .as(ProjectBoardDTO.class)
        );
    }
    public ProjectBoardDTO createProject(RequestSpecification spec, NewProjectDTO request, Function<ValidatableResponse, ProjectBoardDTO> responseValidator) {
        return responseValidator.apply(given()
                .spec(spec)
                .body(request)
                .post(endPoint("/projects/create"))
                .then()
        );
    }

    public ProjectBoardDTO editProject(RequestSpecification spec, Integer projectId, EditProjectDTO request) {
        return editProject(spec, projectId, request,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .as(ProjectBoardDTO.class)
        );
    }

    public ProjectBoardDTO editProject(RequestSpecification spec, Integer projectId, EditProjectDTO request, Function<ValidatableResponse, ProjectBoardDTO> responseValidator) {
        return responseValidator.apply(given()
                .spec(spec)
                .body(request)
                .pathParam("id", projectId)
                .post(endPoint("/projects/{id}/edit"))
                .then()
        );
    }

    public TeamUserDTO addUserToProject(RequestSpecification spec, Integer projectId, Integer userId) {
        return addUserToProject(spec, projectId, userId,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .as(TeamUserDTO.class)
        );
    }

    public void archiveProject(RequestSpecification spec, Integer projectId) {
        archiveProject(
                spec,
                projectId,
                r -> r.assertThat().statusCode(Matchers.equalTo(HttpStatus.NO_CONTENT.value()))
        );
    }

    public void archiveProject(RequestSpecification spec, Integer projectId, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(
                given()
                        .spec(spec)
                        .pathParam("projectId", projectId)
                        .post(endPoint("/projects/{projectId}/archive"))
                        .then()
        );
    }

    public void restoreProject(RequestSpecification spec, Integer projectId) {
        restoreProject(
                spec,
                projectId,
                r -> r.assertThat().statusCode(Matchers.equalTo(HttpStatus.NO_CONTENT.value()))
        );
    }

    public void restoreProject(RequestSpecification spec, Integer projectId, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(
                given()
                        .spec(spec)
                        .pathParam("projectId", projectId)
                        .post(endPoint("/projects/{projectId}/restore"))
                        .then()
        );
    }

    public TeamUserDTO addUserToProject(RequestSpecification spec, Integer projectId, Integer userId, Function<ValidatableResponse, TeamUserDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .queryParam("user", userId)
                        .pathParam("id", projectId)
                        .post(endPoint("/projects/{id}/add-user"))
                        .then()
        );
    }

    public void removeUserFromProject(RequestSpecification spec, Integer projectId, Integer userId) {
        removeUserFromProject(spec, projectId, userId,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
        );
    }

    public void removeUserFromProject(RequestSpecification spec, Integer projectId, Integer userId, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(
                given()
                        .spec(spec)
                        .queryParam("user", userId)
                        .pathParam("id", projectId)
                        .post(endPoint("/projects/{id}/remove-user"))
                        .then()
        );
    }

    public TeamUserDTO changeAdminRole(RequestSpecification spec, Integer projectId, Integer userId, Integer roleId) {
        return changeAdminRole(spec, projectId, userId, roleId,
                r ->  r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .as(TeamUserDTO.class)
        );
    }
    public TeamUserDTO changeAdminRole(RequestSpecification spec, Integer projectId, Integer userId, Integer roleId, Function<ValidatableResponse, TeamUserDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .queryParam("user", userId)
                        .queryParam("role", roleId)
                        .pathParam("id", projectId)
                        .post(endPoint("/projects/{id}/change-admin-role"))
                        .then()
        );
    }

    public TeamUserDTO changeNonAdminRole(RequestSpecification spec, Integer projectId, Integer userId, Integer roleId) {
        return changeNonAdminRole(spec, projectId, userId, roleId,
                r ->  r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .as(TeamUserDTO.class)
        );
    }
    public TeamUserDTO changeNonAdminRole(RequestSpecification spec, Integer projectId, Integer userId, Integer roleId, Function<ValidatableResponse, TeamUserDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .queryParam("user", userId)
                        .queryParam("role", roleId)
                        .pathParam("id", projectId)
                        .post(endPoint("/projects/{id}/change-non-admin-role"))
                        .then()
        );
    }

    public List<TeamUserDTO> getAvailableUsersToAddToProject(RequestSpecification spec, Integer projectId) {
        return getAvailableUsersToAddToProject(spec, projectId,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .body()
                        .jsonPath()
                        .getList(".", TeamUserDTO.class)
        );
    }
    public List<TeamUserDTO> getAvailableUsersToAddToProject(RequestSpecification spec, Integer projectId, Function<ValidatableResponse, List<TeamUserDTO>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("id", projectId)
                        .get(endPoint("/projects/{id}/available-users"))
                        .then()
        );
    }

    public List<SimpleCompanyDTO> getAvailableCompaniesToAddToProject(RequestSpecification spec, Integer projectId) {
        return getAvailableCompaniesToAddToProject(spec, projectId,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .body()
                        .jsonPath()
                        .getList(".", SimpleCompanyDTO.class)
        );
    }
    public List<SimpleCompanyDTO> getAvailableCompaniesToAddToProject(RequestSpecification spec, Integer projectId, Function<ValidatableResponse, List<SimpleCompanyDTO>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("id", projectId)
                        .get(endPoint("/projects/{id}/available-companies"))
                        .then()
        );
    }

    public List<CompanyDirectoryDTO> getProjectCompanies(RequestSpecification spec, Integer projectId) {
        return getProjectCompanies(spec, projectId,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .body()
                        .jsonPath()
                        .getList(".", CompanyDirectoryDTO.class)
        );
    }
    public List<CompanyDirectoryDTO> getProjectCompanies(RequestSpecification spec, Integer projectId, Function<ValidatableResponse, List<CompanyDirectoryDTO>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("id", projectId)
                        .get(endPoint("/projects/{id}/companies"))
                        .then()
        );
    }

    public CompanyDirectoryDTO addCompanyToProject(RequestSpecification spec, Integer projectId, Integer companyId, Integer roleId) {
        return addCompanyToProject(spec, projectId, companyId, roleId,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .as(CompanyDirectoryDTO.class)
        );
    }
    public CompanyDirectoryDTO addCompanyToProject(RequestSpecification spec, Integer projectId, Integer companyId, Integer roleId, Function<ValidatableResponse, CompanyDirectoryDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .queryParam("company", companyId)
                        .queryParam("roleId", roleId)
                        .pathParam("id", projectId)
                        .post(endPoint("/projects/{id}/add-company"))
                        .then()
        );
    }

    public void removeCompanyFromProject(RequestSpecification spec, Integer projectId, Integer companyId) {
        removeCompanyFromProject(spec, projectId, companyId,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
        );
    }

    public void removeCompanyFromProject(RequestSpecification spec, Integer projectId, Integer companyId, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(
                given()
                        .spec(spec)
                        .queryParam("company", companyId)
                        .pathParam("id", projectId)
                        .post(endPoint("/projects/{id}/remove-company"))
                        .then()
        );
    }

    public CompanyDirectoryDTO inviteCompanyToProject(RequestSpecification spec, Integer projectId, Integer companyId, Function<ValidatableResponse, CompanyDirectoryDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .queryParam("company", companyId)
                        .post(endPoint("/projects/{id}/add-company"), projectId)
                        .then()
        );
    }
    public CompanyDirectoryDTO inviteCompanyToProject(RequestSpecification spec, Integer projectId, Integer companyId) {
        return inviteCompanyToProject(spec, projectId, companyId,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .as(CompanyDirectoryDTO.class)
        );
    }

    public void assignCustomerToProject(RequestSpecification spec, Integer projectId, Integer customerCompanyId) {
        assignCustomerToProject(spec, projectId, customerCompanyId,
                r -> r.assertThat().statusCode(Matchers.equalTo(HttpStatus.OK.value())));
    }

    public void assignCustomerToProject(RequestSpecification spec, Integer projectId, Integer customerCompanyId, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(
                given()
                        .spec(spec)
                        .pathParam("customerCompanyId", customerCompanyId)
                        .pathParam("projectId", projectId)
                        .post(endPoint("/projects/{projectId}/customer/{customerCompanyId}"))
                        .then());
    }

    public TeamUserDTO[] getProjectTeam(RequestSpecification spec, Integer projectId, Integer companyId) {
        return getProjectTeam(
                spec, projectId, companyId,
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(TeamUserDTO[].class)
        );

    }
    public TeamUserDTO[] getProjectTeam(RequestSpecification spec, Integer projectId, Integer companyId, Function<ValidatableResponse, TeamUserDTO[]> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("projectId", projectId)
                        .queryParam("company", companyId)
                        .get(endPoint("/projects/{projectId}/team"))
                        .then()
        );
    }

    @SuppressWarnings("unchecked")
    public List<ProjectDTO> getProjects(RequestSpecification spec) {
        return getProjects(spec,
                r -> testBase.extractListInPage(
                        r.assertThat()
                                .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                                .extract().body().jsonPath(), ProjectDTO.class)
        );
    }

    public List<ProjectDTO> getProjects(RequestSpecification spec, Function<ValidatableResponse, List<ProjectDTO>> responseValidator) {
        return responseValidator.apply(given()
                .spec(spec).get(endPoint("/projects"))
                .then());
    }

    public List<ProjectDTO> getArchivedProjects(RequestSpecification spec) {
        return getArchivedProjects(spec,
                           r -> testBase.extractListInPage(
                                   r.assertThat()
                                           .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                                           .extract().body().jsonPath(), ProjectDTO.class)
        );
    }

    public List<ProjectDTO> getArchivedProjects(RequestSpecification spec, Function<ValidatableResponse, List<ProjectDTO>> responseValidator) {
        return responseValidator.apply(given()
                                               .spec(spec).get(endPoint("/archived-projects"))
                                               .then());
    }

    public ProjectBoardDTO getProjectBoard(RequestSpecification spec, Integer projectId) {
        return getProjectBoard(spec, projectId,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .as(ProjectBoardDTO.class));
    }

    public ProjectBoardDTO getProjectBoard(RequestSpecification spec, Integer projectId, Function<ValidatableResponse, ProjectBoardDTO> responseValidator) {
        return responseValidator.apply(given()
                .spec(spec)
                .pathParam("id", projectId)
                .get(endPoint("/project-board/{id}"))
                .then());
    }

    public ProjectDTO getProject(RequestSpecification spec, Integer projectId) {
        return getProject(spec, projectId,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .as(ProjectDTO.class));
    }

    public ProjectDTO getProject(RequestSpecification spec, Integer projectId, Function<ValidatableResponse, ProjectDTO> responseValidator) {
        return responseValidator.apply(given()
                .spec(spec)
                .pathParam("id", projectId)
                .get(endPoint("/projects/{id}"))
                .then());
    }

    public List<Integer> getPermissions(RequestSpecification spec, Integer projectId) {
        return getPermissions(spec, projectId,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .body()
                        .jsonPath()
                        .getList(".", Integer.class));
    }

    public List<Integer> getPermissions(RequestSpecification spec, Integer projectId, Function<ValidatableResponse, List<Integer>> responseValidator) {
        return responseValidator.apply(given()
                .spec(spec)
                .pathParam("id", projectId)
                .get(endPoint("/projects/{id}/permissions"))
                .then());
    }

}
