package com.siryus.swisscon.api.base;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleEntityDTO {
    protected Integer id;
    protected String name;
    
    public boolean equals(SimpleEntityDTO value) {
        return id.equals(value.getId()) && name.equals(value.getName());
    }
}
