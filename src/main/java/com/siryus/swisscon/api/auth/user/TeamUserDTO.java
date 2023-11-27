package com.siryus.swisscon.api.auth.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.naturalprogrammer.spring.lemon.commons.util.UserUtils;
import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.file.file.FileDTO;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRole;
import com.siryus.swisscon.api.util.EmailPhoneUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
public class TeamUserDTO {

    private final Integer id;

    private final String firstName;

    private final String lastName;

    private final FileDTO picture;

    @Deprecated
    private final Integer roleId;

    private final List<Integer> roleIds;

    private final String mobile;

    private final String email;

    private final Boolean isAdmin;

    private final Boolean isUnverified;

    private final Integer companyId;

    private final Boolean inProject;

    @JsonCreator()
    public TeamUserDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("picture") FileDTO picture,
            @JsonProperty("roleId") Integer roleId,
            @JsonProperty("roleIds") List<Integer> roleIds,
            @JsonProperty("mobile") String mobile,
            @JsonProperty("email") String email,
            @JsonProperty("isAdmin") Boolean isAdmin,
            @JsonProperty("isUnverified") Boolean isUnverified,
            @JsonProperty("companyId") Integer companyId,
            @JsonProperty("inProject") Boolean inProject) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.picture = picture;
        this.roleId = roleId;
        this.roleIds = roleIds;
        this.mobile = mobile;
        this.email = email;
        this.isAdmin = isAdmin;
        this.isUnverified = isUnverified;
        this.companyId = companyId;
        this.inProject = inProject;
    }

    public static TeamUserDTO fromCompanyUserRole(CompanyUserRole role) {
        User u = role.getUser();

        return new TeamUserDTO(
                u.getId(),
                u.getGivenName(), u.getSurName(),
                FileDTO.fromFile(u.getPicture()),
                //TODO remove role as soon as ui is updated
                role.getRole().getId(),
                Arrays.asList(role.getRole().getId()),
                EmailPhoneUtils.toFullPhoneNumber(u.getMobileCountryCode(), u.getMobile()),
                User.nullIfUnsetEmail(u.getEmail()),
                Role.isAdmin(role.getRole()),
                u.hasRole(UserUtils.Role.UNVERIFIED),
                role.getCompany().getId(),
                false
        );
    }

    public static TeamUserDTO fromProjectUserRole(ProjectUserRole role) {
        User u = role.getUser();

        return new TeamUserDTO(
                u.getId(),
                u.getGivenName(), u.getSurName(),
                FileDTO.fromFile(u.getPicture()),
                //TODO remove role as soon as ui is updated
                role.getRole().getId(),
                Arrays.asList(role.getRole().getId()),
                EmailPhoneUtils.toFullPhoneNumber(u.getMobileCountryCode(), u.getMobile()),
                User.nullIfUnsetEmail(u.getEmail()),
                Role.isAdmin(role.getRole()),
                u.hasRole(UserUtils.Role.UNVERIFIED),
                role.getProjectCompany().getCompany().getId(),
                true
        );
    }

    public TeamUserDTO inProject(boolean inProject) {
        return toBuilder().inProject(inProject).build();
    }

}
