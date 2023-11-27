package com.siryus.swisscon.api.project.project;

import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.file.file.FileDTO;
import com.siryus.swisscon.api.project.projectstatus.ProjectStatus;
import com.siryus.swisscon.api.project.projecttype.ProjectType;
import com.siryus.swisscon.api.util.DateConverter;
import com.siryus.swisscon.soa.ApiConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents the information needed when displaying the project board
 * 
 * It is used in these calls:
 * - /projects/:id
 * 
 * @author hng
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectBoardDTO {
    private Integer id;
    private String name;
    private ProjectType type;
    private ProjectStatus status;
    private String description;
    private String latitude;
    private String longitude;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private Boolean starred;
    private FileDTO defaultImage;
    private AddressDTO address;    
    private List<TeamUserDTO> teamMembers;
    private Integer projectOwnerCompanyId;
    private Integer projectCustomerCompanyId;
    private Boolean projectCustomerIsReassignable;

    private Map<String, String> features;


    /**
     * Builds a frontend model of the Project as seen on Project Board page
     * @param p Project to convert from
     * @return the dto
     */
    public static ProjectBoardDTO from(Project p, Integer projectOwnerCompanyId, Integer projectCustomerCompanyId,
                                       Boolean starred, Boolean customerIsReassignable) {
        List<TeamUserDTO> members = p.getProjectUserRoles()
                                    .stream()
                                    .map(TeamUserDTO::fromProjectUserRole)
                                    .collect(Collectors.toList());


        return ProjectBoardDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .startDate(DateConverter.toUtcZonedDateTime(p.getStartDate()))
                .endDate(DateConverter.toUtcZonedDateTime(p.getEndDate()))
                .starred(starred)
                .defaultImage(FileDTO.fromFile(p.getDefaultImage()))
                .address(AddressDTO.from(p))
                .teamMembers(members)
                .type(p.getType())
                .status(p.getStatus())
                .projectOwnerCompanyId(projectOwnerCompanyId)
                .projectCustomerCompanyId(projectCustomerCompanyId)
                .projectCustomerIsReassignable(customerIsReassignable)
                .features(buildFeatureMap(p))
                .build();
    }

    private static Map<String, String> buildFeatureMap(Project p) {
        if (ApiConfiguration.getVar(ApiConfiguration.Var.MEDIA_WIDGET_V2).equals("off")) {
            return Map.of(ApiConfiguration.Var.MEDIA_WIDGET_V2.asCamelCase(), "off");
        }
        else {
            Integer cutOffId = Integer.parseInt(ApiConfiguration.getVar(ApiConfiguration.Var.MEDIA_WIDGET_V2_CUT_OFF_PROJECT_ID));
            return Map.of(ApiConfiguration.Var.MEDIA_WIDGET_V2.asCamelCase(), p.getId() > cutOffId ? "on" : "off");
        }
    }
}
