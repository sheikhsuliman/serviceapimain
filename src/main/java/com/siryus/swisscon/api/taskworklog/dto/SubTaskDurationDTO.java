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
public class SubTaskDurationDTO {
    private Integer subTaskId;
    private List<WorkerDurationDTO> workersWorklogs;
    private Long totalDurationInSeconds;
}
