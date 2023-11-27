/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.init;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;

import static io.restassured.RestAssured.given;

import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * @author hng
 */
@Slf4j
public class InitIT extends AbstractMvcTestBase {
    
    @Test
    public void testGetDashboardAuthenticated() {
        InitDTO response = getResponse(loginSpec(), BASE_PATH + "/dashboard/init")
                        .as(InitDTO.class);

        assertNotNull(response, "Received empty response");
        assertNotNull(response.getUser(), "Missing user data in response");
        assertNotNull(response.getUser().getCompany(), "Missing user company data in response");
        assertNotNull(response.getUser().getSsn());
        assertNotNull(response.getUser().getId());
        assertNotNull(response.getUser().getUnverified());
        assertNotNull(response.getUser().getCompany().getWebUrl());
        assertNotNull(response.getUser().getCompany().getLegalTypeId());

        assertNotNull(response.getSettings(), "Missing settings data in response");
        assertTrue(response.getSettings().getCompanyTypes() != null && response.getSettings().getCompanyTypes().size() > 0, "Missing company types settings data in response");
        assertTrue(response.getSettings().getProjectTypes() != null && response.getSettings().getProjectTypes().size() > 0, "Missing project types settings data in response");
        assertTrue(response.getSettings().getGenders() != null && response.getSettings().getGenders().size() > 0, "Missing genders ");
        assertTrue(response.getSettings().getRoles() != null && response.getSettings().getRoles().size() > 0, "Missing roles");
        assertTrue(response.getSettings().getTrades() != null && response.getSettings().getTrades().size() > 0, "Missing trades");        
        assertTrue(response.getSettings().getCountries() != null && response.getSettings().getTrades().size() > 0, "Missing countries");
        assertTrue(response.getSettings().getLanguages() != null && response.getSettings().getLanguages().size() > 0, "Missing languages");
    }

    @Test
    public void testGetDashboardNotAuthenticated() {
        getResponse(defaultSpec(), BASE_PATH + "/dashboard/init", HttpStatus.UNAUTHORIZED);
    }
    
    @Test
    public void testInitAuthenticated() {
        InitDTO response = getResponse(loginSpec(), BASE_PATH + "/auth/init")
                .as(InitDTO.class);

        assertNotNull(response, "Received empty response");
        assertNotNull(response.getUser(), "Missing user data in response");
        assertNotNull(response.getUser().getCompany(), "Missing user company data in response");
        assertNotNull(response.getUser().getCompany().getWebUrl(), "Missing company web url");
        assertNotNull(response.getUser().getMobilePhone(), "User should have a mobile number");
        assertNotNull(response.getUser().getMobileCountryCode(), "User should have a mobile country code");
        assertNotNull(response.getUser().getLanguage(), "User should have a language");
        assertNotNull(response.getUser().getBirthDate(), "User should have a birthdate");
        assertNotNull(response.getUser().getSsn(), "User should have a ssn");
        assertNotNull(response.getUser().getId(), "User should have an id");
        assertNotNull(response.getUser().getUnverified());


        assertNotNull(response.getSettings(), "Missing settings data in response");
        assertTrue(response.getSettings().getCompanyTypes() != null && response.getSettings().getCompanyTypes().size() > 0, "Missing company types settings data in response");
        assertTrue(response.getSettings().getProjectTypes() != null && response.getSettings().getProjectTypes().size() > 0, "Missing project types settings data in response");
        assertTrue(response.getSettings().getGenders() != null && response.getSettings().getGenders().size() > 0, "Missing genders ");
        assertTrue(response.getSettings().getRoles() != null && response.getSettings().getRoles().size() > 0, "Missing roles");
        assertTrue(response.getSettings().getTrades() != null && response.getSettings().getTrades().size() > 0, "Missing trades");
        assertTrue(response.getSettings().getCountries() != null && response.getSettings().getTrades().size() > 0, "Missing countries");
        assertTrue(response.getSettings().getLanguages() != null && response.getSettings().getLanguages().size() > 0, "Missing languages");
        assertTrue(response.getSettings().getCurrencies() != null && response.getSettings().getCurrencies().size() > 0, "Missing currencies");

    }
    
    @Test
    public void testInitNotAuthenticated() {
        InitDTO response = getResponse(defaultSpec(), BASE_PATH + "/auth/init")
                .as(InitDTO.class);

        assertNotNull(response, "Received empty response");
        assertNull(response.getUser(), "User data should be null");

        assertNotNull(response.getSettings(), "Missing settings data in response");
        assertTrue(response.getSettings().getCompanyTypes() != null && response.getSettings().getCompanyTypes().size() > 0, "Missing company types settings data in response");
        assertTrue(response.getSettings().getProjectTypes() != null && response.getSettings().getProjectTypes().size() > 0, "Missing company types settings data in response");
        assertTrue(response.getSettings().getGenders() != null && response.getSettings().getGenders().size() > 0, "Missing genders ");
        assertTrue(response.getSettings().getRoles() != null && response.getSettings().getRoles().size() > 0, "Missing roles");
        assertTrue(response.getSettings().getTrades() != null && response.getSettings().getTrades().size() > 0, "Missing trades");
        assertTrue(response.getSettings().getCountries() != null && response.getSettings().getTrades().size() > 0, "Missing countries");
        assertTrue(response.getSettings().getLanguages() != null && response.getSettings().getLanguages().size() > 0, "Missing languages");
        assertTrue(response.getSettings().getCurrencies() != null && response.getSettings().getCurrencies().size() > 0, "Missing currencies");
    }
}
