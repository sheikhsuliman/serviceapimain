/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.location.location;

import com.siryus.swisscon.api.file.file.FileDTO;
import com.siryus.swisscon.api.general.unit.UnitDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents what needs to be returned to the frontend for the location tree representation. 
 * 
 * Ids contained within are Location ids and NOT LocationNode ids. 
 * 
 * @author hng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationDetailsDTO {
    Integer id;

    Integer parentId;

    String name;
    
    String description;

    FileDTO image;
    
    FileDTO layout;   
    
    BigDecimal surface;
    BigDecimal volume;
    BigDecimal length;
    BigDecimal width;
    BigDecimal height;

    UnitDTO surfaceUnit;
    UnitDTO volumeUnit;
    UnitDTO unit;

    Integer order;

    boolean hasChildren;

    private List<LocationReferenceDTO> locationPath = null;

    public static LocationDetailsDTO from(Location locationEntity, Predicate<Location> hasChildren) {
        return from(locationEntity, hasChildren, l -> null, null);
    }

    public static LocationDetailsDTO from(Location locationEntity, Predicate<Location> hasChildren, AtomicInteger naturalOrder) {
        return from(locationEntity, hasChildren, l -> null, naturalOrder);
    }

    public static LocationDetailsDTO from(
           Location locationEntity,
           Predicate<Location> hasChildren,
           Function<Location, List<LocationReferenceDTO>> locationPath,
           AtomicInteger naturalOrder
   ) {
       if (null == locationEntity) {
           return null;
       }

       Integer order = locationEntity.getOrder();
       if (naturalOrder != null) {
           if (order == null || order.equals(0)) {
               order = naturalOrder.getAndIncrement();
           } else {
               naturalOrder.set(order + 1);
           }
       }
       
       // Basic info
       return new LocationDetailsDTO(
               locationEntity.getId(),
               locationEntity.getParentId(),
               locationEntity.getName(),
               locationEntity.getDescription(),
               FileDTO.fromFile(locationEntity.getDefaultImage()),
               FileDTO.fromFile(locationEntity.getDefaultPlan()),
               locationEntity.getSurface(),
               locationEntity.getVolume(),
               locationEntity.getLength(),
               locationEntity.getWidth(),
               locationEntity.getHeight(),
               UnitDTO.from(locationEntity.getSurfaceUnit()),
               UnitDTO.from(locationEntity.getVolumeUnit()),
               UnitDTO.from(locationEntity.getUnit()),

               order,

               hasChildren.test(locationEntity),

               locationPath.apply(locationEntity)
       );
   }
}
