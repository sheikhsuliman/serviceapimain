package com.siryus.swisscon.api.contract;

import com.naturalprogrammer.spring.lemon.util.LemonUtils;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserService;
import com.siryus.swisscon.api.contract.dto.ContractDTO;
import com.siryus.swisscon.api.util.TemplateUtil;
import com.siryus.swisscon.api.util.TranslationUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.IntConsumer;

@Service
public class ContractQuickLinkService {

    private static final String CONTRACT_ACCEPT_DECLINE_TEMPLATE = "template/contractAcceptedOrDeclined.html";
    private static final String CONTRACT_ACCEPT_DECLINE_MESSAGE_KEY = "contract.accept_or_declined.message";
    private static final String CONTRACT_NAME_KEY = "contractName";

    private static final String CONTRACT_OFFER_ACCEPTED_MESSAGE = "contract.offer_accepted.message";
    private static final String CONTRACT_OFFER_DECLINED_MESSAGE = "contract.offer_declined.message";
    private static final String CONTRACT_INVITATION_ACCEPTED_MESSAGE = "contract.invitation_accepted.message";
    private static final String CONTRACT_INVITATION_DECLINED_MESSAGE = "contract.invitation_declined.message";
    private static final String CONTRACT_ACCEPT_DECLINE_FAILED_MESSAGE = "contract.accept_decline_failed.message";

    private final ContractEventLogService contractEventLogService;
    private final ContractBaseService contractBaseService;
    private final UserService userService;
    private final TemplateUtil templateUtil;
    private final TranslationUtil translationUtil;

    @Autowired
    public ContractQuickLinkService(ContractEventLogService contractEventLogService, ContractBaseService contractBaseService, UserService userService, TemplateUtil templateUtil, TranslationUtil translationUtil) {
        this.contractEventLogService = contractEventLogService;
        this.contractBaseService = contractBaseService;
        this.userService = userService;
        this.templateUtil = templateUtil;
        this.translationUtil = translationUtil;
    }

    public String handleContractLink(Integer contractId, String action, Integer userId) {
        switch (action) {
            case "accept-offer":
                return acceptOffer(contractId, userId);
            case "decline-offer":
                return declineOffer(contractId, userId);
            case "accept-invitation":
                return acceptInvitation(contractId, userId);
            case "decline-invitation":
                return declineInvitation(contractId, userId);
            default: throw ContractExceptions.contractQuickLinkActionNotSupported(action);
        }
    }

    public String acceptOffer(Integer contractId, Integer userId) {
        return acceptOrDeclineContract(contractId, userId, CONTRACT_OFFER_ACCEPTED_MESSAGE,
                contractEventLogService::acceptOffer);
    }

    public String declineOffer(Integer contractId, Integer userId) {
        return acceptOrDeclineContract(contractId, userId, CONTRACT_OFFER_DECLINED_MESSAGE,
                contractEventLogService::declineOffer);
    }

    public String acceptInvitation(Integer contractId, Integer userId) {
        return acceptOrDeclineContract(contractId, userId, CONTRACT_INVITATION_ACCEPTED_MESSAGE,
                contractEventLogService::acceptInvitation);
    }

    public String declineInvitation(Integer contractId, Integer userId) {
        return acceptOrDeclineContract(contractId, userId, CONTRACT_INVITATION_DECLINED_MESSAGE,
                contractEventLogService::declineInvitation);
    }

    private String acceptOrDeclineContract(Integer contractId, Integer userId, String acceptOrDeclineMessage, IntConsumer acceptOrDeclineContractFunction) {
        return acceptDeclineContract(contractId, acceptOrDeclineMessage,
                acceptOrDeclineContractFunction, loginWithUserId(userId));
    }

    private String acceptDeclineContract(Integer contractId, String acceptOrDeclineMessage, IntConsumer acceptOrDeclineContractFunction, User user) {
        try {
            ContractDTO contract = contractBaseService.getContract(contractId);
            acceptOrDeclineContractFunction.accept(contract.getId());
            return getQuickLinkTemplate(acceptOrDeclineMessage,
                    user.getPrefLang().getId(),
                    contract.getName());
        } catch (Exception e) {
            return getQuickLinkTemplate(CONTRACT_ACCEPT_DECLINE_FAILED_MESSAGE,
                    user.getPrefLang().getId(),
                    StringUtils.EMPTY);
        }
    }

    private User loginWithUserId(Integer userId) {
        User user = userService.findById(userId);
        LemonUtils.login(user);
        return user;
    }

    private String getQuickLinkTemplate(String messageKey, String languageId, String contractName) {
        String translatedMessage = translationUtil.get(messageKey, languageId);
        return templateUtil.loadTranslatedTemplate(CONTRACT_ACCEPT_DECLINE_TEMPLATE,
                languageId,
                CONTRACT_ACCEPT_DECLINE_MESSAGE_KEY, translatedMessage,
                CONTRACT_NAME_KEY, contractName);
    }

}
