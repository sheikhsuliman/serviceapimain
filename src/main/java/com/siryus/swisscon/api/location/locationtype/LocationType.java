package com.siryus.swisscon.api.location.locationtype;

import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.location.location.Location;
import com.siryus.swisscon.api.location.locationsubtype.LocationSubType;
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
@Table(name = "location_type")
public class LocationType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@ManyToOne
	@JoinColumn(name = "parent", referencedColumnName = "id")
	public LocationType parent;

	@JsonIgnore
	@OneToMany(mappedBy = "parent")
	@ApiModelProperty(hidden = true)
	private List<Location> children;

	@JsonIgnore
	@OneToMany(mappedBy = "mainLocationType")
	@ApiModelProperty(hidden = true)
	private List<LocationSubType> subTypes;
}
