package com.siryus.swisscon.api.tasks.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.catalog.dto.CatalogNodeDTO;
import com.siryus.swisscon.api.location.location.LocationReferenceDTO;
import com.siryus.swisscon.api.mediawidget.MediaWidgetFileDTO;
import com.siryus.swisscon.api.tasks.entity.TaskType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
@Getter
public class MainTaskDTO {
    private final Integer id;

    private final Integer createdBy;

    private final LocalDateTime created;

    private final TaskType taskType;

    private final Integer taskNumber;

    private final Integer projectId;

    private final Integer companyId;

    private final List<LocationReferenceDTO> locationPath;

    private final SpecificationDTO specification;

    private final CompanyCatalogItemDTO companyCatalogItem;

    private final String title;

    private final String description;

    private final CatalogNodeDTO trade;

    private final TaskStatus status;

    private final RejectDTO reject;

    private final LocalDateTime startDate;

    private final LocalDateTime dueDate;

    private final Integer timeBudgetMinutes;

    private final List<SubTaskDTO> subTasks;

    private final SubTaskDTO defaultSubTask;

    private final List<Integer> team;

    private final String materialsAndMachines;

    private final List<MediaWidgetFileDTO> attachments;

    private final Integer contractId;

    private final Boolean template;

    @JsonCreator
    public MainTaskDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("createdBy") Integer createdBy,
            @JsonProperty("created") LocalDateTime created,
            @JsonProperty("taskType") TaskType taskType,
            @JsonProperty("taskNumber") Integer taskNumber,
            @JsonProperty("projectId") Integer projectId,
            @JsonProperty("companyId") Integer companyId,
            @JsonProperty("locationPath") List<LocationReferenceDTO> locationPath,
            @JsonProperty("specification") SpecificationDTO specification,
            @JsonProperty("companyCatalogItem") CompanyCatalogItemDTO companyCatalogItem,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("trade") CatalogNodeDTO trade,
            @JsonProperty("status") TaskStatus status,
            @JsonProperty("reject") RejectDTO reject,
            @JsonProperty("startDate") LocalDateTime startDate,
            @JsonProperty("dueDate") LocalDateTime dueDate,
            @JsonProperty("timeBudgetMinutes") Integer timeBudgetMinutes,
            @JsonProperty("subTasks") List<SubTaskDTO> subTasks,
            @JsonProperty("defaultSubTask") SubTaskDTO defaultSubTask,
            @JsonProperty("team") List<Integer> team,
            @JsonProperty("materialsAndMachines") String materialsAndMachines,
            @JsonProperty("attachments") List<MediaWidgetFileDTO> attachments,
            @JsonProperty("contractId") Integer contractId,
            @JsonProperty("template") Boolean template
    ) {
        this.id = id;
        this.createdBy = createdBy;
        this.created = created;
        this.taskType = taskType;
        this.taskNumber = taskNumber;
        this.projectId = projectId;
        this.companyId = companyId;
        this.locationPath = locationPath;
        this.specification = specification;
        this.companyCatalogItem = companyCatalogItem;
        this.title = title;
        this.description = description;
        this.trade = trade;
        this.status = status;
        this.reject = reject;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.timeBudgetMinutes = timeBudgetMinutes;
        this.subTasks = subTasks;
        this.defaultSubTask = defaultSubTask;
        this.team = team;
        this.materialsAndMachines = materialsAndMachines;
        this.attachments = attachments;
        this.contractId = contractId;
        this.template = template;
    }

    public static class RejectDTO {
        private final LocalDateTime timestamp;
        private final String comment;
        private final List<MediaWidgetFileDTO> attachments;

        @JsonCreator
        public RejectDTO(
                @JsonProperty("timestamp") LocalDateTime timestamp,
                @JsonProperty("comment") String comment,
                @JsonProperty("attachments") List<MediaWidgetFileDTO> attachments
        ) {
            this.timestamp = timestamp;
            this.comment = comment;
            this.attachments = attachments;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public String getComment() {
            return comment;
        }

        public List<MediaWidgetFileDTO> getAttachments() {
            return attachments;
        }
    }
}
