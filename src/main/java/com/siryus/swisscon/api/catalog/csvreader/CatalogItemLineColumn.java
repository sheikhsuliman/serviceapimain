package com.siryus.swisscon.api.catalog.csvreader;

import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CatalogItemLineColumn {

    private Integer index;
    private String header;
    private BiConsumer<CatalogItemLine, String> setterFromLine;
    private Function<CatalogItemLine, String> getter;
    private BiFunction<CatalogVariationEntity, Map<Integer, String>, String> variationGetter;

    public CatalogItemLineColumn(Integer index,
                                 String header,
                                 BiConsumer<CatalogItemLine, String> setterFromLine,
                                 Function<CatalogItemLine, String> getter,
                                 BiFunction<CatalogVariationEntity, Map<Integer, String>, String> variationGetter) {
        this.index = index;
        this.header = header;
        this.setterFromLine = setterFromLine;
        this.getter = getter;
        this.variationGetter = variationGetter;
    }

    public void setValueFromFields(CatalogItemLine line, List<String> fields) {
        setterFromLine.accept(line, getOrNull(fields, index));
    }

    public String getValueFromLine(CatalogItemLine line) {
        return getter.apply(line);
    }

    public void setValueFromVariation(CatalogItemLine line, CatalogVariationEntity variation, Map<Integer, String> levelNames) {
        if(variation != null) {
            setterFromLine.accept(line, variationGetter.apply(variation, levelNames));
        }
    }

    public String getHeader() {
        return header;
    }

    public static String getOrNull(List<String> fields, int index) {
        if (index < fields.size()) {
            String value = fields.get(index);
            return StringUtils.defaultIfBlank(value, null);
        }
        return null;
    }

}
