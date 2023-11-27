/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.general.currency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the information needed when loading Currency from certain pages
 * 
 * It is used in these calls:
 * - /dashboard/init
 * 
 * @author hng
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDTO {
    String id;
    String name;
    
    /**
     * Builds a CurrencyDTO object by extracting some fields from Currency
     */
    public static CurrencyDTO from(Currency currency) {
        if (currency != null) {
            return CurrencyDTO.builder()
                    .id(currency.getId())
                    .name(currency.getName())
                    .build();
        }
        return null;

    }    
}
