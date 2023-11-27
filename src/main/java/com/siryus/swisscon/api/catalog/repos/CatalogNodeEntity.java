package com.siryus.swisscon.api.catalog.repos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@Setter
@Entity
@Table(name = "global_catalog_node")
public class CatalogNodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "company_id")
    private Integer companyId;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Integer createdBy;

    @CreatedDate
    @Column(name = "created", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    private String snp;

    @Column(name = "parent_snp")
    private String parentSnp;

    private String name;

    private LocalDateTime disabled;

    public CatalogNodeEntity() {
    }

    public CatalogNodeEntity(
            Integer id,
            Integer companyId,
            Integer createdBy,
            LocalDateTime createdDate,
            String snp,
            String parentSnp,
            String name,
            LocalDateTime disabled
    ) {
        this.id = id;
        this.companyId = companyId;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.snp = snp;
        this.parentSnp = parentSnp;
        this.name = name;
        this.disabled = disabled;
    }

}
