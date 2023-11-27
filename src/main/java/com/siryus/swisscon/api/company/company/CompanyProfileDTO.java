package com.siryus.swisscon.api.company.company;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Optional;

@Builder(toBuilder = true)
public class CompanyProfileDTO {
    @NotNull
    @NotBlank
    private final String name;

    private final String director;

    private final Integer[] tradeIds;

    @NotNull
    @Reference(ReferenceType.NUM_WORKERS_OF_COMPANY)
    private final Integer numberOfEmployees;

    @Reference(ReferenceType.COMPANY_LEGAL_TYPE)
    private final Integer companyTypeId;

    @Reference(ReferenceType.FILE)
    private final Integer fileIdProfileImage;

    private final String description;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lastModified;

    @JsonCreator
    public CompanyProfileDTO(
            @JsonProperty("name") String name,
            @JsonProperty("director") String director,
            @JsonProperty("tradeIds") Integer[] tradeIds,
            @JsonProperty("numberOfEmployees") Integer numberOfEmployees,
            @JsonProperty("companyTypeId") Integer companyTypeId,
            @JsonProperty("fileIdProfileImage") Integer fileIdProfileImage,
            @JsonProperty("description") String description,
            @JsonProperty("lastModified") LocalDateTime lastModified
    ) {
        this.name = name;
        this.director = director;
        this.tradeIds = tradeIds;
        this.numberOfEmployees = numberOfEmployees;
        this.companyTypeId = companyTypeId;
        this.fileIdProfileImage = fileIdProfileImage;
        this.description = description;
        this.lastModified = lastModified;
    }

    public static CompanyProfileDTO fromCompany(Company company) {
        return new CompanyProfileDTO(
                company.getName(),
                company.getDirector(),
                company.getCompanyTrades()
                        .stream()
                        .map(ct -> ct.getTrade())
                        .toArray(Integer[]::new),
                company.getNumberOfEmployees().getId(),
                company.getLegalType().getId(),
                Optional.ofNullable(company.getPicture()).map(File::getId).orElse(null),
                company.getDescription(),
                company.getLastModifiedDate()
        );
    }

    public String getName() {
        return name;
    }

    public String getDirector() {
        return director;
    }

    public Integer[] getTradeIds() {
        return tradeIds;
    }

    public Integer getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public Integer getCompanyTypeId() {
        return companyTypeId;
    }

    public Integer getFileIdProfileImage() {
        return fileIdProfileImage;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }
}
