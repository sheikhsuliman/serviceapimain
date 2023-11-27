package com.siryus.swisscon.api.customroles.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.auth.role.RoleName;
import lombok.Builder;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@Builder(toBuilder = true)
public class CustomRoleDTO {
    private final Integer id;

    @NotEmpty
    private final String name;

    @NotEmpty
    private final String description;

    private final boolean deprecated;
    private final boolean memberDefault;
    private final boolean companyDefault;
    private final boolean projectRole;
    private final boolean systemRole;
    private final boolean adminRole;
    private final boolean uniqAndMandatory;

    private final List<CustomPermissionDTO> permissions;

    @JsonCreator
    public CustomRoleDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("deprecated") Boolean deprecated,
            @JsonProperty("memberDefault") Boolean memberDefault,
            @JsonProperty("companyDefault") Boolean companyDefault,
            @JsonProperty("projectRole") Boolean projectRole,
            @JsonProperty("systemRole") Boolean systemRole,
            @JsonProperty("adminRole") Boolean adminRole,
            @JsonProperty("uniqAndMandatory") Boolean uniqAndMandatory,
            @JsonProperty("permissions") List<CustomPermissionDTO> permissions
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.deprecated = ifNull(deprecated, false);
        this.memberDefault = ifNull(memberDefault, false);
        this.companyDefault = ifNull(companyDefault, false);
        this.projectRole = ifNull(projectRole, false);
        this.systemRole = ifNull(systemRole, false);
        this.adminRole = ifNull(adminRole, false);
        this.uniqAndMandatory = ifNull(uniqAndMandatory, false);
        this.permissions = permissions;
    }

    public static CustomRoleDTO from(Role entity) {
        return new CustomRoleDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.isDeprecated(),
                entity.isMemberDefault(),
                entity.isOwnerDefault(),
                entity.isProjectRole(),
                entity.isSystemRole(),
                entity.isAdmin(),
                RoleName.isUniqAndMandatory(entity.getName()),
                entity.getPermissions().stream().map(CustomPermissionDTO::from).collect(Collectors.toList())
        );
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public boolean isMemberDefault() {
        return memberDefault;
    }

    public boolean isCompanyDefault() {
        return companyDefault;
    }

    public boolean isProjectRole() {
        return projectRole;
    }

    public boolean isSystemRole() {
        return systemRole;
    }

    public boolean isAdminRole() {
        return adminRole;
    }

    public boolean isUniqAndMandatory() {
        return uniqAndMandatory;
    }

    public List<CustomPermissionDTO> getPermissions() {
        return permissions;
    }

    private static boolean ifNull(Boolean value, boolean ifNullValue) {
        return value == null ? ifNullValue : value;
    }
}
