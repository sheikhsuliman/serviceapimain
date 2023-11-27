package com.siryus.swisscon.api.auth.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.file.file.FileDTO;
import com.siryus.swisscon.api.general.country.Country;
import com.siryus.swisscon.api.general.gender.Gender;
import com.siryus.swisscon.api.general.langcode.LangCode;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.sql.Date;
import java.util.Optional;

/**
 * Represents the information needed when POST-ing user information on certain pages
 * <p>
 * It is used in these calls:
 * - POST /users/{id}
 *
 * @author hng
 */

@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
@ApiModel(description = "A DTO used for updating a user's own profile information")
public class UserProfileDTO {

    private final UserCategory category;

    private final String position;

    private final Integer mobileCountryCode;

    private final String mobilePhone;

    private final String officePhone;

    private final String email;

    @Reference(ReferenceType.FILE)
    private final Integer profileImageId;

    private final FileDTO profileImage;
    
    @NotNull
    private final String title;

    private final String aboutMe;

    private final String driversLicense;

    private final String car;

    private final String licensePlate;

    private final String responsibility;
    
    @NotNull
    @Reference(ReferenceType.GENDER)
    private final Integer genderId;
    
    @NotNull
    private final String firstName;

    @NotNull
    private final String lastName;
    
    @NotNull
    private final Date birthDate;

    @NotNull
    private final String ssn;
    
    @NotNull
    @Reference(ReferenceType.COUNTRY)
    private final Integer countryOfResidenceId;
    
    @NotNull
    @Reference(ReferenceType.COUNTRY)
    private final Integer nationalityId;

    @Reference(ReferenceType.LANGUAGE)
    private final String languageId;

    @JsonCreator
    public UserProfileDTO(
            @JsonProperty("category") UserCategory category,
            @JsonProperty("position") String position,
            @JsonProperty("mobileCountryCode") Integer mobileCountryCode,
            @JsonProperty("mobilePhone") String mobilePhone,
            @JsonProperty("officePhone") String officePhone,
            @JsonProperty("email") String email,
            @Reference(ReferenceType.FILE)
            @JsonProperty("profileImageId") Integer profileImageId,
            @JsonProperty("profileImage") FileDTO profileImage,
            @NotNull
            @JsonProperty("title") String title,
            @JsonProperty("aboutMe") String aboutMe,
            @JsonProperty("driversLicense") String driversLicense,
            @JsonProperty("car") String car,
            @JsonProperty("licensePlate") String licensePlate,
            @JsonProperty("responsibility") String responsibility,
            @NotNull @Reference(ReferenceType.GENDER)
            @JsonProperty("genderId") Integer genderId,
            @NotNull
            @JsonProperty("firstName") String firstName,
            @NotNull
            @JsonProperty("lastName") String lastName,
            @NotNull
            @JsonProperty("birthDate") Date birthDate,
            @NotNull
            @JsonProperty("ssn") String ssn,
            @NotNull @Reference(ReferenceType.COUNTRY)
            @JsonProperty("countryOfResidenceId") Integer countryOfResidenceId,
            @NotNull @Reference(ReferenceType.COUNTRY)
            @JsonProperty("nationalityId") Integer nationalityId,
            @Reference(ReferenceType.LANGUAGE)
            @JsonProperty("languageId") String languageId
    ) {
        this.category = category;
        this.position = position;
        this.mobileCountryCode = mobileCountryCode;
        this.mobilePhone = mobilePhone;
        this.officePhone = officePhone;
        this.email = email;
        this.profileImageId = profileImageId;
        this.profileImage = profileImage;
        this.title = title;
        this.aboutMe = aboutMe;
        this.driversLicense = driversLicense;
        this.car = car;
        this.licensePlate = licensePlate;
        this.responsibility = responsibility;
        this.genderId = genderId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.ssn = ssn;
        this.countryOfResidenceId = countryOfResidenceId;
        this.nationalityId = nationalityId;
        this.languageId = languageId;
    }


    public static UserProfileDTO fromUser(User user) {
        FileDTO picture = Optional.ofNullable(user.getPicture()).map(FileDTO::fromFile).orElse(null);

        return UserProfileDTO.builder()
                .category(user.getCategory())
                .position(user.getPosition())
                .mobileCountryCode(user.getMobileCountryCode())
                .mobilePhone(user.getMobile())
                .officePhone(user.getPhone())
                .email(user.getEmail())
                .profileImageId(Optional.ofNullable(picture).map(FileDTO::getId).orElse(null))
                .profileImage(picture)
                .title(user.getTitle())
                .aboutMe(user.getAboutMe())
                .driversLicense(user.getDriversLicense())
                .car(user.getCar())
                .licensePlate(user.getLicensePlate())
                .responsibility(user.getResponsibility())
                .genderId(Optional.ofNullable(user.getGender()).map(Gender::getId).orElse(null))
                .firstName(user.getGivenName())
                .lastName(user.getSurName())
                .birthDate(user.getBirthdate())
                .ssn(user.getSsn())
                .countryOfResidenceId(Optional.ofNullable(user.getCountryOfResidence()).map(Country::getId).orElse(null))
                .nationalityId(Optional.ofNullable(user.getNationality()).map(Country::getId).orElse(null))
                .languageId(Optional.ofNullable(user.getPrefLang()).map(LangCode::getId).orElse(null))
                .build();
    }

}
