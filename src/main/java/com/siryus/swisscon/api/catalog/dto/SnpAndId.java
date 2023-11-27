package com.siryus.swisscon.api.catalog.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SnpAndId {
    private final String snp;
    private final int id;

    public SnpAndId(String snp, int id) {
        this.snp = snp;
        this.id = id;
    }

    public String getSnp() {
        return snp;
    }

    public int getId() {
        return id;
    }

    public static List<Integer> combine(List<SnpAndId> companyCatalog, List<SnpAndId> globalCatalog) {
        Map<String, Integer> companyMap = companyCatalog.stream().collect(Collectors.toMap(SnpAndId::getSnp, SnpAndId::getId));
        Map<String, Integer> globalMap = globalCatalog.stream().collect(Collectors.toMap(SnpAndId::getSnp, SnpAndId::getId));

        Map<String, Integer> resultMap = new HashMap<>(globalMap);
        resultMap.keySet().stream().forEach( k -> {
            if (companyMap.containsKey(k)) {
                resultMap.put(k, companyMap.get(k));
            }
        });

        return new ArrayList<>(resultMap.values());
    }

}
