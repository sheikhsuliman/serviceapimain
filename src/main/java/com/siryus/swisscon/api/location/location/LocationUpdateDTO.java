/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.location.location;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import com.siryus.swisscon.api.util.validator.Sanitizable;
import com.siryus.swisscon.api.util.validator.SanitizableHtml;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Model used in updating a location
 * 
 * @author hng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Sanitizable
public class LocationUpdateDTO implements LocationBasicData {
    @Reference(ReferenceType.LOCATION)
    Integer id;
    
    String name;

    @SanitizableHtml
    String description;

    @Reference(ReferenceType.TEMPORARY)
    Integer imageFileId;

    @Reference(ReferenceType.TEMPORARY)
    Integer layoutFileId;
    
    BigDecimal surface;
    BigDecimal volume;
    BigDecimal length;
    BigDecimal width;
    BigDecimal height;
    
    String surfaceUnit;
    String volumeUnit;
    String unit;
}
