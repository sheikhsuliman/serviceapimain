package com.siryus.swisscon.api.company.companyregion;

import javax.persistence.*;

import com.siryus.swisscon.api.general.country.Country;
import com.siryus.swisscon.api.company.company.Company;
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
@Table(name = "company_region")
public class CompanyRegion{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "company", referencedColumnName = "id", nullable = false)
	private Company company;
	
	@ManyToOne
	@JoinColumn(name = "country", referencedColumnName = "id", nullable = false)
	private Country country;

}
