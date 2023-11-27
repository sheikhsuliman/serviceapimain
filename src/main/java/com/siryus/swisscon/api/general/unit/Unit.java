package com.siryus.swisscon.api.general.unit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "unit"/* , schema = "registration", catalog = "" */)
public class Unit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;
	private String symbol;

	@Column(name = "order_index")
	private Integer order;

	@Column(name = "frequently_used")
	private Boolean frequentlyUsed;

	private Boolean length;
	private Boolean surface;
	private Boolean volume;

	@Column(name = "is_force")
	private Boolean force;
	@Column(name = "is_power")
	private Boolean power;
	@Column(name = "is_time")
	private Boolean time;
	@Column(name = "is_weight")
	private Boolean weight;
	@Column(name = "is_illuminance")
	private Boolean illuminance;
	@Column(name = "is_density")
	private Boolean density;
	@Column(name = "is_quantity")
	private Boolean quantity;

	@Column(name = "fixed_price")
	private Boolean fixedPrice;
	
	@Formula("base_unit")
	private Integer baseUnitId;	
	
	@ManyToOne
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@JoinColumn(name = "base_unit", referencedColumnName = "id")
	private Unit baseUnit;

	public static Unit ref(Integer id) {
		return id != null ? Unit.builder().id(id).build() : null;
	}

}
