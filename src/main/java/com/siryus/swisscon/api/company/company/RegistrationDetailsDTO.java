package com.siryus.swisscon.api.company.company;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder(toBuilder = true)
public class RegistrationDetailsDTO {
    private final String vatNumber;
    private final String taxNumber;
    private final String registerEntryNumber;

    @JsonCreator
    public RegistrationDetailsDTO(
            @JsonProperty("vatNumber") String vatNumber,
            @JsonProperty("taxNumber") String taxNumber,
            @JsonProperty("registerEntryNumber") String registerEntryNumber
    ) {
        this.vatNumber = vatNumber;
        this.taxNumber = taxNumber;
        this.registerEntryNumber = registerEntryNumber;
    }

    public static RegistrationDetailsDTO from(Company company) {
        return new RegistrationDetailsDTO(
                company.getVatNumber(),
                company.getTaxNumber(),
                company.getRegisterEntryNumber()
        );
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public String getRegisterEntryNumber() {
        return registerEntryNumber;
    }

}
