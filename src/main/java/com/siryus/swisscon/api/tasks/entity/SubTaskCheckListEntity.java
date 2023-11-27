package com.siryus.swisscon.api.tasks.entity;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sub_task_check_list")
public class SubTaskCheckListEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Integer id;

    @CreatedBy
    @Column(name = "created_by")
    private Integer createdBy;

    @CreatedDate
    @Column(name = "created")
    private LocalDateTime createdDate;

    @Column(name = "checked_by")
    private Integer checkedBy;

    @Column(name = "checked")
    private LocalDateTime checkedDate;

    @Column(name = "sub_task_id", nullable=false)
    private Integer subTaskId;

    @Column(name="title")
    private String title;

    public SubTaskCheckListEntity() {
    }
    public SubTaskCheckListEntity(Integer subTaskId, String title) {
        this(null, subTaskId, title);
    }
    public SubTaskCheckListEntity(Integer id, Integer subTaskId, String title) {
        this.id = id;
        this.subTaskId = subTaskId;
        this.title = title;
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

    public Integer getCheckedBy() {
        return checkedBy;
    }

    public void setCheckedBy(Integer checkedBy) {
        this.checkedBy = checkedBy;
    }

    public LocalDateTime getCheckedDate() {
        return checkedDate;
    }

    public void setCheckedDate(LocalDateTime checkedDate) {
        this.checkedDate = checkedDate;
    }

    public Integer getSubTaskId() {
        return subTaskId;
    }

    public void setSubTaskId(Integer subTask) {
        this.subTaskId = subTask;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
