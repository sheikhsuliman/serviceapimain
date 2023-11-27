package com.siryus.swisscon.api.tasks.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Builder(toBuilder = true)
public class AddTaskChecklistItemRequest {
        @NotNull(message="Title of a sub task checklist cannot be null")
        @NotBlank(message="Title of a sub task checklist cannot be empty")
        private final String title;

        @JsonCreator
        public AddTaskChecklistItemRequest(
                @JsonProperty("title") String title
        ) {
                this.title = title;
        }

        public String getTitle() {
                return title;
        }
}
