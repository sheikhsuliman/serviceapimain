package com.siryus.swisscon.api.auth.user;

import java.sql.Date;

import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.general.country.Country;
import com.siryus.swisscon.api.general.gender.Gender;
import com.siryus.swisscon.api.general.langcode.LangCode;
import io.swagger.annotations.ApiModel;
import lombok.*;

import org.springframework.beans.BeanUtils;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "A DTO extending lemon's UserDto to add SwissCon custom properties")
public class SwissConUserDTO extends UserDto {

    public static SwissConUserDTO fromUser(User source) {
        SwissConUserDTO dto = new SwissConUserDTO();
        BeanUtils.copyProperties(source, dto);
        dto.setId(source.getId().toString());
        return dto;
    }

    private String userPool;

    private String phone;

    private String mobile;

    private String email;

    private String name;

    private File picture;

    private String idCardUrl;

    private String aboutMe;

    private String webSiteUrl;

    private String givenName;

    private String surName;

    private String title;

    private Gender gender;

    private String ssn;

    private Boolean receiveMessages;

    private Boolean acceptScTerms;

    private Date birthdate;

    private LangCode prefLang;

    private Country nationality;

    private Country countryOfResidence;
}
