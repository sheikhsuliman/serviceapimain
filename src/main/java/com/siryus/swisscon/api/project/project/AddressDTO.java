package com.siryus.swisscon.api.project.project;

import com.siryus.swisscon.api.general.country.Country;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private String address;
    private String postalCode;
    private String city;
    private Integer countryId;

    public static AddressDTO from(Project project) {
        AddressDTO address = new AddressDTO();

        address.address = project.getStreet();
        address.postalCode = project.getCode();
        address.city = project.getCity();
        final Country country = project.getCountry();
        if (null != country) {
            address.countryId = country.getId();
        }

        return address;
    }
}
