package com.siryus.swisscon.api.project.project;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.siryus.swisscon.api.auth.user.UserProfileDTO;
import com.siryus.swisscon.api.file.file.FileDTO;
import com.siryus.swisscon.api.project.projectstatus.ProjectStatus;
import com.siryus.swisscon.api.project.projecttype.ProjectType;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRole;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private Integer id;
    private String name;
    private Boolean starred;
    private AddressDTO address;
    private Integer progressPercentage;
    private ProjectStatus status;
    private ProjectType type;
    private FileDTO defaultImage;
    private UserProfileDTO projectOwner;
    private Integer projectOwnerCompanyId;
    private Integer projectCustomerCompanyId;

    public static ProjectDTO fromProject(Project project, ProjectUserRole owner, Integer customerId, Boolean starred, Integer progressPercentage) {
        ProjectDTO projectDTO = new ProjectDTO();

        // set default properties
        projectDTO.setId(project.getId());
        projectDTO.setName(project.getName());
        projectDTO.setAddress(AddressDTO.from(project));
        projectDTO.setStatus(project.getStatus());
        projectDTO.setType(project.getType());
        projectDTO.setStartDate(project.getStartDate());
        projectDTO.setEndDate(project.getEndDate());

        // set custom properties
        projectDTO.setStarred(starred);
        projectDTO.setProgressPercentage(progressPercentage);
        projectDTO.setDefaultImage(FileDTO.fromFile(project.getDefaultImage()));

        projectDTO.setProjectOwner(UserProfileDTO.fromUser(owner.getUser()));
        projectDTO.setProjectOwnerCompanyId(owner.getProjectCompany().getCompany().getId());
        projectDTO.setProjectCustomerCompanyId(customerId);

        return projectDTO;
    }

}
