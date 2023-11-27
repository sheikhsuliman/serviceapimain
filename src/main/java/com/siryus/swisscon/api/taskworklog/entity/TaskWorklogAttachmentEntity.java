package com.siryus.swisscon.api.taskworklog.entity;

import com.siryus.swisscon.api.file.file.File;
import lombok.Builder;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Builder(toBuilder = true)
@Entity
@Table(name = "task_work_log_attachment")
public class TaskWorklogAttachmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name="task_work_log_id", referencedColumnName = "id")
    private TaskWorklogEntity taskWorklog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="file_id", referencedColumnName = "id")
    private File file;

    public TaskWorklogAttachmentEntity() {

    }

    public TaskWorklogAttachmentEntity(Integer id, TaskWorklogEntity taskWorklog, File file) {
        this.id = id;
        this.taskWorklog = taskWorklog;
        this.file = file;
    }

    public Integer getId() {
        return id;
    }

    public TaskWorklogEntity getTaskWorklog() {
        return taskWorklog;
    }

    public File getFile() {
        return file;
    }
}
