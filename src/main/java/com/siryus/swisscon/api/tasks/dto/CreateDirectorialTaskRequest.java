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

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Builder(toBuilder = true)
@Sanitizable
public class CreateDirectorialTaskRequest {
    @NotNull
    @Reference(ReferenceType.LOCATION)
    private final Integer locationId;

    @Reference(ReferenceType.COMPANY)
    private final Integer companyId;

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

    @SanitizableHtml
    private final String materialsAndMachines;

    private final List<@Reference(ReferenceType.FILE) Integer> attachmentIDs;

    @NotNull
    private final Integer tradeCatalogNodeId;
    
    @NotNull    
    private final boolean requiresContract;

    private final boolean template;


    @JsonCreator
    public CreateDirectorialTaskRequest(
            @JsonProperty("locationId") Integer locationId,
            @JsonProperty("companyId") Integer companyId,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("startDate") LocalDateTime startDate,
            @JsonProperty("dueDate") LocalDateTime dueDate,
            @JsonProperty("timeBudgetMinutes") Integer timeBudgetMinutes,
            @JsonProperty("taskTeam") List<Integer> taskTeam,
            @JsonProperty("materialsAndMachines") String materialsAndMachines,
            @JsonProperty("attachmentIDs") List<Integer> attachmentIDs,
            @JsonProperty("tradeCatalogNodeId") Integer tradeCatalogNodeId,
            @JsonProperty("requiresContract") Boolean requiresContract,
            @JsonProperty("template") Boolean template
    ) {
        this.locationId = locationId;
        this.companyId = companyId;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.timeBudgetMinutes = timeBudgetMinutes;
        this.taskTeam = taskTeam;
        this.materialsAndMachines = materialsAndMachines;
        this.attachmentIDs = attachmentIDs;
        this.tradeCatalogNodeId = tradeCatalogNodeId;
        this.requiresContract = Optional.ofNullable(requiresContract).orElse(false);
        this.template = Optional.ofNullable(template).orElse(false);
    }

    public Integer getLocationId() {
        return locationId;
    }

    public Integer getCompanyId() {
        return companyId;
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

    public String getMaterialsAndMachines() {
        return materialsAndMachines;
    }

    public List<Integer> getAttachmentIDs() {
        return attachmentIDs;
    }

    public Integer getTradeCatalogNodeId() {
        return tradeCatalogNodeId;
    }

    public boolean isRequiresContract() {
        return requiresContract;
    }

    public Boolean isTemplate() {
        return template;
    }
}
