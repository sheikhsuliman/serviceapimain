/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.auth.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.file.file.FileDTO;

public class AuthorDTO {
    private final Integer id;
    private final String firstName;
    private final String lastName;
    private final FileDTO image;

    @JsonCreator
    public AuthorDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("image") FileDTO image
    ) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.image = image;
    }

    public static AuthorDTO from(User u) {
        return new AuthorDTO(
                u.getId(),
                u.getGivenName(),
                u.getSurName(),
                FileDTO.fromFile(u.getPicture())
        );
    }

    public static AuthorDTO from(TeamUserDTO u) {
        return new AuthorDTO(
                u.getId(),
                u.getFirstName(),
                u.getLastName(),
                u.getPicture()
        );
    }

    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public FileDTO getImage() {
        return image;
    }
}
