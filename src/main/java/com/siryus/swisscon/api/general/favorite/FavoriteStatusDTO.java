package com.siryus.swisscon.api.general.favorite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteStatusDTO {
    private Integer id;
    private String entityType;
    private Boolean isFavorite;
}
