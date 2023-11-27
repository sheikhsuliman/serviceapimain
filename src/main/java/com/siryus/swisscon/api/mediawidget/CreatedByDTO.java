/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.mediawidget;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class CreatedByDTO {
    public Integer id;
    
    public String name;
    
    public String imageUrl;
    
    static CreatedByDTO from(MediaWidgetFileDTO.CreatedBy createdBy) {
        if (null == createdBy) {
            return null;
        }
        
        CreatedByDTO result = new CreatedByDTO();
        result.setId(createdBy.getId());
        result.setImageUrl(createdBy.getImageUrl());
        result.setName(createdBy.getName());
        
        return result;
    }    
}
