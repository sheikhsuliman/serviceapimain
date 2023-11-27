/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.util;

import java.net.HttpCookie;
import javax.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author hng
 */
@Component
public class CookieService {
    @Value("${cookie.name}")
    private String cookieName;
    
    @Value("${cookie.domain}")
    private String cookieDomain;

    @Value("${cookie.maxAge}")
    private String cookieMaxAge;

    @Value("${cookie.secure}")
    private Boolean cookieIsSecure;
    
    public String getCookieDomain() {
        return this.cookieDomain;
    }
    
    public String getCookieName() { 
        return this.cookieName;
    }
    
    public Integer getCookieMaxAge(int defaultMillisIfMissing) {
        int maxAge = defaultMillisIfMissing / 1000;
        try {
            maxAge = Integer.parseInt(this.cookieMaxAge);
        } catch (Exception e) {
            
        }               
        
        return maxAge;
    }
    
    public Boolean getCookieIsSecure() {
        return this.cookieIsSecure;
    }
    
    public javax.servlet.http.Cookie parseCookie(String header) {       
        if (header == null || header.isEmpty()) {
            return null;
        }
        
        String expectedName = getCookieName();
        for(HttpCookie cookie : HttpCookie.parse(header)) {
            if (cookie.getName().equals(expectedName)) {
                Cookie c = new Cookie(cookie.getName(), cookie.getValue());
                c.setVersion(cookie.getVersion());
                c.setPath(cookie.getPath());
                c.setDomain(cookie.getDomain());
                c.setMaxAge((int)cookie.getMaxAge());
                c.setHttpOnly(cookie.isHttpOnly());
                c.setSecure(cookie.getSecure());
                
                return c;
            }
        }
        
        return null;
    }
}
