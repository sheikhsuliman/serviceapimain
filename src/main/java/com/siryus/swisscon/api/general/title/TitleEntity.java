package com.siryus.swisscon.api.general.title;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="title")
public class TitleEntity {
    @Id
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
