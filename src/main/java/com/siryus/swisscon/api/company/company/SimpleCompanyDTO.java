package com.siryus.swisscon.api.company.company;

import com.siryus.swisscon.api.file.file.FileDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimpleCompanyDTO {

    private Integer id;
    private String name;
    private FileDTO picture;

    public static SimpleCompanyDTO from(Company company) {
        return SimpleCompanyDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .picture(FileDTO.fromFile(company.getPicture()))
                .build();
    }
}
