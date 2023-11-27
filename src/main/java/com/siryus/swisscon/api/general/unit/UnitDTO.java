/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.general.unit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 *
 * @author hng
 */
@Builder( toBuilder = true )
public class UnitDTO {
    private final Integer id;
    private final String name;
    private final String symbol;
    private final Type type;

    @JsonCreator
    public UnitDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("name") String name,
            @JsonProperty("symbol") String symbol,
            @JsonProperty("type") Type type
    ) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.type = type;
    }

    public static UnitDTO from(Unit u) {
        if (u == null) {
            return null;
        }
        return new UnitDTO(
                u.getId(), u.getName(), u.getSymbol(), type(u)
        );
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public Type getType() {
        return type;
    }

    public boolean equals(UnitDTO unit) {
        return this.id.equals(unit.getId());
    }

    private static Type type(Unit u) {
        if (u.getLength()) return Type.LENGTH;
        if (u.getSurface()) return Type.SURFACE;
        if (u.getDensity()) return  Type.DENSITY;
        if (u.getFixedPrice()) return Type.FIXED_PRICE;
        if (u.getForce()) return Type.FORCE;
        if (u.getIlluminance()) return Type.ILLUMINANCE;
        if (u.getPower()) return Type.POWER;
        if (u.getQuantity()) return Type.QUANTITY;
        if (u.getTime()) return Type.TIME;
        if (u.getVolume()) return Type.VOLUME;
        if (u.getWeight()) return Type.WEIGHT;

        return Type.OTHER;
    }

    public enum Type {
        DENSITY,
        FIXED_PRICE,
        FORCE,
        ILLUMINANCE,
        LENGTH,
        POWER,
        QUANTITY,
        SURFACE,
        TIME,
        VOLUME,
        WEIGHT,

        OTHER
    }
}
