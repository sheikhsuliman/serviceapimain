package com.siryus.swisscon.api.tasks.entity;

import com.siryus.swisscon.api.tasks.dto.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Entity
@Table(name = "sub_task")
public class SubTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private Integer createdBy;

    @CreatedDate
    @Column(name = "created", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedBy
    @Column(name = "last_modified_by", nullable = false)
    private Integer lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified", nullable = false)
    private LocalDateTime lastModifiedDate;

    private LocalDateTime disabled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="main_task_id", referencedColumnName = "id")
    private MainTaskEntity mainTask;

    @Column(name = "sub_task_number")
    private Integer subTaskNumber;

    @Column(name="title")
    private String title;

    @Column(name="description")
    private String description;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.ASSIGNED;

    @Column(name="time_budget_min")
    private Integer timeBudgetMinutes;

    public SubTaskEntity() {
    }

    public SubTaskEntity(MainTaskEntity mainTask, Integer subTaskNumber, String title, String description) {
        this(mainTask, subTaskNumber, title, description, null);
    }

    public SubTaskEntity(MainTaskEntity mainTask, Integer subTaskNumber, String title, String description, Integer timeBudgetMinutes) {
        this.mainTask = mainTask;
        this.subTaskNumber = subTaskNumber;
        this.title = title;
        this.description = description;
        this.timeBudgetMinutes = timeBudgetMinutes;
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

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Integer getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(Integer lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public LocalDateTime getDisabled() {
        return disabled;
    }

    public void setDisabled(LocalDateTime disabled) {
        this.disabled = disabled;
    }

    public MainTaskEntity getMainTask() {
        return mainTask;
    }

    public void setMainTask(MainTaskEntity mainTask) {
        this.mainTask = mainTask;
    }

    public Integer getSubTaskNumber() {
        return subTaskNumber;
    }

    public void setSubTaskNumber(Integer subTaskNumber) {
        this.subTaskNumber = subTaskNumber;
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

    public void setTimeBudgetMinutes(Integer timeBudgetMinutes){
        this.timeBudgetMinutes=timeBudgetMinutes;
    }

    public Integer getTimeBudgetMinutes(){
        return timeBudgetMinutes;
    }

    public static SubTaskEntity ref(Integer id) {
        return SubTaskEntity.builder().id(id).build();
    }
}
