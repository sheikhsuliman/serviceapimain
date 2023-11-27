package com.siryus.swisscon.api.catalog.repos;

import com.siryus.swisscon.api.catalog.dto.CatalogVariationDTO;
import com.siryus.swisscon.api.general.unit.Unit;
import com.siryus.swisscon.api.general.unit.UnitDTO;
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Builder(toBuilder = true)
@Getter
@Setter
@Entity
@Table(name = "catalog_variation")
public class CatalogVariationEntity {
    public static final String CHECK_LIST_ITEMS_DELIMITER = "\n";

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

    @Column(name = "snp")
    private String snp;

    @Column(name = "global_catalog_node_id")
    private Integer catalogNodeId;

    @Column(name = "variation_number")
    private Integer variationNumber;

    @Column(name = "active")
    private boolean active;

    @Column(name = "task_name")
    private String taskName;

    @Column(name = "task_variation")
    private String taskVariation;

    @Column(name = "check_list", length = 4000)
    private String checkList;

    @ManyToOne
    @JoinColumn(name = "unit_id", referencedColumnName = "id")
    private Unit unit;

    @Column(name = "price")
    private BigDecimal price;

    public CatalogVariationEntity() {
        // Public no arg constructor required by @Entity
    }

    public CatalogVariationEntity(
            Integer id,
            Integer companyId,
            Integer createdBy,
            LocalDateTime createdDate,
            String snp,
            Integer catalogNodeId,
            Integer variationNumber,
            boolean active,
            String taskName,
            String taskVariation,
            String checkList,
            Unit unit,
            BigDecimal price
    ) {
        this.id = id;
        this.companyId = companyId;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.snp = snp;
        this.catalogNodeId = catalogNodeId;
        this.variationNumber = variationNumber;
        this.active = active;
        this.taskName = taskName;
        this.taskVariation = taskVariation;
        this.checkList = checkList;
        this.unit = unit;
        this.price = price;
    }

    public CatalogVariationEntity(
            Integer companyId,
            String snp,
            Integer catalogNodeId,
            Integer variationNumber,
            boolean active,
            String taskName,
            String taskVariation,
            String checkList,
            Unit unit,
            BigDecimal price
    ) {
        this.companyId = companyId;
        this.snp = snp;
        this.catalogNodeId = catalogNodeId;
        this.variationNumber = variationNumber;
        this.active = active;
        this.taskName = taskName;
        this.taskVariation = taskVariation;
        this.checkList = checkList;
        this.unit = unit;
        this.price = price;
    }
    public List<String> getCheckListItems() {
        return checkList != null ? Arrays.asList(checkList.split(CHECK_LIST_ITEMS_DELIMITER)) : null;
    }

    public boolean same(CatalogVariationDTO dto) {
        return dto.getDescription().equals(this.getTaskName()) &&
                Objects.equals(dto.getVariant(), this.getTaskVariation()) &&
                Objects.equals(
                        Optional.ofNullable(dto.getUnit()).map(UnitDTO::getId).orElse(null),
                        Optional.ofNullable(this.getUnit()).map(Unit::getId).orElse(null)
                ) &&
                Objects.equals(dto.getCheckListItemsString(), this.getCheckList()) &&
                Objects.equals(dto.getPrice(), this.getPrice()) &&
                Objects.equals(dto.isActive(), this.isActive());
    }
}
