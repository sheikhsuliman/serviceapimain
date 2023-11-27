package com.siryus.swisscon.api.tasks.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.general.unit.UnitDTO;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;

import java.math.BigDecimal;
import java.util.List;

public class CompanyCatalogItemDTO {
    private final Integer id;
    private final String snpNumber;
    private final Integer variationNumber;
    private final Integer companyId;
    private final String companyVariation;
    private final UnitDTO unit;
    private final BigDecimal price;
    private final List<String> checkListItems;

    @JsonCreator
    public CompanyCatalogItemDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("snpNumber") String snpNumber,
            @JsonProperty("variationNumber") Integer variationNumber,
            @JsonProperty("companyId") Integer companyId,
            @JsonProperty("companyVariation") String companyVariation,
            @JsonProperty("unit") UnitDTO unit,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("checkListItems") List<String> checkListItems
    ) {
        this.id = id;
        this.snpNumber = snpNumber;
        this.companyId = companyId;
        this.variationNumber = variationNumber;
        this.companyVariation = companyVariation;
        this.unit = unit;
        this.price = price;
        this.checkListItems = checkListItems;
    }

    public static CompanyCatalogItemDTO from(CatalogVariationEntity entity) {
        if (entity == null) {
            return null;
        }
        return new CompanyCatalogItemDTO(
                entity.getId(),
                entity.getSnp(),
                entity.getVariationNumber(),
                entity.getCompanyId(),
                entity.getTaskName(),
                UnitDTO.from(entity.getUnit()),
                entity.getPrice(),
                entity.getCheckListItems()
        );
    }

    public Integer getId() {
        return id;
    }

    public String getSnpNumber() {
        return snpNumber;
    }

    public Integer getVariationNumber() {
        return variationNumber;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public String getCompanyVariation() {
        return companyVariation;
    }

    public UnitDTO getUnit() {
        return unit;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public List<String> getCheckListItems() {
        return checkListItems;
    }
}
