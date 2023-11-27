package com.siryus.swisscon.api.project.project;


import com.siryus.swisscon.api.util.DateConverter;
import com.siryus.swisscon.api.util.validator.Sanitizable;
import com.siryus.swisscon.api.util.validator.SanitizableHtml;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Sanitizable
public class EditProjectDTO {
    @NotNull
    @NotEmpty
    @Size(max = 255)
    private String name;
    private Integer typeId;
    @SanitizableHtml
    @Size(max = 1024)
    private String description;
    private String latitude;
    private String longitude;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private Integer defaultImageId;
    private AddressDTO address;

    public static EditProjectDTO from(Project project) {
        return EditProjectDTO.builder()
                .name(project.getName())
                .typeId(project.getType() != null ? project.getType().getId() : null)
                .description(project.getDescription())
                .latitude(project.getLatitude())
                .longitude(project.getLongitude())
                .startDate(DateConverter.toUtcZonedDateTime(project.getStartDate()))
                .endDate(DateConverter.toUtcZonedDateTime(project.getEndDate()))
                .defaultImageId(project.getDefaultImage() != null ? project.getDefaultImage().getId() : null)
                .address(AddressDTO.from(project))
                .build();
    }

}
