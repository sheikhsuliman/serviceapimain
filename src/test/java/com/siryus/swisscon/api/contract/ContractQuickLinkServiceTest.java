package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserService;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.contract.dto.ContractDTO;
import com.siryus.swisscon.api.exceptions.LocalizedReason;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;
import com.siryus.swisscon.api.general.langcode.LangCode;
import com.siryus.swisscon.api.util.TemplateUtil;
import com.siryus.swisscon.api.util.TranslationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

public class ContractQuickLinkServiceTest {

    private static final String TOKEN = "a5X3aP";
    private static final Integer USER_ID = 35;
    private static final Integer CONTRACTOR_ID = 77;
    private static final Integer CONTRACT_ID = 135;
    private static final String CONTRACT_NAME = "Contract XYZ";

    private final ContractEventLogService contractEventLogServiceMock = Mockito.mock(ContractEventLogService.class);
    private final ContractBaseService contractBaseServiceMock = Mockito.mock(ContractBaseService.class);
    private final UserService userServiceMock = Mockito.mock(UserService.class);
    private final ContractQuickLinkService contractQuickLinkService;

    public ContractQuickLinkServiceTest() {
        TranslationUtil translationUtil = new TranslationUtil();
        TemplateUtil templateUtil = new TemplateUtil(translationUtil);
        contractQuickLinkService = new ContractQuickLinkService(
                contractEventLogServiceMock,
                contractBaseServiceMock,
                userServiceMock,
                templateUtil,
                translationUtil);
    }

    @BeforeEach
    public void initTest() {
        Mockito.when(userServiceMock.findById(USER_ID))
                .thenReturn(User.builder().id(USER_ID).prefLang(LangCode.builder().id("en_US").build()).build());
        Mockito.when(userServiceMock.findById(CONTRACTOR_ID))
                .thenReturn(User.builder().id(CONTRACTOR_ID).prefLang(LangCode.builder().id("en_US").build()).build());
        Mockito.when(contractBaseServiceMock.getContract(CONTRACT_ID))
                .thenReturn(ContractDTO.builder()
                        .name(CONTRACT_NAME)
                        .id(CONTRACT_ID)
                        .contractorSigners(Collections.singletonList(TeamUserDTO.builder().id(CONTRACTOR_ID).build()))
                        .customerSigners(Collections.singletonList(TeamUserDTO.builder().id(CONTRACTOR_ID).build()))
                        .build());
    }

    @Test
    public void Given_correctContract_When_acceptOffer_Then_returnCorrectHtml() {
        String htmlContent = contractQuickLinkService.acceptOffer(CONTRACT_ID, USER_ID);
        TestAssert.templateContainsCaseInsensitive(htmlContent, "<table", "contract", "offer", "accepted", CONTRACT_NAME);
    }

    @Test
    public void Given_correctContract_When_declineOffer_Then_returnCorrectHtml() {
        String htmlContent = contractQuickLinkService.declineOffer(CONTRACT_ID, USER_ID);
        TestAssert.templateContainsCaseInsensitive(htmlContent, "<table", "contract", "offer", "declined", CONTRACT_NAME);
    }

    @Test
    public void Given_correctContract_When_acceptInvitation_Then_returnCorrectHtml() {
        String htmlContent = contractQuickLinkService.acceptInvitation(CONTRACT_ID, USER_ID);
        TestAssert.templateContainsCaseInsensitive(htmlContent, "<table", "contract", "invitation", "accepted", CONTRACT_NAME);
    }

    @Test
    public void Given_correctContract_When_declineInvitation_Then_returnCorrectHtml() {
        String htmlContent = contractQuickLinkService.declineInvitation(CONTRACT_ID, USER_ID);
        TestAssert.templateContainsCaseInsensitive(htmlContent, "<table", "contract", "invitation", "declined", CONTRACT_NAME);
    }

    @Test
    public void Given_failedExecution_When_acceptOffer_Then_returnFailedHtml() {
        Mockito.when(contractEventLogServiceMock.acceptOffer(CONTRACT_ID))
                .thenThrow(LocalizedResponseStatusException.badRequest(LocalizedReason.like(6, TOKEN)));
        String htmlContent = contractQuickLinkService.acceptOffer(CONTRACT_ID, USER_ID);
        TestAssert.templateContainsCaseInsensitive(htmlContent, "<table", "contract", "failed", "accept", "decline");
    }

}
