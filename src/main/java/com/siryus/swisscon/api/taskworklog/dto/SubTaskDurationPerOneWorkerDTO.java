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
public class SubTaskDurationPerOneWorkerDTO {
    Integer workerId;
    List<SubTaskDurationDTO> durationBySubTask;
    Long totalDurationInSeconds;
}