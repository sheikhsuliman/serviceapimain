/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.company.company;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.base.SimpleEntityDTO;
import com.siryus.swisscon.api.company.companytrade.CompanyTrade;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.numworkersofcompany.NumWorkersOfCompany;
import com.siryus.swisscon.api.file.file.FileDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the information needed when loading Company information in dashboard page
 * <p>
 * It is used in these calls:
 * - /dashboard/init
 *
 * @author hng
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDashboardDTO {
    private Integer id;
    private String name;
    private String director;
    private List<Integer> trades;
    private NumWorkersOfCompany numberOfEmployees;
    private String address1;
    private String address2;
    private String phone;
    private String fax;
    private String webUrl;
    private String companyEmail;
    private String description;
    private String plz;
    private String city;
    private SimpleEntityDTO country;
    private Integer legalTypeId;
    private FileDTO picture;
    private List<Role> roles; // List of all roles this user has in this company; will probably be removed in the future when Roles system is in place
    private String vatNumber;
    private String taxNumber;
    private String registerEntryNumber;
    
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lastModified;
    /**
     * Builds a CompanyDashboardDTO object by extracting some fields from Company
     *
     * @param company the company to convert from
     * @param userId  optional, if not null, it will (for now) filter the roles of this user within that company
     * @return
     */
    public static CompanyDashboardDTO from(Company company, Integer userId) {
        if (company == null) {
            return null;
        }

        CompanyDashboardDTOBuilder b = CompanyDashboardDTO.builder();

        /*
         * Get all the roles that this user has in this company
         * After roles will be implemented in the backend, it is likely that
         * this information will not be returned anymore to the frontend so
         * THIS IS JUST TEMPORARY
         */
        List<CompanyUserRole> roles = company.getCompanyUserRoles();
        if (userId != null) {
            roles = roles.stream()
                    .filter(role -> Objects.requireNonNull(role.getUser().getId()).equals(userId))
                    .collect(Collectors.toList());
        }

        SimpleEntityDTO countryDTO = SimpleEntityDTO.builder().id(company.getCountry().getId()).name(company.getCountry().getCode()).build();
        Integer legalTypeId =  company.getLegalType() != null ? company.getLegalType().getId() : null;

        return b.id(company.getId())
                .name(company.getName())
                .director(company.getDirector())
                .trades(company.getCompanyTrades().stream().map(CompanyTrade::getTrade).collect(Collectors.toList()))
                .numberOfEmployees(company.getNumberOfEmployees())
                .companyEmail(company.getCompanyEmail())
                .phone(company.getPhone())
                .fax(company.getFax())
                .plz(company.getPlz())
                .address1(company.getAddress1())
                .address2(company.getAddress2())
                .webUrl(company.getWebUrl())
                .description(company.getDescription())
                .legalTypeId(legalTypeId)
                .city(company.getCity())
                .country(countryDTO)
                .picture(FileDTO.fromFile(company.getPicture()))
                .roles(roles.stream()
                        .map(CompanyUserRole::getRole)
                        .collect(Collectors.toList()))
                .vatNumber(company.getVatNumber())
                .taxNumber(company.getTaxNumber())
                .registerEntryNumber(company.getRegisterEntryNumber())
                .lastModified(company.getLastModifiedDate())
                .build();

    }
}
