package com.siryus.swisscon.api.auth.role;

import com.siryus.swisscon.api.auth.permission.Permission;
import lombok.Builder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Builder(toBuilder = true)
@Entity
@Table(name = "role")
public class Role {
	@Id
	@GeneratedValue(strategy =  GenerationType.IDENTITY)
	private Integer id;

	private String name;

	private String description;

	private boolean deprecated;

	@Column(name = "member_default")
	private boolean memberDefault;

	@Column(name = "owner_default")
	private boolean ownerDefault;

	@Column(name = "project_role")
	private boolean projectRole;

	@Column(name= "system_role")
	private boolean systemRole;

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinTable(
			name = "role_permission",
			joinColumns = { @JoinColumn(name = "role_id") },
			inverseJoinColumns = { @JoinColumn(name = "permission_id") }
	)
	private List<Permission> permissions;

	public Role() {
	}

	public Role(
			Integer id,
			String name,
			String description,
			boolean deprecated,
			boolean memberDefault,
			boolean ownerDefault,
			boolean projectRole,
			boolean systemRole,
			List<Permission> permissions
	) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.deprecated = deprecated;
		this.memberDefault = memberDefault;
		this.ownerDefault = ownerDefault;
		this.projectRole = projectRole;
		this.systemRole = systemRole;
		this.permissions = permissions;
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

	public boolean isMemberDefault() {
		return memberDefault;
	}

	public void setMemberDefault(boolean memberDefault) {
		this.memberDefault = memberDefault;
	}

	public boolean isOwnerDefault() {
		return ownerDefault;
	}

	public void setOwnerDefault(boolean companyDefault) {
		this.ownerDefault = companyDefault;
	}

	public boolean isProjectRole() {
		return projectRole;
	}

	public void setProjectRole(boolean projectRole) {
		this.projectRole = projectRole;
	}

	public boolean isSystemRole() {
		return systemRole;
	}

	public void setSystemRole(boolean systemRole) {
		this.systemRole = systemRole;
	}

	public List<Permission> getPermissions() {
		return permissions;
	}

	public boolean isAdmin() {
		return Optional.ofNullable(getPermissions()).orElse(Collections.emptyList()).stream().anyMatch(Permission::isAdminPermission);
	}

	public static boolean isAdmin(Role role) {
		return role.isAdmin();
	}

	public static Role ref(Integer id) {
		return id == null ? null : Role.builder().id(id).build();
	}
}
