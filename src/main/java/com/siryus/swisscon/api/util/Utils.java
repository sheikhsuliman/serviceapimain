/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.util;

import java.sql.Date;
import java.text.Format;
import java.text.SimpleDateFormat;

/**
 *
 * @author hng
 */
public class Utils {
    static final Format germanDateFormatter = new SimpleDateFormat("MM.dd.yy");

    public static String formatDateToGerman(Date date) {
        if (date == null) {
            return null;
        }
        
        return germanDateFormatter.format(date);
    }
    
    public static boolean isEmpty(String value) {
        return null == value || value.isEmpty();
    } 

    public static boolean stringEquals(String email, String retypeEmail) {
        return ( isEmpty(email) && isEmpty(retypeEmail) ) ||
                ( !isEmpty(email) && !isEmpty(retypeEmail) && email.equals(retypeEmail) );
    }
}
