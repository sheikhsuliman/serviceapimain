package com.siryus.swisscon.api.contract.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;

@Builder(toBuilder = true)
@Getter
public class SendMessageRequest {
    @Reference(ReferenceType.COMPANY)
    private final Integer recipientCompanyId;

    @NotEmpty
    private final String messageText;

    @JsonCreator
    public SendMessageRequest(
            @JsonProperty("recipientCompanyId") Integer recipientCompanyId,
            @JsonProperty("messageText") String messageText
    ) {
        this.recipientCompanyId = recipientCompanyId;
        this.messageText = messageText;
    }
}
