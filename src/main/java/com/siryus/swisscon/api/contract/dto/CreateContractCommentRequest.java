
package com.siryus.swisscon.api.contract.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
public class CreateContractCommentRequest {
    @NotNull
    @Reference(ReferenceType.CONTRACT)
    private final Integer contractId;  
    
    private final String text;
    
    private final Integer fileId;
    
    @JsonCreator
    public CreateContractCommentRequest(
            @JsonProperty("contractId") Integer contractId,
            @JsonProperty("text") String text,
            @JsonProperty("fileId") Integer fileId
    ) {
        this.contractId = contractId;
        this.text = text;
        this.fileId = fileId;
    }    
}
