package com.siryus.swisscon.api.project.project;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import com.siryus.swisscon.api.util.validator.Sanitizable;
import com.siryus.swisscon.api.util.validator.SanitizableHtml;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
public class NewProjectDTO {
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
    @Reference(ReferenceType.FILE)
    private Integer defaultImageId;
    private AddressDTO address;
}
