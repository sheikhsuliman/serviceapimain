package com.siryus.swisscon.api.auth.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.UserUtils;
import com.naturalprogrammer.spring.lemon.commons.util.UserUtils.SignUpValidation;
import com.naturalprogrammer.spring.lemon.commons.util.UserUtils.SignupInput;
import com.naturalprogrammer.spring.lemon.commons.util.UserUtils.UpdateValidation;
import com.naturalprogrammer.spring.lemon.domain.AbstractUser;
import com.siryus.swisscon.api.auth.signup.SignupUserDTO;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.general.country.Country;
import com.siryus.swisscon.api.general.favorite.Favoritable;
import com.siryus.swisscon.api.general.gender.Gender;
import com.siryus.swisscon.api.general.langcode.LangCode;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRole;
import com.siryus.swisscon.api.util.EmailPhoneUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "\"user\"")
public class User extends AbstractUser<Integer> implements Favoritable {

	private static final String RANDOM = "{RANDOM}";
	private static final String UNSET_EMAIL_PREFIX = "undefined@";
	private static final String UNSET_EMAIL_SUFFIX = ".unknown";
	private static final String UNSET_EMAIL = UNSET_EMAIL_PREFIX + RANDOM + UNSET_EMAIL_SUFFIX;
	public static final int NAME_MIN = 1;
	public static final int NAME_MAX = 50;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private @Nullable Integer id;

	@JsonView(SignupInput.class)
	@NotBlank(message = "{blank.name}", groups = {SignUpValidation.class, UpdateValidation.class})
	@Size(min=NAME_MIN, max=NAME_MAX, groups = {SignUpValidation.class, UpdateValidation.class})
	@Column(name = "user_name")
	private String username;

	@Enumerated(EnumType.STRING)
	private UserCategory category;

	private String position;

	@Column(name = "user_pool")
	private String userPool;

	private String phone;

	@Column(name = "mobile")
	private String mobile;

	@Column(name = "mobile_country_code")
	private Integer mobileCountryCode;

	private String name;

	@ManyToOne
	@JoinColumn(name = "picture_id", referencedColumnName = "id")
	private File picture;

	@Column(name = "id_card_url")
	private String idCardUrl;

	@Column(name = "about_me")
	private String aboutMe;

	@Column(name = "drivers_license")
	private String driversLicense;

	private String car;

	@Column(name = "license_plate")
	private String licensePlate;

	private String responsibility;

	@Column(name = "website_url")
	private String webSiteUrl;

	@JsonView(SignupInput.class)
	@Column(name = "given_name")
	private String givenName;

	@JsonView(SignupInput.class)
	@Column(name = "surname")
	private String surName;

	@Column(name = "sc_verified")
	private Date scVerified;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime disabled;

	@Column(name = "confirmed_user")
	private String confirmedUser;

	@Column(name = "confirmed_date")
	private Date confirmedDate;

	@JsonView(SignupInput.class)
	private String title;

	@JsonView(SignupInput.class)
	@ManyToOne
	@JoinColumn(name = "gender", referencedColumnName = "id")
	private Gender gender;

	private String ssn;

	@Column(name = "receive_messages")
	private Boolean receiveMessages;

	@Column(name = "accept_sc_terms")
	private Boolean acceptScTerms;

	@Column(name = "birth_date")
	private Date birthdate;

	@JsonView(SignupInput.class)
	@ManyToOne
	@JoinColumn(name = "pref_lang", referencedColumnName = "id")
	private LangCode prefLang;

	@ManyToOne
	@JoinColumn(referencedColumnName = "id")
	private Country nationality;

	@ManyToOne
	@JoinColumn(referencedColumnName = "id")
	private Country countryOfResidence;

	@JsonIgnore
	@OneToMany(mappedBy = "user")
	@ApiModelProperty(hidden = true)
	private List<CompanyUserRole> companyUserRoles;

	@JsonIgnore
	@OneToMany(mappedBy = "project")
	@ApiModelProperty(hidden = true)
	private List<ProjectUserRole> projectUserRoles;

	public String getUsername() {
		if (isUnsetEmail(email)) {
			return getFullMobile();
		}
		return email;
	}

	public String getFullMobile() {
		return EmailPhoneUtils
				.toFullPhoneNumber(mobileCountryCode, mobile);
	}

	public boolean isUnverified() {
		return getRoles() != null && getRoles().contains(UserUtils.Role.UNVERIFIED);
	}

	public boolean isVerified() {
		return !isUnverified();
	}

	@Override
	public UserDto toUserDto() {
		SwissConUserDTO userDto = new SwissConUserDTO();
		String[] skipProps = {"roles"};
		BeanUtils.copyProperties(this, userDto, skipProps);

		userDto.setId(Objects.requireNonNull(getId()).toString());
		userDto.setUsername(email);
		userDto.setEmail(nullIfUnsetEmail(userDto.getEmail()));
		// roles would be org.hibernate.collection.internal.PersistentSet,
		// which is not in another microservices not having Hibernate.
		// So, let's convert it to HashSet
		userDto.setRoles(new HashSet<>(roles));
		userDto.setTag(toTag());
		userDto.initialize();

		return userDto;
	}

	public static User from(SwissConUserDTO userDto) {
		User user = new User();
		String[] skipProps = {"roles"};
		BeanUtils.copyProperties(userDto, user, skipProps);

		user.setId(Integer.valueOf(userDto.getId()));
		user.setEmail(userDto.getUsername());
		// roles would be org.hibernate.collection.internal.PersistentSet,
		// which is not in another microservices not having Hibernate.
		// So, let's convert it to HashSet
		user.setRoles(new HashSet<>(userDto.getRoles()));

		return user;
	}

	public User with(SignupUserDTO signupDTO) {
		setTitle(signupDTO.getTitle());
		setGivenName(signupDTO.getFirstName());
		setSurName(signupDTO.getLastName());
		setGender(Gender.builder().id(signupDTO.getGenderId()).build());
		setPrefLang(LangCode.builder().id(signupDTO.getLanguage()).build());

		if(signupDTO.getEmail() != null) {
			setEmail(signupDTO.getEmail()); //TODO remove after SI-177
		}
		setPassword(signupDTO.getPassword()); // password for signup will be encoded in the service
		return this;
	}

	public static User from(TeamUserAddDTO teamUserAddDTO) {
		User user = new User();

		user.setGivenName(teamUserAddDTO.getFirstName());
		user.setSurName(teamUserAddDTO.getLastName());

		String emailOrPhone = teamUserAddDTO.getEmailOrPhone();

		if(EmailPhoneUtils.isEmail(emailOrPhone)) {
			user.setEmail(emailOrPhone);
		} else {
			user.setMobile(EmailPhoneUtils.toPhoneNumber(emailOrPhone));
			user.setMobileCountryCode(teamUserAddDTO.getCountryCode());
			user.setEmail(User.getRandomMail());
		}
		return user;
	}

	//TODO find a better solution for creating a user without mail
	public static String getRandomMail() {
		return UNSET_EMAIL.replace(RANDOM, RandomStringUtils.randomAlphabetic(10));
	}

	public static boolean isUnsetEmail(String aEmail) {
		if(aEmail != null) {
			return aEmail.startsWith(UNSET_EMAIL_PREFIX) && aEmail.endsWith(UNSET_EMAIL_SUFFIX);
		}
		return false;
	}

	public static String nullIfUnsetEmail(String aEmail) {
		return isUnsetEmail(aEmail) ? null : aEmail;
	}
}
