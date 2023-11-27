package com.siryus.swisscon.api.catalog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;
import com.siryus.swisscon.api.general.unit.UnitDTO;
import lombok.Builder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Builder(toBuilder = true)
public class CatalogVariationDTO {
    private final Integer id;

    private final Integer companyId;

    @NotNull
    @Positive
    private final Integer variationNumber;
    private final boolean active;
    private final String description;
    private final String variant;
    private final List<String> checkListItems;
    private final UnitDTO unit;

    @Positive
    private final BigDecimal price;

    private final boolean companySpecific;

    @JsonCreator
    public CatalogVariationDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("companyId") Integer companyId,
            @JsonProperty("variationNumber") Integer variationNumber,
            @JsonProperty("active") boolean active,
            @JsonProperty("description") String description,
            @JsonProperty("variant") String variant,
            @JsonProperty("checkListItems") List<String> checkListItems,
            @JsonProperty("unit") UnitDTO unit,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("companySpecific") boolean companySpecific
    ) {
        this.id = id;
        this.companyId = companyId;
        this.variationNumber = variationNumber;
        this.active = active;
        this.description = description;
        this.variant = variant;
        this.checkListItems = checkListItems;
        this.unit = unit;
        this.price = price;
        this.companySpecific = companySpecific;
    }
    public static CatalogVariationDTO from(CatalogVariationEntity entity) {
        return from(entity, false);
    }

    public static CatalogVariationDTO from(CatalogVariationEntity entity, boolean companySpecific) {
        return new CatalogVariationDTO(
                entity.getId(),
                entity.getCompanyId(),
                entity.getVariationNumber(),
                entity.isActive(),
                entity.getTaskName(),
                entity.getTaskVariation(),
                splitCheckListItems(entity.getCheckList()),
                UnitDTO.from(entity.getUnit()),
                entity.getPrice(),
                companySpecific
        );
    }

    public static List<CatalogVariationDTO> variationDTOsFromEntities(List<CatalogVariationEntity> variations, int maxGlobalCatalogVariation) {
        if (variations == null || variations.isEmpty()) {
            return null;
        }

        return variations.stream()
                .filter(v -> v.getVariationNumber() <= maxGlobalCatalogVariation || v.isActive())
                .map( v -> CatalogVariationDTO.from(v, v.getVariationNumber() > maxGlobalCatalogVariation))
                .collect(Collectors.toList());
    }

    public Integer getId() {
        return id;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getVariationNumber() {
        return variationNumber;
    }

    public boolean isActive() {
        return active;
    }

    public String getDescription() {
        return description;
    }

    public String getVariant() {
        return variant;
    }

    public UnitDTO getUnit() {
        return unit;
    }

    public BigDecimal getPrice() { return price; }

    public List<String> getCheckListItems() {
        return checkListItems;
    }

    @JsonIgnore
    public String getCheckListItemsString() {
        return joinCheckListItems(this.checkListItems);
    }

    private static List<String> splitCheckListItems(String checkListItemsString) {
        if (checkListItemsString == null) {
            return null;
        }

        return Arrays.asList(checkListItemsString.split("\n"));
    }

    private static String joinCheckListItems(List<String> checkListItems) {
        return checkListItems == null ? null : String.join("\n", checkListItems);
    }

    public boolean isCompanySpecific() {
        return companySpecific;
    }
}
