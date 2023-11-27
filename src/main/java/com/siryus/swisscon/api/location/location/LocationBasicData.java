package com.siryus.swisscon.api.location.location;

import java.math.BigDecimal;

public interface LocationBasicData {
    String getName();
    String getDescription();
    BigDecimal getHeight();
    BigDecimal getWidth();
    BigDecimal getLength();
    BigDecimal getSurface();
    BigDecimal getVolume();

    String getUnit();
    String getSurfaceUnit();
    String getVolumeUnit();
}
