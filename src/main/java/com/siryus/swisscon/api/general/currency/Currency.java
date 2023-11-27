package com.siryus.swisscon.api.general.currency;


import javax.persistence.*;

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
@Table(name = "currency")
public class Currency {

	@Id
	String id;

	@Column(name = "name")
	private String name;

}
