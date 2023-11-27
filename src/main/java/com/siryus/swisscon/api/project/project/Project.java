package com.siryus.swisscon.api.project.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.general.country.Country;
import com.siryus.swisscon.api.general.favorite.Favoritable;
import com.siryus.swisscon.api.location.location.Location;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompany;
import com.siryus.swisscon.api.project.projectstatus.ProjectStatus;
import com.siryus.swisscon.api.project.projecttype.ProjectType;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRole;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project")
public class Project implements Favoritable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@CreatedDate
	@Column(name = "created", nullable = false, updatable = false)
	private LocalDateTime createdDate;

	@LastModifiedDate
	@Column(name = "last_modified", nullable = false)
	private LocalDateTime lastModifiedDate;

	@CreatedBy
	@Column(name = "created_by", updatable = false)
	private Integer createdBy;

	@LastModifiedBy
	@Column(name = "last_modified_by")
	private Integer lastModifiedBy;

	private LocalDateTime disabled;

	private String name;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@Column(name = "start_date")
	private LocalDateTime startDate;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@Column(name = "end_date")
	private LocalDateTime endDate;

	private String description;

	private String street;

	private String code;

	private String city;

	@ManyToOne @JoinColumn(name = "country_id", referencedColumnName = "id")
	private Country country;

	private String website;

	private String longitude;

	private String latitude;

	@Column(name = "internal_id")
	private String internalId;

	@Column(name = "cached_data")
	private String cachedData;

	@ManyToOne
	@JoinColumn(referencedColumnName = "id")
	private ProjectStatus status;

	@ManyToOne
	@JoinColumn(referencedColumnName = "id")
	private ProjectType type;

	@ManyToOne
	@JoinColumn(name = "default_image", referencedColumnName = "id")
	private File defaultImage;

	public List<ProjectUserRole> getProjectUserRoles() {
		if (null == this.projectUserRoles) {
			this.projectUserRoles = new ArrayList<>();
		}
		return projectUserRoles;
	}

	@JsonIgnore
	@OneToMany(mappedBy = "project")
	@ApiModelProperty(hidden = true)
	private List<ProjectUserRole> projectUserRoles;

	public static Project ref(Integer id) {
		return Project.builder().id(id).build();
	}
}
