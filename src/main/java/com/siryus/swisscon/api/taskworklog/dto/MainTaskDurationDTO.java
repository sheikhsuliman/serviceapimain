package com.siryus.swisscon.api.taskworklog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MainTaskDurationDTO {
    private Integer mainTaskId;
    private List<SubTaskDurationDTO> subTasksDuration;
    private Long totalDurationInSeconds;
}
