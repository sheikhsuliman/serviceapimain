package com.siryus.swisscon.api.location.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.general.favorite.Favoritable;
import com.siryus.swisscon.api.general.unit.Unit;
import com.siryus.swisscon.api.location.locationstatus.LocationStatus;
import com.siryus.swisscon.api.location.locationsubtype.LocationSubType;
import com.siryus.swisscon.api.location.locationtype.LocationType;
import com.siryus.swisscon.api.project.project.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder(toBuilder =  true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "location")
public class Location implements Favoritable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime disabled;

    @CreatedDate
    @Column(name = "created", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified", nullable = false)
    private LocalDateTime lastModifiedDate;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Integer createdBy;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private Integer lastModifiedBy;

    private String name;

    private String description;

    @Column(name = "order_index")
    private Integer order;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "start_date")
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "end_date")
    private LocalDateTime endDate;

    private BigDecimal surface;

    private BigDecimal volume;

    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;

    private Boolean starred;

    @Formula("parent")
    private Integer parentId;

    @Formula(" (select p.name from location as p where p.id = parent) ")
    private String parentName;

    @ManyToOne
    @JoinColumn(name = "project", referencedColumnName = "id", nullable = false)
    public Project project;

    @ManyToOne
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JoinColumn(name = "parent", referencedColumnName = "id")
    private Location parent;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    public LocationType type;

    @ManyToOne
    @JoinColumn(name = "sub_type", referencedColumnName = "id")
    public LocationSubType subType;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    public LocationStatus status;

    @ManyToOne
    @JoinColumn(name = "default_image", referencedColumnName = "id")
    public File defaultImage;

    @ManyToOne
    @JoinColumn(name = "default_plan", referencedColumnName = "id")
    public File defaultPlan;

    @ManyToOne
    @JoinColumn(name = "surface_unit", referencedColumnName = "id")
    public Unit surfaceUnit;

    @ManyToOne
    @JoinColumn(name = "volume_unit", referencedColumnName = "id")
    public Unit volumeUnit;

    @ManyToOne
    @JoinColumn(name = "unit", referencedColumnName = "id")
    public Unit unit;

}
