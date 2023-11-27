/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.filter;

import com.naturalprogrammer.spring.lemon.commons.security.BlueTokenService;
import com.naturalprogrammer.spring.lemon.commons.security.LemonPrincipal;
import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.naturalprogrammer.spring.lemon.commons.util.LecUtils;
import com.naturalprogrammer.spring.lemon.commonsweb.security.LemonCommonsWebTokenAuthenticationFilter;
import com.naturalprogrammer.spring.lemon.security.LemonJpaTokenAuthenticationFilter;
import com.naturalprogrammer.spring.lemon.security.LemonUserDetailsService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.siryus.swisscon.api.auth.LemonService;
import com.siryus.swisscon.api.util.CookieService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.siryus.swisscon.api.auth.LemonService.USER_ID_CLAIM;

/**
 *
 * @author hng
 */
public class LemonCommonsCookieAuthFilter extends LemonJpaTokenAuthenticationFilter {
    private static final Log log = LogFactory.getLog(LemonCommonsWebTokenAuthenticationFilter.class);

    private CookieService cookieService;
    private LemonService lemonService;
    private BlueTokenService blueTokenService;
    
    public LemonCommonsCookieAuthFilter(LemonService service, BlueTokenService blueTokenService, LemonUserDetailsService<?, ?> userDetailsService, CookieService cookieService) {        
        super(blueTokenService, userDetailsService);        

        this.cookieService = cookieService;
        this.lemonService = service;
        this.blueTokenService = blueTokenService;
    }

    /*
        If a cookie is present then check it and resolve to a user, otherwise this filter does nothing.
    */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
        log.debug("Inside LemonCommonsCookieAuthFilter ...");

        Cookie[] cookies = request.getCookies();

        if (cookies == null || cookies.length == 0) {
            log.debug("Non authenticated request.");
            filterChain.doFilter(request, response);            
            return;
        }

        // Find first cookie which has a token in it
        Cookie authCookie = null; 
        for (Cookie cookie : cookies) {            
            if (cookie.getName().equalsIgnoreCase(cookieService.getCookieName())) {
                authCookie = cookie; 
                break;
            }
        }

        if (authCookie == null) {      
            log.debug("Non authenticated request.");
            filterChain.doFilter(request, response);            
            return;
        }

        // Extract token
        String token = authCookie.getValue();
        if (token == null || token.isEmpty())
        {
            log.error("Token authentication failed - missing token inside cookie");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed: missing token inside cookie");
            response.addCookie(authCookie);
            return;              
        }

        try {
            Authentication auth = createAuthToken(token);
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.debug("Token authentication successful");
        } catch (Exception e) {
            // Cookie token is expired, reject request and force browser to remove it
            Cookie newCookie = lemonService.createAuthCookie(token, 0L);
            newCookie.setMaxAge(0);
            response.addCookie(newCookie);
            return;
        }

        filterChain.doFilter(request, response);            
    }

    protected Authentication createAuthToken(String token) {
        JWTClaimsSet claims = this.blueTokenService.parseToken(token, "auth");
        if (claims.getClaim(USER_ID_CLAIM) == null) {
            throw new BadCredentialsException("Old format JWT");
        }

        UserDto userDto = LecUtils.getUserDto(claims);
        if (userDto == null) {
            userDto = this.fetchUserDto(claims);
        }

        LemonPrincipal principal = new LemonPrincipal(userDto);
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }
}
