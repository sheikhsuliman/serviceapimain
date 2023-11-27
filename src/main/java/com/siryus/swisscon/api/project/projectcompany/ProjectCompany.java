package com.siryus.swisscon.api.project.projectcompany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.project.project.Project;
import com.siryus.swisscon.api.project.projectcompanytrade.ProjectCompanyTrade;
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_company")
public class ProjectCompany {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@CreatedDate
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@Column(name = "created", nullable = false, updatable = false)
	private LocalDateTime createdDate;

	@LastModifiedDate
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@Column(name = "last_modified", nullable = false)
	private LocalDateTime lastModifiedDate;

	@CreatedBy
	@Column(name = "created_by", updatable = false)
	private Integer createdBy;

	@LastModifiedBy
	@Column(name = "last_modified_by")
	private Integer lastModifiedBy;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime disabled;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company", referencedColumnName = "id", nullable = false)
	private Company company;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project", referencedColumnName = "id", nullable = false)
	private Project project;

	@JsonIgnore
	@OneToMany(mappedBy = "projectCompany", fetch = FetchType.LAZY)
	@ApiModelProperty(hidden = true)
	private List<ProjectCompanyTrade> projectCompanyTrades;

}
