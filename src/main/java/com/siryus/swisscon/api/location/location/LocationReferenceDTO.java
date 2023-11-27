package com.siryus.swisscon.api.location.location;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.location.locationtype.LocationType;

public class LocationReferenceDTO {
    private final Integer id;
    private final Integer parentId;
    private final String name;
    private final LocationType type;

    @JsonCreator
    public LocationReferenceDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("parentId")  Integer parentId,
            @JsonProperty("name")  String name,
            @JsonProperty("type")  LocationType type
    ) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public Integer getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public LocationType getType() {
        return type;
    }
}
