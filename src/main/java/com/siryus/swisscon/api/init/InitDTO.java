/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.init;

import com.siryus.swisscon.api.auth.user.DashboardUserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * This class provides information on the currently logged in user and the global project settings.. 
 * 
 * The "user" fields in this class is optional: if a call is made from an authenticated page, 
 * it will contain the currently logged in user. Otherwise it will be null.
 * 
 * The "settings" field will always be present.
 * 
 * The model for 
 * @author hng
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InitDTO {
    private DashboardUserDTO user; // only present if a user is logged in
    
    private InitSettingsDTO settings;

    private Map<String, String> features;
}
