package com.siryus.swisscon.api.base.helpers;

import com.siryus.swisscon.api.taskworklog.dto.EventHistoryDTO;
import com.siryus.swisscon.api.taskworklog.dto.MainTaskDurationDTO;
import com.siryus.swisscon.api.taskworklog.dto.RejectTaskRequest;
import com.siryus.swisscon.api.taskworklog.dto.SubTaskDurationDTO;
import com.siryus.swisscon.api.taskworklog.dto.TaskStatusDTO;
import com.siryus.swisscon.api.taskworklog.dto.TaskWorklogRequest;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.siryus.swisscon.api.base.AbstractMvcTestBase.endPoint;
import static io.restassured.RestAssured.given;

public class WorkLogs {
    public TaskStatusDTO startTimer(RequestSpecification spec, TaskWorklogRequest request) {
        return startTimer(
                spec, request,
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(TaskStatusDTO.class)
        );
    }
    public TaskStatusDTO startTimer(RequestSpecification spec, TaskWorklogRequest request, Function<ValidatableResponse, TaskStatusDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .body(request)
                        .post(endPoint("/work-log/start-timer"))
                        .then()
        );
    }

    public TaskStatusDTO stopTimer(RequestSpecification spec, TaskWorklogRequest request) {
        return stopTimer(
                spec, request,
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(TaskStatusDTO.class)
        );
    }
    public TaskStatusDTO stopTimer(RequestSpecification spec, TaskWorklogRequest request, Function<ValidatableResponse, TaskStatusDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .body(request)
                        .post(endPoint("/work-log/stop-timer"))
                        .then()
        );
    }

    public TaskStatusDTO completeSubTask(RequestSpecification spec, TaskWorklogRequest request) {
        return completeSubTask(
                spec, request,
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(TaskStatusDTO.class)
        );
    }
    public TaskStatusDTO completeSubTask(RequestSpecification spec, TaskWorklogRequest request, Function<ValidatableResponse, TaskStatusDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .body(request)
                        .post(endPoint("/work-log/complete-sub-task"))
                        .then()
        );
    }

    public TaskStatusDTO completeTask(RequestSpecification spec, TaskWorklogRequest request) {
        return completeTask(
                spec, request,
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(TaskStatusDTO.class)
        );
    }
    public TaskStatusDTO completeTask(RequestSpecification spec, TaskWorklogRequest request, Function<ValidatableResponse, TaskStatusDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .body(request)
                        .post(endPoint("/work-log/complete-task"))
                        .then()
        );
    }

    public TaskStatusDTO approveTask(RequestSpecification spec, TaskWorklogRequest request) {
        return approveTask(
                spec, request,
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(TaskStatusDTO.class)
        );
    }
    public TaskStatusDTO approveTask(RequestSpecification spec, TaskWorklogRequest request, Function<ValidatableResponse, TaskStatusDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .body(request)
                        .post(endPoint("/work-log/approve-task"))
                        .then()
        );
    }

    public TaskStatusDTO rejectTask(RequestSpecification spec, RejectTaskRequest request) {
        // if different responses are expected later, we can pass this in as a parameter
        Function<ValidatableResponse, TaskStatusDTO> responseValidator =
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(TaskStatusDTO.class);

        return responseValidator.apply(
                given()
                        .spec(spec)
                        .body(request)
                        .post(endPoint("/work-log/reject-task"))
                        .then()
        );
    }

    public TaskStatusDTO rejectTaskContractor(RequestSpecification spec, TaskWorklogRequest request) {
        return rejectTaskContractor(
                spec, request,
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(TaskStatusDTO.class)
        );
    }
    public TaskStatusDTO rejectTaskContractor(RequestSpecification spec, TaskWorklogRequest request, Function<ValidatableResponse, TaskStatusDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .body(request)
                        .post(endPoint("/work-log/reject-task-contractor"))
                        .then()
        );
    }

    public MainTaskDurationDTO getMainTaskDuration(RequestSpecification spec, Integer mainTaskId) {
        return getMainTaskDuration(
                spec, mainTaskId,
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(MainTaskDurationDTO.class)
        );
    }
    public MainTaskDurationDTO getMainTaskDuration(RequestSpecification spec, Integer mainTaskId, Function<ValidatableResponse, MainTaskDurationDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("mainTaskId", mainTaskId)
                        .get(endPoint("/work-log/task/{mainTaskId}"))
                        .then()
        );
    }

    public SubTaskDurationDTO getSubTaskDuration(RequestSpecification spec, Integer SubTaskId) {
        return getSubTaskDuration(
                spec, SubTaskId,
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(SubTaskDurationDTO.class)
        );
    }
    public SubTaskDurationDTO getSubTaskDuration(RequestSpecification spec, Integer SubTaskId, Function<ValidatableResponse, SubTaskDurationDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("subTaskId", SubTaskId)
                        .get(endPoint("/work-log/sub-task/{subTaskId}"))
                        .then()
        );
    }

    public SubTaskDurationDTO getSubTaskWorkerDuration(RequestSpecification spec, Integer subTaskId, Integer workerId) {
        return getSubTaskWorkerDuration(
                spec, subTaskId, workerId,
                r -> r.statusCode(HttpStatus.OK.value()).extract().as(SubTaskDurationDTO.class)
        );
    }

    public SubTaskDurationDTO getSubTaskWorkerDuration(RequestSpecification spec, Integer subTaskId, Integer workerId, Function<ValidatableResponse, SubTaskDurationDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .pathParam("subTaskId", subTaskId)
                        .pathParam("workerId", workerId)
                        .get(endPoint("/work-log/sub-task/{subTaskId}/worker/{workerId}"))
                        .then()
        );
    }
    public void cancelSubTaskTimerWorkLog(RequestSpecification spec, Integer subTaskId, Integer startTimerEventId ) {
        cancelSubTaskTimerWorkLog(spec, subTaskId, startTimerEventId,
                r -> r.assertThat().statusCode(HttpStatus.OK.value())
        );
    }

    public void cancelSubTaskTimerWorkLog(RequestSpecification spec, Integer subTaskId, Integer startTimerEventId, Consumer<ValidatableResponse> responseConsumer ) {
        responseConsumer.accept(
                given()
                        .spec(spec)
                        .pathParam("subTaskId", subTaskId)
                        .pathParam("startTimerEventId", startTimerEventId)
                        .post(endPoint("/work-log/sub-task/{subTaskId}/cancel-timer/{startTimerEventId}"))
                        .then()
        );
    }

    public List<EventHistoryDTO> mainTaskWorkLogHistory(RequestSpecification spec, Integer mainTaskId) {
        return given()
                .spec(spec)
                .pathParam("mainTaskId", mainTaskId)
                .get(endPoint("/work-log/task/{mainTaskId}/history"))
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .jsonPath()
                .getList("content", EventHistoryDTO.class);
    }
}
