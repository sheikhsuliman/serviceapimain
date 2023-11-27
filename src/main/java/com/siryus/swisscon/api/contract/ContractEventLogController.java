package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.contract.dto.ContractEventLogDTO;
import com.siryus.swisscon.api.contract.dto.ContractEventsDTO;
import com.siryus.swisscon.api.contract.dto.SendMessageRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
    @RequestMapping("/api/rest/contract-event-log")
@Api( tags = {"Contract:event-log"})
public class ContractEventLogController {

    private final ContractEventLogService eventLogService;

    @Autowired
    public ContractEventLogController(ContractEventLogService eventLogService) {
        this.eventLogService = eventLogService;
    }

    @ApiOperation(
            "List all events logged for given contract"
    )
    @GetMapping("{contractId}/list")
    List<ContractEventLogDTO> listEvents(@PathVariable Integer contractId) {
        return eventLogService.listEvents(contractId);
    }

    @ApiOperation(
            "List all events logged for given primary contract and all it's extensions"
    )
    @GetMapping("primary/{primaryContractId}/list")
    List<ContractEventLogDTO> listEventsForPrimaryContract(@PathVariable Integer primaryContractId) {
        return eventLogService.listEventsForPrimaryContract(primaryContractId);
    }

    @Deprecated
    @ApiOperation(
            "List all events logged for given contract"
    )
    @GetMapping("{contractId}/list-contract-events")
    ContractEventsDTO listContractEvents(@PathVariable Integer contractId) {
        return eventLogService.listContractEvents(contractId);
    }

    @ApiOperation(
            "Send an offer for given contract to specified customer"
    )
    @PostMapping("{contractId}/send-offer")
    ContractEventLogDTO sendOffer(@PathVariable Integer contractId, @RequestBody SendMessageRequest request) {
        return eventLogService.sendOffer(contractId, request);
    }

    @ApiOperation(
            "Accept an offer"
    )
    @PostMapping("{contractId}/accept-offer")
    ContractEventLogDTO acceptOffer(@PathVariable Integer contractId) {
        return eventLogService.acceptOffer(contractId);
    }

    @ApiOperation(
            "Self accept an offer."
    )
    @PostMapping("{contractId}/self-accept-offer")
    ContractEventLogDTO selfAcceptOffer(@PathVariable Integer contractId, @RequestBody SendMessageRequest request) {
        return eventLogService.selfAcceptOffer(contractId, request);
    }

    @ApiOperation(
            "Decline an offer"
    )
    @PostMapping("{contractId}/decline-offer")
    ContractEventLogDTO declineOffer(@PathVariable Integer contractId) {
        return eventLogService.declineOffer(contractId);
    }

    @ApiOperation(
            "Send an invitation for given contract to specified contractor"
    )
    @PostMapping("{contractId}/send-invitation")
    ContractEventLogDTO sendInvitation(@PathVariable Integer contractId, @RequestBody SendMessageRequest request) {
        return eventLogService.sendInvitation(contractId, request);
    }

    @ApiOperation(
            "Accept an invitation"
    )
    @PostMapping("{contractId}/accept-invitation")
    ContractEventLogDTO acceptInvitation(@PathVariable Integer contractId) {
        return eventLogService.acceptInvitation(contractId);
    }

    @ApiOperation(
            "Decline an invitation"
    )
    @PostMapping("{contractId}/decline-invitation")
    ContractEventLogDTO declineInvitation(@PathVariable Integer contractId) {
        return eventLogService.declineInvitation(contractId);
    }

}
