package com.siryus.swisscon.api.general.country;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CountryServiceTest extends AbstractMvcTestBase {

    private final CountryService countryService;

    @Autowired
    public CountryServiceTest(CountryService countryService) {
        this.countryService = countryService;
    }

    @Test
    public void testGetCountries() {
        List<Country> countries = countryService.getCountries();

        assertTrue(countries.stream().anyMatch(c->c.getCode().equals("DE")), "Countries list should contain DE");
        assertTrue(countries.stream().anyMatch(c->c.getCode().equals("AT")), "Countries list should contain AT");
        assertTrue(countries.stream().anyMatch(c->c.getCode().equals("CH")), "Countries list should contain CH");
        assertTrue(countries.stream().anyMatch(c->c.getCode().equals("ES")), "Countries list should contain ES");
        assertTrue(countries.stream().anyMatch(c->c.getCode().equals("LI")), "Countries list should contain LI");
        assertTrue(countries.stream().anyMatch(c->c.getCode().equals("CN")), "Countries list should contain CN");
    }
}
