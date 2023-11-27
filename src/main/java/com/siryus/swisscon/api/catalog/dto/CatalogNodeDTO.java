package com.siryus.swisscon.api.catalog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeEntity;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Builder(toBuilder = true)
public class CatalogNodeDTO {
    private final Integer id;
    private final Integer companyId;
    private final String snp;
    private final String parentSnp;
    private final boolean leaf;
    private final String name;

    private final LocalDateTime created;
    private final Integer createdBy;

    private final LocalDateTime disabled;

    private final List<CatalogNodeDTO> children;

    private final List<CatalogVariationDTO> variations;

    @JsonCreator
    public CatalogNodeDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("companyId") Integer companyId,
            @JsonProperty("snp") String snp,
            @JsonProperty("parentSnp") String parentSnp,
            @JsonProperty("leaf") boolean leaf,
            @JsonProperty("name") String name,
            @JsonProperty("created") LocalDateTime created,
            @JsonProperty("createdBy") Integer createdBy,
            @JsonProperty("disabled") LocalDateTime disabled,
            @JsonProperty("children") List<CatalogNodeDTO> children,
            @JsonProperty("variations") List<CatalogVariationDTO> variations
    ) {
        this.id = id;
        this.companyId = companyId;
        this.snp = snp;
        this.parentSnp = parentSnp;
        this.leaf = leaf;
        this.name = name;
        this.created = created;
        this.createdBy = createdBy;
        this.disabled = disabled;
        this.children = children == null ? null : Collections.unmodifiableList(children);
        this.variations = variations == null ? null : Collections.unmodifiableList(variations);
    }

    public static CatalogNodeDTO from(CatalogNodeEntity entity, boolean leaf) {
        return from(entity, null, leaf, 0);
    }

    public static CatalogNodeDTO from(
            CatalogNodeEntity nodeEntity,
            List<CatalogVariationEntity> variations,
            boolean leaf
    ) {
        return from(nodeEntity, variations, leaf, variations.size());
    }

    public static CatalogNodeDTO from(
            CatalogNodeEntity nodeEntity,
            List<CatalogVariationEntity> variations,
            boolean leaf,
            int maxGlobalCatalogVariation
    ) {
        if (nodeEntity == null) {
            return null;
        }
        return new CatalogNodeDTO(
                nodeEntity.getId(),
                nodeEntity.getCompanyId(),
                nodeEntity.getSnp(),
                nodeEntity.getParentSnp(),
                leaf,
                nodeEntity.getName(),
                nodeEntity.getCreatedDate(),
                nodeEntity.getCreatedBy(),
                nodeEntity.getDisabled(),
                null,
                CatalogVariationDTO.variationDTOsFromEntities(variations, maxGlobalCatalogVariation)
        );
    }

    public CatalogNodeDTO withChildren(List<CatalogNodeDTO> children) {
        return  this.toBuilder().children(children).build();
    }

    public Integer getId() {
        return id;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public String getSnp() {
        return snp;
    }

    public String getParentSnp() {
        return parentSnp;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getDisabled() {
        return disabled;
    }

    public List<CatalogNodeDTO> getChildren() {
        return children;
    }

    public List<CatalogVariationDTO> getVariations() {
        return variations;
    }
}
