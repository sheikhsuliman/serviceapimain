package com.siryus.swisscon.api.company.companyuserrole;

import java.time.LocalDateTime;

import javax.persistence.*;

import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.auth.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;

import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "company_user_role")
public class CompanyUserRole {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime confirmed;

	@Column(name = "confirmed_by")
	private String confirmedBy;

	@ManyToOne
	@JoinColumn(name = "company", referencedColumnName = "id", nullable = false)
    Company company;

	@ManyToOne
	@JoinColumn(name="user_id", referencedColumnName = "id", nullable = false)
	User user;

	@ManyToOne
	@JoinColumn(name = "role", referencedColumnName = "id", nullable = false)
	Role role;

	@Override
	public String toString() {
		return new ToStringBuilder(this).
				append("confirmed", confirmed).
				append("confirmedBy", confirmedBy).
				append("company", company != null ? company.getName() : null).
				append("role", role != null ? role.getName() : null).
				append("user", user != null ? user.getName() : null).
				toString();
	}

}
