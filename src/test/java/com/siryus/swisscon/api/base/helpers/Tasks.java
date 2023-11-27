package com.siryus.swisscon.api.base.helpers;

import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.tasks.dto.AddTaskChecklistItemRequest;
import com.siryus.swisscon.api.tasks.dto.AssignCompanyToTaskRequest;
import com.siryus.swisscon.api.tasks.dto.CommentDTO;
import com.siryus.swisscon.api.tasks.dto.CreateCommentDTO;
import com.siryus.swisscon.api.tasks.dto.CreateContractualTaskRequest;
import com.siryus.swisscon.api.tasks.dto.CreateDirectorialTaskRequest;
import com.siryus.swisscon.api.tasks.dto.CreateSubTaskRequest;
import com.siryus.swisscon.api.tasks.dto.IdResponse;
import com.siryus.swisscon.api.tasks.dto.ListTaskIdsRequest;
import com.siryus.swisscon.api.tasks.dto.ListTasksRequest;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import com.siryus.swisscon.api.tasks.dto.SubTaskDTO;
import com.siryus.swisscon.api.tasks.dto.TaskChecklistItem;
import com.siryus.swisscon.api.tasks.dto.UpdateDirectorialTaskRequest;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.siryus.swisscon.api.base.AbstractMvcTestBase.endPoint;
import static io.restassured.RestAssured.given;

public class Tasks {
    public MainTaskDTO addContractualTask(RequestSpecification asAdmin, CreateContractualTaskRequest request){
        return addContractualTask(asAdmin, request,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .as(MainTaskDTO.class)
        );
    }
    public MainTaskDTO addContractualTask(RequestSpecification spec, CreateContractualTaskRequest request, Function<ValidatableResponse, MainTaskDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .body(request)
                        .post(endPoint("/tasks/contractual-task"))
                        .then()
        );
    }

    public MainTaskDTO addDirectorialTask(RequestSpecification asAdmin, CreateDirectorialTaskRequest request){
        return addDirectorialTask(asAdmin, request,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .as(MainTaskDTO.class)
        );
    }
    public MainTaskDTO addDirectorialTask(RequestSpecification spec, CreateDirectorialTaskRequest request, Function<ValidatableResponse, MainTaskDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .body(request)
                        .post(endPoint("/tasks/directorial-task"))
                        .then()
        );
    }

    public MainTaskDTO updateDirectorialTask(RequestSpecification asAdmin, Integer taskId, UpdateDirectorialTaskRequest request){
        return updateDirectorialTask(asAdmin, taskId, request,
                                  r -> r.assertThat()
                                          .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                                          .extract()
                                          .as(MainTaskDTO.class)
        );
    }
    public MainTaskDTO updateDirectorialTask(RequestSpecification spec, Integer taskId, UpdateDirectorialTaskRequest request, Function<ValidatableResponse, MainTaskDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .body(request)
                        .pathParam("taskId", taskId)
                        .post(endPoint("/tasks/directorial-task/{taskId}/update"))
                        .then()
        );
    }


    public MainTaskDTO getMainTask(RequestSpecification asAdmin, Integer taskId ){
        return getMainTask(asAdmin, taskId,
                           r -> r.assertThat()
                                             .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                                             .extract()
                                             .as(MainTaskDTO.class)
        );
    }
    public MainTaskDTO getMainTask(RequestSpecification spec, Integer taskId, Function<ValidatableResponse, MainTaskDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("taskId", taskId)
                        .get(endPoint("/tasks/task/{taskId}"))
                        .then()
        );
    }

    public SubTaskDTO addSubTask(RequestSpecification spec, CreateSubTaskRequest request) {
        return addSubTask(spec, request,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .as(SubTaskDTO.class)
        );
    }

    public SubTaskDTO addSubTask(RequestSpecification spec, CreateSubTaskRequest request, Function<ValidatableResponse, SubTaskDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .body(request)
                        .post(endPoint("/tasks/sub-task"))
                        .then()
        );
    }

    public void addUserToMainTask(RequestSpecification spec, Integer subTaskId, Integer workerId) {
        addUserToMainTask(spec, subTaskId, workerId,
                r -> r.assertThat().statusCode(Matchers.equalTo(HttpStatus.CREATED.value()))
        );
    }
    public void addUserToMainTask(RequestSpecification spec, Integer mainTaskId, Integer workerId, Consumer<ValidatableResponse> responseValidator ) {
        responseValidator.accept(
                given()
                        .spec(spec)
                        .pathParam("mainTaskId", mainTaskId)
                        .pathParam("workerId", workerId)
                        .post(endPoint("/tasks/task/{mainTaskId}/add-user/{workerId}"))
                        .then()
        );
    }

    public MainTaskDTO assignCompanyToMainTask(RequestSpecification spec, Integer mainTaskId, AssignCompanyToTaskRequest request ) {
        return assignCompanyToMainTask(spec, mainTaskId, request,
                                        r -> r.assertThat().statusCode(Matchers.equalTo(HttpStatus.OK.value())).extract().as(MainTaskDTO.class));
    }

    public MainTaskDTO assignCompanyToMainTask(RequestSpecification spec, Integer mainTaskId, AssignCompanyToTaskRequest request, Function<ValidatableResponse, MainTaskDTO> responseValidator ) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("mainTaskId", mainTaskId)
                        .body(request)
                        .post(endPoint("/tasks/task/{mainTaskId}/assign-company"))
                        .then()
        );
    }

    public MainTaskDTO unAssignCompanyFromMainTask(RequestSpecification spec, Integer mainTaskId ) {
        return unAssignCompanyFromMainTask(spec, mainTaskId,
                       r -> r.assertThat().statusCode(Matchers.equalTo(HttpStatus.OK.value())).extract().as(MainTaskDTO.class));
    }

    public MainTaskDTO unAssignCompanyFromMainTask(RequestSpecification spec, Integer mainTaskId, Function<ValidatableResponse, MainTaskDTO> responseValidator ) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("mainTaskId", mainTaskId)
                        .post(endPoint("/tasks/task/{mainTaskId}/un-assign-company"))
                        .then()
        );
    }

    public List<TeamUserDTO> getSubTaskTeam(RequestSpecification spec, Integer subTaskId) {
        return getSubTaskTeam(
                spec, subTaskId,
                r -> r.statusCode(HttpStatus.OK.value()).extract()
                        .jsonPath()
                        .getList(".", TeamUserDTO.class)
        );
    }

    public List<TeamUserDTO> getSubTaskTeam(RequestSpecification spec, Integer subTaskId, Function<ValidatableResponse, List<TeamUserDTO>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("subTaskId", subTaskId)
                        .get(endPoint("/tasks/sub-task/{subTaskId}/team"))
                        .then()
        );
    }

    public List<TeamUserDTO> getMainTaskTeam(RequestSpecification spec, Integer mainTaskId) {
        return getMainTaskTeam(
                spec, mainTaskId,
                r -> r.statusCode(HttpStatus.OK.value()).extract()
                        .jsonPath()
                        .getList(".", TeamUserDTO.class)
        );
    }

    public List<TeamUserDTO> getMainTaskTeam(RequestSpecification spec, Integer mainTaskId, Function<ValidatableResponse, List<TeamUserDTO>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("mainTaskId", mainTaskId)
                        .get(endPoint("/tasks/task/{mainTaskId}/team"))
                        .then()
        );
    }

    public void removeUserFromMainTask(RequestSpecification spec, Integer mainTask, Integer workerId) {
        removeUserFromMainTask(spec, mainTask, workerId,
                r -> r.assertThat().statusCode(Matchers.equalTo(HttpStatus.OK.value()))
        );
    }
    public void removeUserFromMainTask(RequestSpecification spec, Integer mainTaskId, Integer workerId, Consumer<ValidatableResponse> responseValidator ) {
        responseValidator.accept(
                given()
                        .spec(spec)
                        .pathParam("mainTaskId", mainTaskId)
                        .queryParam("user", workerId)
                        .post(endPoint("/tasks/task/{mainTaskId}/remove-user"))
                        .then()
        );
    }

    public void addUserToSubTask(RequestSpecification spec, Integer subTaskId, Integer workerId) {
        addUserToSubTask(spec, subTaskId, workerId,
                r -> r.assertThat().statusCode(Matchers.equalTo(HttpStatus.CREATED.value()))
        );
    }
    public void addUserToSubTask(RequestSpecification spec, Integer subTaskId, Integer workerId, Consumer<ValidatableResponse> responseValidator ) {
        responseValidator.accept(
                given()
                        .spec(spec)
                        .pathParam("subTaskId", subTaskId)
                        .pathParam("workerId", workerId)
                        .post(endPoint("/tasks/sub-task/{subTaskId}/add-user/{workerId}"))
                        .then()
        );
    }

    public IdResponse addMainTaskChecklistItem(RequestSpecification spec, Integer mainTaskId, AddTaskChecklistItemRequest request) {
        return addMainTaskChecklistItem(
                spec, mainTaskId, request,
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(IdResponse.class)
        );
    }
    public IdResponse addMainTaskChecklistItem(RequestSpecification spec, Integer mainTaskId, AddTaskChecklistItemRequest request, Function<ValidatableResponse, IdResponse> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .body(request)
                        .pathParam("mainTaskId", mainTaskId)
                        .post(endPoint("/tasks/check-list/main-task/{mainTaskId}/add"))
                        .then()
        );
    }

    public IdResponse addSubTaskChecklistItem(RequestSpecification spec, Integer subTaskId) {
        return addSubTaskChecklistItem(
                spec, subTaskId,
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(IdResponse.class)
        );
    }
    public IdResponse addSubTaskChecklistItem(RequestSpecification spec, Integer subTaskId, Function<ValidatableResponse, IdResponse> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("subTaskId", subTaskId)
                        .post(endPoint("/tasks/check-list/sub-task/{subTaskId}/add"))
                        .then()
        );
    }

    public TaskChecklistItem[] getMainTaskCheckList(RequestSpecification spec, Integer mainTaskId) {
        return getMainTaskCheckList(
                spec, mainTaskId,
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(TaskChecklistItem[].class)
        );
    }
    public TaskChecklistItem[] getMainTaskCheckList(RequestSpecification spec, Integer mainTaskId, Function<ValidatableResponse, TaskChecklistItem[]> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("mainTaskId", mainTaskId)
                        .get(endPoint("/tasks/check-list/main-task/{mainTaskId}"))
                        .then()
        );
    }

    public TaskChecklistItem[] getSubTaskCheckList(RequestSpecification spec, Integer subTaskId) {
        return getSubTaskCheckList(
                spec, subTaskId,
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(TaskChecklistItem[].class)
        );
    }

    public TaskChecklistItem[] getSubTaskCheckList(RequestSpecification spec, Integer subTaskId, Function<ValidatableResponse, TaskChecklistItem[]> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("subTaskId", subTaskId)
                        .get(endPoint("/tasks/check-list/sub-task/{subTaskId}"))
                        .then()
        );
    }

    public void onCheckListItem(RequestSpecification spec, Integer checklistItemId) {
        onCheckListItem(
                spec, checklistItemId,
                r -> r.assertThat().statusCode(Matchers.equalTo(HttpStatus.OK.value()))
        );
    }
    public void onCheckListItem(RequestSpecification spec, Integer checklistItemId, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(
                given()
                        .spec(spec)
                        .pathParam("checklistItemId", checklistItemId)
                        .post(endPoint("/tasks/check-list/{checklistItemId}/on"))
                        .then()
        );
    }

    public void addCommentToSubTask(RequestSpecification spec, Integer subTaskId, CreateCommentDTO request) {
        addCommentToSubTask(
                spec, subTaskId, request,
                r -> r.assertThat().statusCode(Matchers.equalTo(HttpStatus.CREATED.value()))
        );
    }

    public void addCommentToSubTask(RequestSpecification spec, Integer subTaskId, CreateCommentDTO request, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(
                given()
                        .spec(spec)
                        .body(request)
                        .pathParam("subTaskId", subTaskId)
                        .post(endPoint("/tasks/sub-task/{subTaskId}/add-comment"))
                        .then()
        );
    }

    public CommentDTO[] getSubTaskComments(RequestSpecification spec, Integer subTaskId) {
        return getSubTaskComments(
                spec, subTaskId,
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(CommentDTO[].class)
        );
    }
    public CommentDTO[] getSubTaskComments(RequestSpecification spec, Integer subTaskId, Function<ValidatableResponse, CommentDTO[]> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("subTaskId", subTaskId)
                        .get(endPoint("/tasks/sub-task/{subTaskId}/comments"))
                        .then()
        );
    }
    public CommentDTO updateSubTaskComment(RequestSpecification spec, Integer commentId, CreateCommentDTO request) {
        return updateSubTaskComment(
                spec, commentId, request,
                r -> r.assertThat().statusCode(Matchers.equalTo(HttpStatus.OK.value())).extract().as(CommentDTO.class)
        );
    }
    public CommentDTO updateSubTaskComment(RequestSpecification spec, Integer commentId, CreateCommentDTO request, Function<ValidatableResponse, CommentDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("commentId", commentId)
                        .body(request)
                        .post(endPoint("/tasks/comment/{commentId}/update"))
                        .then()
        );
    }

    public void archiveSubTaskComment(RequestSpecification spec, Integer commentId) {
        archiveSubTaskComment(
                spec, commentId,
                r -> r.assertThat().statusCode(Matchers.equalTo(HttpStatus.OK.value()))
        );
    }
    public void archiveSubTaskComment(RequestSpecification spec, Integer commentId, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(
                given()
                        .spec(spec)
                        .pathParam("commentId", commentId)
                        .post(endPoint("/tasks/comment/{commentId}/archive"))
                        .then()
        );
    }
    
    public MainTaskDTO[] listMainTasksForProject(RequestSpecification spec, Integer projectId, ListTasksRequest request) {
        return listMainTasksForProject(
                spec, projectId, request,
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(MainTaskDTO[].class)
        );
    }
    public MainTaskDTO[] listMainTasksForProject(RequestSpecification spec, Integer projectId, ListTasksRequest request, Function<ValidatableResponse, MainTaskDTO[]> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .body(request)
                        .pathParam("projectId", projectId)
                        .post(endPoint("/tasks/project/{projectId}/tasks"))
                        .then()
        );
    }

    public Map<Integer, List<Integer>> listMainAndSubTasksForProject(RequestSpecification spec, Integer projectId, ListTaskIdsRequest request) {
        return listMainAndSubTasksForProject(
                spec, projectId, request,
                r -> r.statusCode(HttpStatus.OK.value()).extract().jsonPath().getMap("."));
    }

    public Map<Integer, List<Integer>> listMainAndSubTasksForProject(RequestSpecification spec, Integer projectId, ListTaskIdsRequest request, Function<ValidatableResponse, Map<Integer, List<Integer>>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("projectId", projectId)
                        .body(request)
                        .post(endPoint("/tasks/project/{projectId}/task-and-sub-task-ids"))
                        .then()
        );
    }

    public MainTaskDTO[] listMainTasksForLocation(RequestSpecification spec, Integer locationId) {
        return listMainTasksForLocation(
                spec, locationId,
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(MainTaskDTO[].class)
        );
    }
    public MainTaskDTO[] listMainTasksForLocation(RequestSpecification spec, Integer locationId, Function<ValidatableResponse, MainTaskDTO[]> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("locationId", locationId)
                        .get(endPoint("/tasks/location/{locationId}/tasks"))
                        .then()
        );
    }

    public Map<Integer, List<Integer>> listMainAndSubTasksForLocations(RequestSpecification spec, Integer locationId, ListTaskIdsRequest request) {
        return listMainAndSubTasksForLocations(
                spec, locationId, request,
                r -> r.statusCode(HttpStatus.OK.value()).extract().jsonPath().getMap("."));
    }

    public Map<Integer, List<Integer>> listMainAndSubTasksForLocations(RequestSpecification spec, Integer locationId, ListTaskIdsRequest request, Function<ValidatableResponse, Map<Integer, List<Integer>>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("locationId", locationId)
                        .body(request)
                        .post(endPoint("/tasks/location/{locationId}/task-and-sub-task-ids"))
                        .then()
        );
    }

    public List<Integer> listSubTaskIds(RequestSpecification spec, Integer mainTaskId) {
        return listSubTaskIds(
                spec, mainTaskId,
                r -> r.statusCode(HttpStatus.OK.value()).extract().jsonPath().getList("."));
    }

    public List<Integer> listSubTaskIds(RequestSpecification spec, Integer mainTaskId, Function<ValidatableResponse, List<Integer>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("mainTaskId", mainTaskId)
                        .get(endPoint("/tasks/task/{mainTaskId}/sub-task-ids"))
                        .then()
        );
    }

    public List<TeamUserDTO> listMainTaskAvailableUsers(RequestSpecification spec, Integer taskId) {
        return listMainTaskAvailableUsers(spec, taskId, r -> r.assertThat().statusCode(HttpStatus.OK.value()).extract()
                .jsonPath().getList(".", TeamUserDTO.class));
    }

    public List<TeamUserDTO> listMainTaskAvailableUsers(RequestSpecification spec, Integer taskId, Function<ValidatableResponse, List<TeamUserDTO>> responseValidator) {
        return responseValidator.apply(
                given()
                    .spec(spec)
                .pathParam("taskId", taskId)
                .get(endPoint("/tasks/task/{taskId}/available-users"))
                .then()
        );
    }

    public List<TeamUserDTO> listMainTaskAvailableUsersForCreation(RequestSpecification spec, Integer projectId, Integer companyId) {
        return listMainTaskAvailableUsersForCreation(spec, projectId, companyId, r -> r.assertThat().statusCode(HttpStatus.OK.value()).extract()
                .jsonPath().getList(".", TeamUserDTO.class));
    }

    public List<TeamUserDTO> listMainTaskAvailableUsersForCreation(RequestSpecification spec, Integer projectId, Integer companyId, Function<ValidatableResponse, List<TeamUserDTO>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .queryParam("project", projectId)
                        .queryParam("company", companyId)
                        .get(endPoint("/tasks/task/available-users-for-create"))
                        .then()
        );
    }

    public List<TeamUserDTO> listSubTaskAvailableUsers(RequestSpecification spec, Integer subTaskId) {
        return listSubTaskAvailableUsers(spec, subTaskId, r -> r.assertThat().statusCode(HttpStatus.OK.value()).extract()
                .jsonPath().getList(".", TeamUserDTO.class));
    }

    public List<TeamUserDTO> listSubTaskAvailableUsers(RequestSpecification spec, Integer subTaskId, Function<ValidatableResponse, List<TeamUserDTO>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("subTaskId", subTaskId)
                        .get(endPoint("/tasks/sub-task/{subTaskId}/available-users"))
                        .then()
        );
    }
}
