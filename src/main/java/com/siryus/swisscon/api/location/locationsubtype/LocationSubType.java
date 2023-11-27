package com.siryus.swisscon.api.location.locationsubtype;

import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.siryus.swisscon.api.location.location.Location;
import com.siryus.swisscon.api.location.locationtype.LocationType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "location_sub_type")
public class LocationSubType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;

	@ManyToOne
	@JoinColumn(name = "main_type", referencedColumnName = "id", nullable = false)
	public LocationType mainLocationType;

	@JsonIgnore
	@OneToMany(mappedBy = "subType")
	@ApiModelProperty(hidden = true)
	private List<Location> locations;

}
