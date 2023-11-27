package com.siryus.swisscon.api.tasks.entity;

import com.siryus.swisscon.api.specification.Specification;
import com.siryus.swisscon.api.tasks.dto.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name ="main_task")
public class MainTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @CreatedBy
    @Column(name = "created_by")
    private Integer createdBy;

    @CreatedDate
    @Column(name = "created")
    private LocalDateTime createdDate;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private Integer lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified")
    private LocalDateTime lastModifiedDate;

    private LocalDateTime disabled;

    @Column(name = "task_type")
    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    @Column(name = "task_number")
    private Integer taskNumber;

    @Column(name="location_id")
    private Integer locationId;

    @Column(name="project_id")
    private Integer projectId;

    @OneToOne
    @JoinColumn(name="specification_id", referencedColumnName = "id")
    private Specification specification;

    @Column(name = "company_id")
    private Integer companyId;

    @Column(name = "global_catalog_node_id")
    private Integer globalCatalogNodeId;

    @Column(name="title")
    private String title;

    @Column(name="description")
    private String description;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(name="start_date")
    private LocalDateTime startDate;

    @Column(name="due_date")
    private LocalDateTime dueDate;

    @Column(name = "time_budget_min")
    private Integer timeBudgetMinutes;

    @Column(name = "materials_and_machines")
    private String materialsAndMachines;

    @Column(name = "template")
    private boolean template = false;

    public MainTaskEntity(
            TaskType taskType,
            Integer taskNumber,
            Integer locationId,
            Integer projectId,
            Integer companyId,
            Specification specification,
            Integer globalCatalogNodeId,
            String title,
            String description,
            LocalDateTime startDate,
            LocalDateTime dueDate,
            Integer timeBudgetMinutes,
            String materialsAndMachines,
            TaskStatus status,
            boolean template
    ) {
        this.taskType = taskType;
        this.taskNumber = taskNumber;
        this.locationId = locationId;
        this.projectId = projectId;
        this.companyId = companyId;
        this.specification = specification;
        this.globalCatalogNodeId = globalCatalogNodeId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.timeBudgetMinutes = timeBudgetMinutes;
        this.materialsAndMachines = materialsAndMachines;
        this.template = template;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public Integer getLastModifiedBy() {
        return lastModifiedBy;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public LocalDateTime getDisabled() {
        return disabled;
    }

    public void setDisabled(LocalDateTime disabled) {
        this.disabled = disabled;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public Integer getTaskNumber() {
        return taskNumber;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Specification getSpecification() {
        return specification;
    }

    public void setSpecification(Specification specification) {
        this.specification = specification;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public Integer getGlobalCatalogNodeId() {
        return globalCatalogNodeId;
    }

    public void setGlobalCatalogNodeId(Integer globalCatalogNodeId) {
        this.globalCatalogNodeId = globalCatalogNodeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getTimeBudgetMinutes() {
        return timeBudgetMinutes;
    }

    public void setTimeBudgetMinutes(Integer timeBudgetMinutes) {
        this.timeBudgetMinutes = timeBudgetMinutes;
    }

    public String getMaterialsAndMachines() {
        return materialsAndMachines;
    }

    public void setMaterialsAndMachines(String materialsAndMachines) {
        this.materialsAndMachines = materialsAndMachines;
    }

    public boolean isTemplate() {
        return template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

    public static MainTaskEntity ref(Integer id) {
        return MainTaskEntity.builder().id(id).build();
    }
}
