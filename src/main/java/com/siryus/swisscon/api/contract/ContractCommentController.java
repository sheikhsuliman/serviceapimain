package com.siryus.swisscon.api.contract;

import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.siryus.swisscon.api.contract.dto.ContractCommentDTO;
import com.siryus.swisscon.api.contract.dto.ContractCommentsDTO;
import com.siryus.swisscon.api.contract.dto.CreateContractCommentRequest;
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

@RestController
@RequestMapping("/api/rest/contract-comment")
@Api( tags = {"Contract:comment"})
class ContractCommentController {
    private final ContractCommentService commentService;

    @Autowired
    ContractCommentController(ContractCommentService commentService) {
        this.commentService = commentService;
    }
    
    @ApiOperation(value = "Add comment to contract")    
    @PostMapping(path = "add-comment")
    @PreAuthorize("hasPermission(#comment.contractId, 'CONTRACT', 'CONTRACT_UPDATE')")
    ContractCommentDTO addComment(
            @RequestBody CreateContractCommentRequest comment
    ) {        
        return commentService.addComment(comment, Integer.parseInt(LecwUtils.currentUser().getId()));
    }

    @Deprecated
    @ApiOperation(value = "List all comments in a contract")
    @GetMapping(path = "list-comments/{contractId}")
    @PreAuthorize("hasPermission(#contractId, 'CONTRACT', 'CONTRACT_VIEW_DETAILS')")
    List<ContractCommentDTO> listCommentsDeprecated(@PathVariable Integer contractId) {
        return commentService.listComments(contractId);
    }

    @ApiOperation(value = "List all comments in a contract")
    @GetMapping(path = "{contractId}/list")
    @PreAuthorize("hasPermission(#contractId, 'CONTRACT', 'CONTRACT_VIEW_DETAILS')")
    List<ContractCommentDTO> listComments(@PathVariable Integer contractId) {
        return commentService.listComments(contractId);
    }

    @ApiOperation(value = "List all comments in a contract")
    @GetMapping(path = "primary/{primaryContractId}/list")
    @PreAuthorize("hasPermission(#primaryContractId, 'CONTRACT', 'CONTRACT_VIEW_DETAILS')")
    List<ContractCommentDTO> listCommentsForPrimaryContract(@PathVariable Integer primaryContractId) {
        return commentService.listCommentsForPrimaryContract(primaryContractId);
    }

    @Deprecated
    @ApiOperation(value = "List all comments in a contract")
    @GetMapping(path = "list-contract-comments/{contractId}")
    @PreAuthorize("hasPermission(#contractId, 'CONTRACT', 'CONTRACT_VIEW_DETAILS')")
    ContractCommentsDTO listContractComments(@PathVariable Integer contractId) {
        return commentService.listContractComments(contractId);
    }

    @ApiOperation(value = "Remove a comment in a contract")
    @PostMapping(path = "remove-comment/{commentId}")
    @PreAuthorize("hasPermission(#commentId, 'CONTRACT_COMMENT', 'CONTRACT_UPDATE')")
    void removeComment(@PathVariable Integer commentId) {        
        commentService.removeComment(commentId);
    }        
}
