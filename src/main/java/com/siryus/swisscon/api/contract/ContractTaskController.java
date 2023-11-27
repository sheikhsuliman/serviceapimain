package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.contract.dto.ContractAddTasksRequest;
import com.siryus.swisscon.api.contract.dto.ContractTaskDTO;
import com.siryus.swisscon.api.contract.dto.ContractUpdateTaskRequest;
import com.siryus.swisscon.api.tasks.dto.ListTaskIdsRequest;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/rest/contract-task")
@Api(tags = {"Contract:task"})
class ContractTaskController {

    private final ContractTasksReader reader;
    private final ContractTaskService contractTaskService;

    @Autowired
    ContractTaskController(
            ContractTasksReader reader,
            ContractTaskService contractTaskService
    ) {
        this.reader = reader;
        this.contractTaskService = contractTaskService;
    }


    @ApiOperation("Add a set of tasks to a contract")
    @PostMapping("add-tasks")
    @PreAuthorize("hasPermission(#contractAddTaskRequest.contractId, 'CONTRACT', 'CONTRACT_UPDATE')")
    public List<ContractTaskDTO> addTasks(@RequestBody ContractAddTasksRequest contractAddTaskRequest) {
        return contractTaskService.addTasks(contractAddTaskRequest);
    }

    @ApiOperation("Update a task inside a contract")
    @PostMapping("update-task")
    @PreAuthorize("hasPermission(#contractUpdateTaskRequest.contractTaskId, 'CONTRACT_TASK', 'CONTRACT_UPDATE')")
    public ContractTaskDTO updateTask(@RequestBody ContractUpdateTaskRequest contractUpdateTaskRequest) {
        return contractTaskService.updateTask(contractUpdateTaskRequest);
    }

    @ApiOperation("Remove a task from a contract")
    @PostMapping("remove-task/{contractTaskId}")
    @PreAuthorize("hasPermission(#contractTaskId, 'CONTRACT_TASK', 'CONTRACT_UPDATE')")
    public void removeTask(@PathVariable() Integer contractTaskId) {
        contractTaskService.removeTask(contractTaskId);
    }

    @ApiOperation("Get the tasks which could be added to the contract")
    @GetMapping("{contractId}/available-tasks-to-add")
    @PreAuthorize("hasPermission(#contractId, 'CONTRACT', 'CONTRACT_UPDATE')")
    public List<MainTaskDTO> availableTasksToAdd(@PathVariable Integer contractId) {
        return contractTaskService.availableTasksToAdd(contractId);
    }

    @ApiOperation("List tasks of a contract")
    @GetMapping("{contractId}/list-tasks")
    @PreAuthorize("hasPermission(#contractId, 'CONTRACT', 'CONTRACT_VIEW_DETAILS')")
    public List<ContractTaskDTO> listTasks(@PathVariable Integer contractId) {
        return reader.listTasks(contractId);
    }

    @ApiOperation("List task and sub task id(s) for specific contract")
    @PostMapping("{contractId}/list-task-and-sub-task-ids")
    @PreAuthorize("hasPermission(#contractId, 'CONTRACT', 'CONTRACT_VIEW_DETAILS')")
    public Map<Integer, List<Integer>> listTaskAndSubTaskIdsByContract(@PathVariable Integer contractId, @RequestBody ListTaskIdsRequest request) {
        return reader.listTaskAndSubTaskIdsByContract(contractId, request);
    }


    @ApiOperation("List tasks of a primary contract")
    @GetMapping("primary/{primaryContractId}/list-tasks")
    @PreAuthorize("hasPermission(#primaryContractId, 'CONTRACT', 'CONTRACT_VIEW_DETAILS')")
    public List<ContractTaskDTO> listTasksByPrimaryContract(@PathVariable Integer primaryContractId) {
        return reader.listTasksByPrimaryContract(primaryContractId);
    }

    @ApiOperation("List Negatable tasks for given primary contract")
    @GetMapping("primary/{primaryContractId}/list-negateable-tasks")
    @PreAuthorize("hasPermission(#primaryContractId, 'CONTRACT', 'CONTRACT_VIEW_DETAILS')")
    public List<ContractTaskDTO> listNegateableTasks(@PathVariable Integer primaryContractId) {
        return contractTaskService.listNegateableTasks(primaryContractId);
    }

    @ApiOperation("Negate one of tasks from primary contract or one of previous sub-contracts")
    @PostMapping("{contractId}/negate-task/{contractTaskIdToNegate}")
    public ContractTaskDTO negateTask(@PathVariable Integer contractId, @PathVariable Integer contractTaskIdToNegate) {
        return contractTaskService.negateTask(contractId, contractTaskIdToNegate);
    }
}
