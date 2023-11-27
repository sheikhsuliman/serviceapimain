package com.siryus.swisscon.api.location.location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author hng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationTreeDTO extends LocationDetailsDTO {

    List<LocationTreeDTO> children = new ArrayList<>();

    public LocationTreeDTO(LocationDetailsDTO data) {
        this.id = data.id;
        this.description = data.description;
        this.height = data.height;
        this.image = data.image;
        this.layout = data.layout;
        this.length = data.length;
        this.name = data.name;
        this.surface = data.surface;
        this.surfaceUnit = data.surfaceUnit;
        this.unit = data.unit;
        this.volume = data.volume;
        this.volumeUnit = data.volumeUnit;
        this.width = data.width;
        this.hasChildren = data.hasChildren;
    }

    public static LocationTreeDTO from(Location location, Boolean hasChildren) {
        return new LocationTreeDTO(LocationDetailsDTO.from(location, l-> hasChildren));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LocationTreeDTO.class.getSimpleName() + "[", "]")
                .add("children=" + children)
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("description='" + description + "'")
                .add("image=" + image)
                .add("layout=" + layout)
                .add("surface=" + surface)
                .add("volume=" + volume)
                .add("length=" + length)
                .add("width=" + width)
                .add("height=" + height)
                .add("surfaceUnit=" + surfaceUnit)
                .add("volumeUnit=" + volumeUnit)
                .add("unit=" + unit)
                .add("hasChildren=" + hasChildren)
                .toString();
    }
}
