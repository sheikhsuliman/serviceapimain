package com.siryus.swisscon.api.company.company;

import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.file.file.FileDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDetailsDTO {

    private Integer id;
    private String name;
    private String companyEmail;
    private String address1;
    private String address2;
    private String phone;
    private String plz;
    private String city;
    private Integer countryId;
    private Integer accountTypeId;
    private Integer[] tradeIds;
    private FileDTO picture;
    private String description;

    private List<TeamUserDTO> team;

    public static CompanyDetailsDTO from(Company company, List<TeamUserDTO> team) {

        Integer[] tradeIds = company.getCompanyTrades()
                .stream()
                .map(ct -> ct.getTrade())
                .toArray(Integer[]::new);

        return CompanyDetailsDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .companyEmail(company.getCompanyEmail())
                .address1(company.getAddress1())
                .address2(company.getAddress2())
                .phone(company.getPhone())
                .plz(company.getPlz())
                .city(company.getCity())
                .countryId(company.getCountry() != null ? company.getCountry().getId() : null)
                .tradeIds(tradeIds)
                .picture(FileDTO.fromFile(company.getPicture()))
                .description(company.getDescription())
                .team(team)
                .build();

    }
}
