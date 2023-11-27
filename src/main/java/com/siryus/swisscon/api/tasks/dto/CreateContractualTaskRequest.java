package com.siryus.swisscon.api.tasks.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import com.siryus.swisscon.api.util.validator.Sanitizable;
import com.siryus.swisscon.api.util.validator.SanitizableHtml;
import lombok.Builder;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
@Sanitizable
public class CreateContractualTaskRequest {
    @NotNull
    @Reference(ReferenceType.LOCATION)
    private final Integer locationId;

    @NotNull
    @Valid
    private final CreateCompanyCatalogItemRequest companyCatalogItem;

    @NotNull
    @Valid
    private final CreateSpecificationRequest specification;

    @NotNull
    @NotEmpty
    @Size(max = 255)
    private final String title;

    @NotNull
    @NotEmpty
    @SanitizableHtml
    @Size(max = 1024)
    private final String description;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @NotNull
    private final LocalDateTime startDate;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @NotNull
    private final LocalDateTime dueDate;

    @NotNull
    private final Integer timeBudgetMinutes;

    private final List<Integer> taskTeam;

    private final boolean template;

    @JsonCreator
    public CreateContractualTaskRequest(
            @JsonProperty("locationId") Integer locationId,
            @JsonProperty("companyCatalogItem") CreateCompanyCatalogItemRequest companyCatalogItem,
            @JsonProperty("specification") CreateSpecificationRequest specification,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("startDate") LocalDateTime startDate,
            @JsonProperty("dueDate") LocalDateTime dueDate,
            @JsonProperty("timeBudgetMinutes") Integer timeBudgetMinutes,
            @JsonProperty("taskTeam") List<Integer> taskTeam,
            @JsonProperty("template") Boolean template
    ) {
        this.locationId = locationId;
        this.companyCatalogItem = companyCatalogItem;
        this.specification = specification;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.timeBudgetMinutes = timeBudgetMinutes;
        this.taskTeam = taskTeam;
        this.template = template;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public CreateCompanyCatalogItemRequest getCompanyCatalogItem() {
        return companyCatalogItem;
    }

    public CreateSpecificationRequest getSpecification() {
        return specification;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public Integer getTimeBudgetMinutes() {
        return timeBudgetMinutes;
    }

    public List<Integer> getTaskTeam() { return taskTeam; }

    public boolean isTemplate() {
        return template;
    }
}
