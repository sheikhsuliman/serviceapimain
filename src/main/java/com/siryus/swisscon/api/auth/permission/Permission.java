package com.siryus.swisscon.api.auth.permission;

import lombok.Builder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Builder(toBuilder = true)
@Entity
@Table(name = "permission")
public class Permission {

	@Id
	@GeneratedValue(strategy =  GenerationType.IDENTITY)
	private Integer id;

	private String name;

	private String description;

	private boolean deprecated;

	@Column(name= "project_permission")
	private boolean projectPermission;

	@Column(name= "admin_permission")
	private boolean adminPermission;

	public Permission() {
	}

	public Permission(
			Integer id,
			String name,
			String description,
			boolean deprecated,
			boolean projectPermission,
			boolean adminPermission
	) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.deprecated = deprecated;
		this.projectPermission = projectPermission;
		this.adminPermission = adminPermission;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isDeprecated() {
		return deprecated;
	}

	public void setDeprecated(boolean deprecated) {
		this.deprecated = deprecated;
	}

	public boolean isProjectPermission() {
		return projectPermission;
	}

	public void setProjectPermission(boolean projectPermission) {
		this.projectPermission = projectPermission;
	}

	public boolean isAdminPermission() {
		return adminPermission;
	}

	public void setAdminPermission(boolean adminPermission) {
		this.adminPermission = adminPermission;
	}
}
