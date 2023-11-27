/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.auth.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.company.CompanyDashboardDTO;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.file.file.FileDTO;
import com.siryus.swisscon.api.general.gender.Gender;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the information needed when GET-ing user information on certain pages
 * 
 * It is used in these calls:
 * - GET /dashboard/init
 * 
 * @author hng
 */
@Getter
@Setter
@EqualsAndHashCode
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "A DTO used for retrieving user information for Dashboard page")
public class DashboardUserDTO {
    Integer id;
    UserCategory category;
    String position;
    String title;
    Gender gender;
    String firstName;
    String lastName;
    CompanyDashboardDTO company;
    FileDTO picture;
    List<Role> roles;
    Boolean unverified;
    String email;
    Boolean isReceive;
    String aboutMe;
    String driversLicense;
    String car;
    String licensePlate;
    String responsibility;
    String ssn;
    Integer mobileCountryCode;
    String mobilePhone;
    String language;
    Integer nationality;
    Integer countryOfResidence;

    @JsonSerialize(using = DateSerializer.class)
    @JsonDeserialize(using = DateDeserializers.DateDeserializer.class)
    Date birthDate;

    String rabbitJwt;
    
    /**
     * Builds a UserDTO object by extracting some fields from User
     */
    public static DashboardUserDTO from(User user) {
        if (user == null) {
            return null;            
        }
        
        List<CompanyUserRole> cur =  user.getCompanyUserRoles();
        List<Role> roles = cur.stream().map(CompanyUserRole::getRole).collect(Collectors.toList());
        
        Company c = null;
        if (!cur.isEmpty()) {
            c = cur.get(0).getCompany();
        }

        return DashboardUserDTO.builder()
                .id(user.getId())
                .category(user.getCategory())
                .position(user.getPosition())
                .title(user.getTitle())
                .gender(user.getGender())
                .firstName(user.getGivenName())
                .lastName(user.getSurName())
                .company(CompanyDashboardDTO.from(c, user.getId()))
                .picture(FileDTO.fromFile(user.getPicture()))
                .roles(roles)
                .unverified(user.isUnverified())
                .email(User.nullIfUnsetEmail(user.getEmail()))
                .mobileCountryCode(user.getMobileCountryCode())
                .mobilePhone(user.getMobile())
                .ssn(user.getSsn())
                .language(user.getPrefLang() != null ? user.getPrefLang().getId() : null)
                .isReceive(user.getReceiveMessages())
                .aboutMe(user.getAboutMe())
                .driversLicense(user.getDriversLicense())
                .car(user.getCar())
                .licensePlate(user.getLicensePlate())
                .responsibility(user.getResponsibility())
                .nationality(user.getNationality() != null ? user.getNationality().getId() : null)
                .countryOfResidence(user.getCountryOfResidence() != null ? user.getCountryOfResidence().getId() : null)
                .birthDate(user.getBirthdate())
                .build();
    }    
}
