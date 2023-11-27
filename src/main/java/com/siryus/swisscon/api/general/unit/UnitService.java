package com.siryus.swisscon.api.general.unit;

import com.siryus.swisscon.api.general.GeneralException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

@Service("unitService")
public class UnitService {
    private final UnitRepository unitRepository;

    @Autowired
    public UnitService(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    public Unit findBySymbolName(String symbolName) {
        if (null == symbolName || symbolName.isEmpty()) {
            return null;
        }
        
        return unitRepository.findBySymbolName(symbolName);
    }

    public Unit findVolumeUnitBySymbolName(String symbolName) {
        Unit unit = findBySymbolName(symbolName);

        return unit.getVolume() ? unit : null;
    }

    public Unit findSurfaceUnitBySymbolName(String symbolName) {
        Unit unit = findBySymbolName(symbolName);

        return unit.getSurface() ? unit : null;
    }

    public UnitDTO getUnitById(Integer unitId) {
        return UnitDTO.from(unitRepository.findById(unitId)
                .orElseThrow(() -> GeneralException.unitNotFound(unitId))
        );
    }
}
