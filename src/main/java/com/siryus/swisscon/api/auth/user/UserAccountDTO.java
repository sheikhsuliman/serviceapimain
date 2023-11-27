/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.auth.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

/**
 * This class is used to represent data input when changing User Contact Info
 * 
 * @author hng
*/
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class UserAccountDTO {
    String languageId;
}
