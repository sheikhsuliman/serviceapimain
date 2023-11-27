package com.siryus.swisscon.api.tasks.entity;

import com.siryus.swisscon.api.project.project.Project;
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

@Entity
@Table(name = "task_link")
@Builder(toBuilder = true)
public class TaskLinkEntity {
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
    @Column(name = "disabled_by")
    private Integer disabledBy;

    private LocalDateTime disabled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="project_id", referencedColumnName = "id")
    private Project project;

    @Column(name = "link_type")
    @Enumerated(EnumType.STRING)
    private TaskLinkType linkType;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="src_task_id", referencedColumnName = "id")
    private MainTaskEntity sourceTask;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="dst_task_id", referencedColumnName = "id")
    private MainTaskEntity destinationTask;

    public TaskLinkEntity() {
    }

    public TaskLinkEntity(Integer id, Integer createdBy, LocalDateTime createdDate, Integer disabledBy, LocalDateTime disabled, Project project, TaskLinkType linkType, MainTaskEntity sourceTask, MainTaskEntity destinationTask) {
        this.id = id;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.disabledBy = disabledBy;
        this.disabled = disabled;
        this.project = project;
        this.linkType = linkType;
        this.sourceTask = sourceTask;
        this.destinationTask = destinationTask;
    }

    public Integer getId() {
        return id;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public Integer getDisabledBy() {
        return disabledBy;
    }

    public LocalDateTime getDisabled() {
        return disabled;
    }

    public Project getProject() {
        return project;
    }

    public TaskLinkType getLinkType() {
        return linkType;
    }

    public MainTaskEntity getSourceTask() {
        return sourceTask;
    }

    public MainTaskEntity getDestinationTask() {
        return destinationTask;
    }
}
