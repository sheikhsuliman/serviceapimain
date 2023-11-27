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

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Model used when creating a Location
 * 
 * @author hng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Sanitizable
public class LocationCreateDTO implements LocationBasicData {
    @NotNull
    @NotEmpty
    @Size(max = 255)
    String name;

    @SanitizableHtml
    @Size(max = 1024)
    String description;

    @Reference(ReferenceType.TEMPORARY)
    Integer imageFileId;

    @Reference(ReferenceType.TEMPORARY)
    Integer layoutFileId;

    @Positive
    BigDecimal surface;
    @Positive
    BigDecimal volume;
    @Positive
    BigDecimal length;
    @Positive
    BigDecimal width;
    @Positive
    BigDecimal height;

    String surfaceUnit;

    String volumeUnit;

    String unit;

    @Reference(ReferenceType.LOCATION)
    Integer parent;
    
    /**
     * Optional field, needs to be present when (and only then) attaching children at root level 
     * because a pseudo root node needs to be created once. In all other instances parent id will 
     * be used.
     */
    @Reference(ReferenceType.PROJECT)
    Integer projectId; 
}
