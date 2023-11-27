package com.siryus.swisscon.api.taskworklog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.file.file.FileDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder(toBuilder = true)
public class UserInfoEventHistoryDTO {
    private final Integer id;
    private final String firstName;
    private final String lastName;
    private final FileDTO image;

    @JsonCreator
    public UserInfoEventHistoryDTO(
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

    /**
     * Builds a UserDTO object by extracting some fields from User
     */
    public static UserInfoEventHistoryDTO from(User user) {
        if (user == null) {
            return null;
        }
        return builder()
                .id(user.getId())
                .firstName(user.getGivenName())
                .lastName(user.getSurName())
                .image(FileDTO.fromFile(user.getPicture()))
                .build();
    }
}
