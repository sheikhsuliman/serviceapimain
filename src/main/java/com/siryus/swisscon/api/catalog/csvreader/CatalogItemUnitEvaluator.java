package com.siryus.swisscon.api.catalog.csvreader;

import com.siryus.swisscon.api.general.unit.Unit;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

public class CatalogItemUnitEvaluator {

    private CatalogItemUnitEvaluator() {
        throw new IllegalStateException("Util class not instantiable");
    }

    public static Unit evaluateUnit(CatalogItemLine line, Map<String, Unit> units) {
        if(StringUtils.isNotBlank(line.getTask())) {
            return evaluateUnitFromPosition(line, units);
        }
        return null;
    }

    private static Unit evaluateUnitFromPosition(CatalogItemLine line, Map<String, Unit> units) {
        String unit = line.getCompanyUnit() != null ? line.getCompanyUnit() : line.getUnit();
        if (unit != null) {
            return Optional
                    .ofNullable(units.get(unit))
                    .orElseThrow(()-> CatalogCSVException.unitWithSymbolNotFound(line.getUnit()));
        }
        throw CatalogCSVException.positionHasNoUnit(line.getSnpNumber());
    }

}
