package com.siryus.swisscon.api.tasks.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class EditTaskChecklistItemRequest {
        @NotNull(message="Title of a sub task checklist cannot be null")
        @NotBlank(message="Title of a sub task checklist cannot be empty")
        private final String title;

        @JsonCreator
        public EditTaskChecklistItemRequest(
                @JsonProperty("title") String title
        ) {
                this.title = title;
        }

        public String getTitle() {
                return title;
        }
}
