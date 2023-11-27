package com.siryus.swisscon.api.tasks.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.tasks.entity.SubTaskCheckListEntity;

public class TaskChecklistItem {
    private final Integer id;

    private final String title;

    private final boolean checked;


    @JsonCreator
    public TaskChecklistItem(
            @JsonProperty("id") Integer id,
            @JsonProperty("title") String title,
            @JsonProperty("checked") boolean checked
    ) {
        this.id = id;
        this.title = title;
        this.checked = checked;
    }

    public static TaskChecklistItem fromEntity(SubTaskCheckListEntity entity) {
        return new TaskChecklistItem(entity.getId(), entity.getTitle(), entity.getCheckedDate() != null);
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isChecked() {
        return checked;
    }
}
