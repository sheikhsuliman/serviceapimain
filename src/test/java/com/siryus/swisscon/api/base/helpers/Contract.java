package com.siryus.swisscon.api.base.helpers;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.contract.dto.ContractAddTasksRequest;
import com.siryus.swisscon.api.contract.dto.ContractCommentDTO;
import com.siryus.swisscon.api.contract.dto.ContractDTO;
import com.siryus.swisscon.api.contract.dto.ContractEventLogDTO;
import com.siryus.swisscon.api.contract.dto.ContractSummaryDTO;
import com.siryus.swisscon.api.contract.dto.ContractTaskDTO;
import com.siryus.swisscon.api.contract.dto.ContractUpdateTaskRequest;
import com.siryus.swisscon.api.contract.dto.CreateContractCommentRequest;
import com.siryus.swisscon.api.contract.dto.CreateContractRequest;
import com.siryus.swisscon.api.contract.dto.ListContractsRequest;
import com.siryus.swisscon.api.contract.dto.SendMessageRequest;
import com.siryus.swisscon.api.contract.dto.UpdateContractRequest;
import com.siryus.swisscon.api.tasks.dto.ListTaskIdsRequest;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import io.restassured.http.ContentType;
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

public class Contract {

    private final AbstractMvcTestBase testBase;

    public Contract(AbstractMvcTestBase testBase) {
        this.testBase = testBase;
    }

    public ContractDTO getContract(RequestSpecification spec, Integer contractId) {
        return getContract(spec, contractId,
                r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value())).extract().as(ContractDTO.class)
        );
    }

    public ContractDTO getContract(RequestSpecification spec, Integer contractId, Function<ValidatableResponse, ContractDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .contentType(ContentType.JSON)
                        .pathParam("contractId", contractId)
                        .get(endPoint("/contract/{contractId}"))
                        .then()
        );
    }

    public List<ContractSummaryDTO> listContracts(RequestSpecification spec, ListContractsRequest request) {
        return listContracts(spec, request,
                r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .body()
                        .jsonPath()
                        .getList(".", ContractSummaryDTO.class)
        );
    }

    public List<ContractSummaryDTO> listContracts(RequestSpecification spec, ListContractsRequest request, Function<ValidatableResponse, List<ContractSummaryDTO>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .contentType(ContentType.JSON)
                        .body(request)
                        .post(endPoint("/contract/list"))
                        .then()
        );
    }

    public List<ContractSummaryDTO> listPrimaryContractExtensions(RequestSpecification spec, Integer primaryContractId) {
        return listPrimaryContractExtensions(spec, primaryContractId,
                             r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                                     .extract()
                                     .body()
                                     .jsonPath()
                                     .getList(".", ContractSummaryDTO.class)
        );
    }

    public List<ContractSummaryDTO> listPrimaryContractExtensions(RequestSpecification spec, Integer primaryContractId, Function<ValidatableResponse, List<ContractSummaryDTO>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .contentType(ContentType.JSON)
                        .pathParam("primaryContractId", primaryContractId)
                        .get(endPoint("/contract/primary/{primaryContractId}/list-extensions"))
                        .then()
        );
    }

    public ContractDTO createContract(RequestSpecification spec, CreateContractRequest request) {
        return createContract(spec, request,
                              r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value())).extract().as(ContractDTO.class)
        );
    }

    public ContractDTO createContract(RequestSpecification spec, CreateContractRequest request, Function<ValidatableResponse, ContractDTO> responseValidator) {
        return responseValidator.apply(
            given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .body(request)
                .post(endPoint("/contract/create"))
            .then()
        );
    }

    public ContractDTO updateContract(RequestSpecification spec, Integer contractId, UpdateContractRequest request) {
        return updateContract(spec, contractId, request,
                              r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value())).extract().as(ContractDTO.class)
        );
    }

    public ContractDTO updateContract(RequestSpecification spec, Integer contractId, UpdateContractRequest request, Function<ValidatableResponse, ContractDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .contentType(ContentType.JSON)
                        .body(request)
                        .pathParam("contractId", contractId)
                        .post(endPoint("/contract/{contractId}/update"))
                        .then()
        );
    }

    public void createContractComment(RequestSpecification spec, CreateContractCommentRequest request) {
        createContractComment(spec, request,
            r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value()))
        );
    }

    public void createContractComment(RequestSpecification spec, CreateContractCommentRequest request, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(
            given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .body(request)
                .post(endPoint("/contract-comment/add-comment"))
            .then()
        );
    }

    public List<ContractTaskDTO> addContractTasks(RequestSpecification spec, ContractAddTasksRequest request) {
        return addContractTasks(spec, request,
                r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .body()
                        .jsonPath()
                        .getList(".", ContractTaskDTO.class)
        );
    }

    public List<ContractTaskDTO> addContractTasks(RequestSpecification spec, ContractAddTasksRequest request, Function<ValidatableResponse, List<ContractTaskDTO>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .contentType(ContentType.JSON)
                        .body(request)
                        .post(endPoint("/contract-task/add-tasks"))
                        .then()
        );
    }

    public ContractTaskDTO updateContractTask(RequestSpecification spec, ContractUpdateTaskRequest request) {
        return updateContractTask(spec, request,
                r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract().as(ContractTaskDTO.class)
        );
    }

    public ContractTaskDTO updateContractTask(RequestSpecification spec, ContractUpdateTaskRequest request, Function<ValidatableResponse, ContractTaskDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .contentType(ContentType.JSON)
                        .body(request)
                        .post(endPoint("/contract-task/update-task"))
                        .then()
        );
    }

    public void removeContractTask(RequestSpecification spec, Integer contractTaskId) {
        removeContractTask(spec, contractTaskId,
                r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value()))
        );
    }

    public void removeContractTask(RequestSpecification spec, Integer contractTaskId, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(
                given()
                        .spec(spec)
                        .contentType(ContentType.JSON)
                        .pathParam("contractTaskId", contractTaskId)
                        .post(endPoint("/contract-task/remove-task/{contractTaskId}"))
                        .then()
        );
    }

    public ContractTaskDTO negateContractTask(RequestSpecification spec, Integer contractId, Integer contractTaskIdToNegate) {
        return negateContractTask(spec, contractId, contractTaskIdToNegate,
                                  r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                                          .extract().as(ContractTaskDTO.class)
        );
    }
    public ContractTaskDTO negateContractTask(RequestSpecification spec, Integer contractId, Integer contractTaskIdToNegate, Function<ValidatableResponse, ContractTaskDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .contentType(ContentType.JSON)
                        .pathParam("contractId", contractId)
                        .pathParam("contractTaskIdToNegate", contractTaskIdToNegate)
                        .post(endPoint("/contract-task/{contractId}/negate-task/{contractTaskIdToNegate}"))
                        .then()
        );
    }

    public List<ContractTaskDTO> listContractTasks(RequestSpecification spec, Integer contractId) {
        return listContractTasks(spec, contractId,
                r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .body()
                        .jsonPath()
                        .getList(".", ContractTaskDTO.class)
        );
    }

    public List<ContractTaskDTO> listContractTasks(RequestSpecification spec, Integer contractId, Function<ValidatableResponse, List<ContractTaskDTO>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .contentType(ContentType.JSON)
                        .pathParam("contractId", contractId)
                        .get(endPoint("/contract-task/{contractId}/list-tasks"))
                        .then()
        );
    }

    public List<ContractTaskDTO> listPrimaryContractTasks(RequestSpecification spec, Integer primaryContractId) {
        return listPrimaryContractTasks(spec, primaryContractId,
                                 r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                                         .extract()
                                         .body()
                                         .jsonPath()
                                         .getList(".", ContractTaskDTO.class)
        );
    }

    public List<ContractTaskDTO> listPrimaryContractTasks(RequestSpecification spec, Integer primaryContractId, Function<ValidatableResponse, List<ContractTaskDTO>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .contentType(ContentType.JSON)
                        .pathParam("primaryContractId", primaryContractId)
                        .get(endPoint("/contract-task/primary/{primaryContractId}/list-tasks"))
                        .then()
        );
    }

    public List<ContractTaskDTO> listNegateableTasks(RequestSpecification spec, Integer primaryContractId) {
        return listNegateableTasks(spec, primaryContractId,
                                        r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                                                .extract()
                                                .body()
                                                .jsonPath()
                                                .getList(".", ContractTaskDTO.class)
        );
    }

    public List<ContractTaskDTO> listNegateableTasks(RequestSpecification spec, Integer primaryContractId, Function<ValidatableResponse, List<ContractTaskDTO>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .contentType(ContentType.JSON)
                        .pathParam("primaryContractId", primaryContractId)
                        .get(endPoint("/contract-task/primary/{primaryContractId}/list-negateable-tasks"))
                        .then()
        );
    }

    public List<MainTaskDTO> availableContractTasksToAdd(RequestSpecification spec, Integer contractId) {
        return availableContractTasksToAdd(spec, contractId,
                r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .body()
                        .jsonPath()
                        .getList(".", MainTaskDTO.class));
    }

    public List<MainTaskDTO> availableContractTasksToAdd(RequestSpecification spec, Integer contractId, Function<ValidatableResponse, List<MainTaskDTO>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .contentType(ContentType.JSON)
                        .pathParam("contractId", contractId)
                        .get(endPoint("/contract-task/{contractId}/available-tasks-to-add"))
                        .then());
    }
    
    public ContractCommentDTO[] listContractComments(RequestSpecification spec, Integer contractId, Function<ValidatableResponse, ContractCommentDTO[]> responseValidator) {
        return responseValidator.apply(
            given()
                .spec(spec)
                .when()
                .pathParam("contractId", contractId)
                .get(endPoint("/contract-comment/list-comments/{contractId}"))
            .then()
        );
    }

    public Map<Integer, List<Integer>> listTaskAndSubTaskIdsByContract(RequestSpecification spec, Integer contractId, ListTaskIdsRequest request) {
        return listTaskAndSubTaskIdsByContract(spec, contractId, request,
                r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .jsonPath().getMap(".")
        );
    }

    public Map<Integer, List<Integer>> listTaskAndSubTaskIdsByContract(RequestSpecification spec, Integer contractId,
                                                                       ListTaskIdsRequest request,
                                                                       Function<ValidatableResponse, Map<Integer, List<Integer>>> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .contentType(ContentType.JSON)
                        .pathParam("contractId", contractId)
                        .body(request)
                        .post(endPoint("/contract-task/{contractId}/list-task-and-sub-task-ids"))
                        .then());
    }


    public ContractCommentDTO[] listContractComments(RequestSpecification spec, Integer contractId) {
        return listContractComments(spec, contractId, 
            r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                .extract()
                .as(ContractCommentDTO[].class)
        );
    }        

    public void removeContractComment(RequestSpecification spec, Integer commentId) {
        removeContractComment(spec, commentId, r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value())));
    }

    public void removeContractComment(RequestSpecification spec, Integer commentId, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(
            given()
                .spec(spec)
                .when()
                .pathParam("commentId", commentId)
                .post(endPoint("/contract-comment/remove-comment/{commentId}"))
            .then()
        );
    }

    public ContractEventLogDTO contractSendOffer(RequestSpecification spec, Integer contractId, SendMessageRequest request) {
        return contractSendOffer(spec, contractId, request,
                r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract().as(ContractEventLogDTO.class)
        );
    }

    public ContractEventLogDTO contractSendOffer(RequestSpecification spec, Integer contractId, SendMessageRequest request, Function<ValidatableResponse, ContractEventLogDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .contentType(ContentType.JSON)
                        .body(request)
                        .pathParam("contractId", contractId)
                        .post(endPoint("/contract-event-log/{contractId}/send-offer"))
                        .then()
        );
    }

    public ContractEventLogDTO contractSendInvitation(RequestSpecification spec, Integer contractId, SendMessageRequest request) {
        return contractSendInvitation(spec, contractId, request,
                r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract().as(ContractEventLogDTO.class)
        );
    }

    public ContractEventLogDTO contractSendInvitation(RequestSpecification spec, Integer contractId, SendMessageRequest request, Function<ValidatableResponse, ContractEventLogDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .contentType(ContentType.JSON)
                        .body(request)
                        .pathParam("contractId", contractId)
                        .post(endPoint("/contract-event-log/{contractId}/send-invitation"))
                        .then()
        );
    }

    public ContractEventLogDTO selfAcceptOffer(RequestSpecification spec, Integer contractId, SendMessageRequest request) {
        return selfAcceptOffer(spec, contractId, request,
                r -> r.statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract().as(ContractEventLogDTO.class)
        );
    }

    public ContractEventLogDTO selfAcceptOffer(RequestSpecification spec, Integer contractId, SendMessageRequest request, Function<ValidatableResponse, ContractEventLogDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .contentType(ContentType.JSON)
                        .body(request)
                        .pathParam("contractId", contractId)
                        .post(endPoint("/contract-event-log/{contractId}/self-accept-offer"))
                        .then()
        );
    }

    public String quickLinkContractAction(Integer contractId, String action, Integer userId) {
        return quickLinkContractAction(contractId, action, userId,
                r -> r.assertThat().statusCode(HttpStatus.OK.value()).extract().response().body().prettyPrint());
    }

    public String quickLinkContractAction(Integer contractId, String action, Integer userId, Function<ValidatableResponse, String> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(testBase.defaultSpec())
                        .contentType(ContentType.HTML)
                        .accept(ContentType.HTML)
                        .pathParam("contractId", contractId)
                        .queryParam("action", action)
                        .queryParam("userId", userId)
                        .get("/q/contract/{contractId}")
                        .then()
        );
    }

}
