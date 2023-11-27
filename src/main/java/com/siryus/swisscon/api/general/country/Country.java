package com.siryus.swisscon.api.general.country;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.general.langcode.LangCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "country")
public class Country {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;

	private String code;

	private String nationality;
	
	private String path;	
	
	private String type;
	
	
	@Formula("parent_id")
	private Integer parentId;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@ManyToOne
	@JoinColumn(referencedColumnName = "id")
	private Country parent;
	
	@ManyToOne
	@JoinColumn(name = "default_language", referencedColumnName = "id")
	public LangCode defaultLanguage;


	public static Country ref(Integer id) {
		return  id == null ? null : Country.builder().id(id).build();
	}
}
