package com.siryus.swisscon.api.general.favorite;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.siryus.swisscon.api.auth.user.User;
import lombok.*;
import org.hibernate.annotations.Any;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.Formula;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "favorite")
public class Favorite {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NotNull
	@ManyToOne
	@JoinColumn(name="user_id", referencedColumnName = "id", nullable = false)
	private User user;

	@Column(name="reference_id")
	private Integer referenceId;

	@Column(name = "reference_type")
	private String referenceType;
	
	
	
}
