package com.siryus.swisscon.api.company.company;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactDetailDTO {

    private String address1;
    private String address2;
    private String phone;
    private String plz;
    private String city;
    private String webUrl;
    private String companyEmail;
    private String fax;

    @Reference(ReferenceType.COUNTRY)
    private Integer countryId;

    public static ContactDetailDTO fromCompany(Company company) {
        ContactDetailDTO contactDetailDTO = new ContactDetailDTO();

        contactDetailDTO.setAddress1(company.getAddress1());
        contactDetailDTO.setAddress2(company.getAddress2());
        contactDetailDTO.setPhone(company.getPhone());
        contactDetailDTO.setPlz(company.getPlz());
        contactDetailDTO.setCity(company.getCity());
        contactDetailDTO.setWebUrl(company.getWebUrl());
        contactDetailDTO.setCompanyEmail(company.getCompanyEmail());
        contactDetailDTO.setFax(company.getFax());
        contactDetailDTO.setCountryId(company.getCountry().getId());

        return contactDetailDTO;
    }
}
