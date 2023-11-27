package com.siryus.swisscon.api.project.projectstatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.siryus.swisscon.api.project.project.Project;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_status")
public class ProjectStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;

	@JsonIgnore
	@OneToMany(mappedBy = "status")
	@ApiModelProperty(hidden = true)
	private List<Project> projects;

}
