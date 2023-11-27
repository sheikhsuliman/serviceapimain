package com.siryus.swisscon.api.company.companylegaltype;

import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.siryus.swisscon.api.company.company.Company;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "company_legal_type")
public class CompanyLegalType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;

	@JsonIgnore
	@OneToMany(mappedBy = "legalType")
	@ApiModelProperty(hidden = true)
	private List<Company> companies;

}
