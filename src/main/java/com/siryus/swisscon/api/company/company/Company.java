package com.siryus.swisscon.api.company.company;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.siryus.swisscon.api.auth.signup.SignupCompanyDTO;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.company.companylegaltype.CompanyLegalType;
import com.siryus.swisscon.api.company.companytrade.CompanyTrade;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.numworkersofcompany.NumWorkersOfCompany;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.general.country.Country;
import com.siryus.swisscon.api.general.favorite.Favoritable;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompany;
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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "company")
@EntityListeners(AuditingEntityListener.class)
public class Company implements Favoritable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private LocalDateTime disabled;

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

	private String name;

	private String director;

	@ManyToOne
	@JoinColumn(name = "picture_id", referencedColumnName = "id")
	private File picture;

	private String description;

	private String address1;

	private String address2;

	private String plz;

	private String city;

	@Column(name="vat")
	private String vatNumber;

	@Column(name = "tax_number")
	private String taxNumber;

	@Column(name = "register_entry_number")
	private String registerEntryNumber;
	
	private String longitude;

	private String latitude;

	@Column(name = "register_scan_doc_url")
	private String registerScanDocUrl;

	@Column(name = "register_url")
	private String registerUrl;

	@Column(name = "fiscal_id")
	private String fiscalId;

	@Column(name = "web_url")
	private String webUrl;

	private String phone;

	@Column(name = "confirmed_date")
	private Date confirmedDate;

	@Column(name = "company_email")
	private String companyEmail;

	private String fax;
	
	private String founded;
	private String certification;
	
	@Column(name = "annual_turnover")
	private String annualTurnover;
	
	@Column(name = "apprentice_training")
	private Boolean apprenticeTraining;
	
	
	@Column(name = "special_skills")
	private String specialSkills;
	
	private String remarks;
	
	@Transient
	private User owner;
	
	@Transient
	private List<String> trades;
	

	@ManyToOne
	@JoinColumn(referencedColumnName = "id")
	private Country country;

	@ManyToOne
	@JoinColumn(name = "confirmed_user", referencedColumnName = "id")
	private User confirmedUser;

	@ManyToOne
	@JoinColumn(name = "creation_user", referencedColumnName = "id")
	private User creationUser;

	@ManyToOne
	@JoinColumn(name = "company_size_id", referencedColumnName = "id")
	public NumWorkersOfCompany numberOfEmployees;

	@ManyToOne
	@JoinColumn(name = "company_legal_type_id", referencedColumnName = "id")
	private CompanyLegalType legalType;

	@JsonIgnore
	@OneToMany(mappedBy = "company")
	@ApiModelProperty(hidden = true)
	private List<CompanyTrade> companyTrades;

	@JsonIgnore
	@OneToMany(mappedBy = "company")
	@ApiModelProperty(hidden = true)
	private List<ProjectCompany> projectCompanies;


	@JsonIgnore
	@OneToMany(mappedBy = "company")
	@ApiModelProperty(hidden = true)
	private List<CompanyUserRole> companyUserRoles;

	public void setContactData(ContactDetailDTO contactDetailDTO) {
		this.setAddress1(contactDetailDTO.getAddress1());
		this.setAddress2(contactDetailDTO.getAddress2());
		this.setPhone(contactDetailDTO.getPhone());
		this.setPlz(contactDetailDTO.getPlz());
		this.setCity(contactDetailDTO.getCity());

		this.setWebUrl(contactDetailDTO.getWebUrl());
		this.setCompanyEmail(contactDetailDTO.getCompanyEmail());
		this.setFax(contactDetailDTO.getFax());

		this.setCountry(Country.ref(contactDetailDTO.getCountryId()));
	}

	public Company with(SignupCompanyDTO companySignupDTO) {
		Country country = new Country();
		country.setId(companySignupDTO.getCountryId());

		NumWorkersOfCompany numWorkersOfCompany = new NumWorkersOfCompany();
		numWorkersOfCompany.setId(companySignupDTO.getNumberOfEmployeesId());

		setName(companySignupDTO.getName());
		setCountry(country);
		setNumberOfEmployees(numWorkersOfCompany);

		return this;
	}

}
