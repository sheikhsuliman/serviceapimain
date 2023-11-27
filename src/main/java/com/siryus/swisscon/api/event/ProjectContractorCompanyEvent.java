package com.siryus.swisscon.api.event;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder(toBuilder = true)
@Getter
public class ProjectContractorCompanyEvent implements CustomEvent {
    private final Integer projectId;
    private final Integer contractorCompanyId;
    private final List<Integer> taskIds;

    public ProjectContractorCompanyEvent(
            Integer projectId,
            Integer contractorCompanyId,
            List<Integer> taskIds
    ) {
        this.projectId = projectId;
        this.contractorCompanyId = contractorCompanyId;
        this.taskIds = taskIds;
    }
}
