package com.siryus.swisscon.api.project.projectuserrole;

import javax.persistence.*;

import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.project.project.Project;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompany;
import com.siryus.swisscon.api.auth.role.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_user_role")
public class ProjectUserRole {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "project", referencedColumnName = "id", nullable = false)
    Project project;

	@ManyToOne
	@JoinColumn(name="user_id", referencedColumnName = "id", nullable = false)
	User user;

	@ManyToOne
	@JoinColumn(name = "role", referencedColumnName = "id", nullable = false)
	Role role;

	@ManyToOne
	@JoinColumn(name = "project_company", referencedColumnName = "id", nullable = false)
	ProjectCompany projectCompany;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime disabled;

}
