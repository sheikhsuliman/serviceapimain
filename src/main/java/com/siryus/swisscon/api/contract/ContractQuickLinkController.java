package com.siryus.swisscon.api.contract;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/q")
@Api(tags = {"Contract:quick-link"})
public class ContractQuickLinkController {

    private final ContractQuickLinkService quickLinkService;

    @Autowired
    public ContractQuickLinkController(ContractQuickLinkService quickLinkService) {
        this.quickLinkService = quickLinkService;
    }

    @ApiOperation(
            "Handle a contract quick link (accept/decline offer/invitation)"
    )
    @GetMapping(value = "contract/{contractId}", produces = MediaType.TEXT_HTML_VALUE)
    String handleLink(@PathVariable() Integer contractId,
                      @RequestParam(value = "action") String action,
                      @RequestParam(value = "userId") Integer userId) {
        return quickLinkService.handleContractLink(contractId, action, userId);
    }

}
