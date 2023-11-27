package com.siryus.swisscon.api.taskworklog.entity;

import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.tasks.entity.MainTaskEntity;
import com.siryus.swisscon.api.tasks.entity.SubTaskEntity;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventCounter;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventPerSubTaskCounter;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQuery;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SqlResultSetMapping(
        name = "WorkLogEventCounterMapping",
        classes = {
                @ConstructorResult(
                        targetClass = WorkLogEventCounter.class,
                        columns = {
                                @ColumnResult(name = "event", type = String.class),
                                @ColumnResult(name = "count", type = Long.class)
                        }
                )
        }
)
@SqlResultSetMapping(
        name = "WorkLogPerSubTaskEventCounterMapping",
        classes = {
                @ConstructorResult(
                        targetClass = WorkLogEventPerSubTaskCounter.class,
                        columns = {
                                @ColumnResult(name = "sub_task_id", type = Integer.class),
                                @ColumnResult(name = "event", type = String.class),
                                @ColumnResult(name = "count", type = Long.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "TaskWorklogEntity.getEventsCountForWorkerBySubTask",
        query = "SELECT sub_task_id, event, count(*) as count FROM task_work_log WHERE worker_id = :workerId GROUP BY sub_task_id, event",
        resultSetMapping = "WorkLogPerSubTaskEventCounterMapping"
)
@NamedNativeQuery(
        name = "TaskWorklogEntity.getEventsCountForWorker",
        query = "SELECT event, count(*) as count FROM task_work_log WHERE worker_id = :workerId GROUP BY event",
        resultSetMapping = "WorkLogEventCounterMapping"
)
@NamedNativeQuery(
        name = "TaskWorklogEntity.getEventsCountForSubTask",
        query = "SELECT event, count(*) as count FROM task_work_log WHERE sub_task_id = :subTaskId GROUP BY event",
        resultSetMapping = "WorkLogEventCounterMapping"
)
@NamedNativeQuery(
        name = "TaskWorklogEntity.getEventsCountForMainTask",
        query = "SELECT w.event, count(*) as count FROM task_work_log w WHERE w.main_task_id = :mainTaskId GROUP BY w.event",
        resultSetMapping = "WorkLogEventCounterMapping"
)

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "task_work_log")
public class TaskWorklogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="main_task_id", referencedColumnName = "id")
    private MainTaskEntity mainTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="sub_task_id", referencedColumnName = "id")
    private SubTaskEntity subTask;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="worker_id", referencedColumnName = "id")
    private User worker;

    @Column(name="timestamp")
    private LocalDateTime timestamp;

    @Column(name="event")
    @Enumerated(EnumType.STRING)
    private WorkLogEventType event;

    @Column(name="comment")
    private String comment;

    @Column(name="latitude")
    private String latitude;

    @Column(name="longitude")
    private String longitude;

    @Column(name="modify_to_worklog_id")
    private Integer modifyingWorklogId;

    @OneToMany(mappedBy = "taskWorklog", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private List<TaskWorklogAttachmentEntity> attachmentIDs = new ArrayList<>();
    
    public TaskWorklogEntity(
            MainTaskEntity mainTask, SubTaskEntity subTask,
            User worker,
            LocalDateTime timestamp,
            WorkLogEventType event, String comment, String latitude, String longitude,
            Integer modifyingWorklogId,
            List<TaskWorklogAttachmentEntity> attachmentIDs
    ) {
        this.mainTask = mainTask;
        this.subTask = subTask;
        this.worker = worker;
        this.timestamp = timestamp;
        this.event = event;
        this.comment = comment;
        this.latitude = latitude;
        this.longitude = longitude;
        this.modifyingWorklogId=modifyingWorklogId;
        this.attachmentIDs = attachmentIDs;
    }
}
