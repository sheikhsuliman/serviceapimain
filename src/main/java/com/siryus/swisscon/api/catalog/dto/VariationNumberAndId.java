package com.siryus.swisscon.api.catalog.dto;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class VariationNumberAndId {
    private final Integer variationNumber;
    private final Integer id;

    public VariationNumberAndId(Integer variationNumber, Integer id) {
        this.variationNumber = variationNumber;
        this.id = id;
    }

    public Integer getVariationNumber() {
        return variationNumber;
    }

    public Integer getId() {
        return id;
    }

    public static List<Integer> combine(List<VariationNumberAndId> companyCatalog, List<VariationNumberAndId> globalCatalog) {
        var companyCatalogMap = companyCatalog.stream().collect(Collectors.toMap(VariationNumberAndId::getVariationNumber, VariationNumberAndId::getId));
        var globalCatalogMap = globalCatalog.stream().collect(Collectors.toMap(VariationNumberAndId::getVariationNumber, VariationNumberAndId::getId));

        var combinedMap = new HashMap<>(globalCatalogMap);
        combinedMap.putAll(companyCatalogMap);

        List<VariationNumberAndId> combinedList =combinedMap.entrySet().stream().map(e -> new VariationNumberAndId(e.getKey(), e.getValue())).collect(
                Collectors.toList());

        combinedList.sort(Comparator.comparingInt(VariationNumberAndId::getVariationNumber));

        return combinedList.stream().map(VariationNumberAndId::getId).collect(Collectors.toList());
    }

}
