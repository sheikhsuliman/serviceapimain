package com.siryus.swisscon.api.customroles.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.auth.permission.Permission;
import lombok.Builder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Builder(toBuilder = true)
public class CustomPermissionDTO {
    @NotNull
    private final Integer id;

    @NotEmpty
    private final String name;

    @NotEmpty
    private final String description;

    private final boolean deprecated;
    private final boolean projectPermission;
    private final boolean adminPermission;

    @JsonCreator
    public CustomPermissionDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("deprecated") boolean deprecated,
            @JsonProperty("projectPermission") boolean projectPermission,
            @JsonProperty("adminPermission") boolean adminPermission
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.deprecated = deprecated;
        this.projectPermission = projectPermission;
        this.adminPermission = adminPermission;
    }

    public static CustomPermissionDTO from(Permission entity) {
        return new CustomPermissionDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.isDeprecated(),
                entity.isProjectPermission(),
                entity.isAdminPermission()
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

    public boolean isProjectPermission() {
        return projectPermission;
    }

    public boolean isAdminPermission() {
        return adminPermission;
    }
}
