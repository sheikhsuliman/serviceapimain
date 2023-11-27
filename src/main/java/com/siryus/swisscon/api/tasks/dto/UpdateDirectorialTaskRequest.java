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
import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Optional;

@Builder(toBuilder = true)
@Getter
@Sanitizable
public class UpdateDirectorialTaskRequest {
    @NotNull
    @Reference(ReferenceType.LOCATION)
    private final Integer locationId;

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

    @SanitizableHtml
    private final String materialsAndMachines;

    @NotNull
    private final Integer tradeCatalogNodeId;

    private final Boolean template;

    @JsonCreator
    public UpdateDirectorialTaskRequest(
            @JsonProperty("locationId") Integer locationId,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("startDate") LocalDateTime startDate,
            @JsonProperty("dueDate") LocalDateTime dueDate,
            @JsonProperty("timeBudgetMinutes") Integer timeBudgetMinutes,
            @JsonProperty("materialsAndMachines") String materialsAndMachines,
            @JsonProperty("tradeCatalogNodeId") Integer tradeCatalogNodeId,
            @JsonProperty("template") Boolean template
    ) {
        this.locationId = locationId;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.timeBudgetMinutes = timeBudgetMinutes;
        this.materialsAndMachines = materialsAndMachines;
        this.tradeCatalogNodeId = tradeCatalogNodeId;
        this.template = Optional.ofNullable(template).orElse(false);
    }
}
