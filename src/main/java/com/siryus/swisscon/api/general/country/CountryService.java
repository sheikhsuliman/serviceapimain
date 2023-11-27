package com.siryus.swisscon.api.general.country;

import com.siryus.swisscon.api.general.GeneralException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("countryService")
public class CountryService {

    private final CountryRepository countryRepository;

    @Autowired
    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    public List<Country> getCountries() {
        return this.countryRepository.getCountryByType(CountryType.COUNTRY.toString());
    }

    public Country findById(Integer id) {
        return countryRepository.findById(id).orElse(null);
    }

    public void validateCountryId(Integer countryId) {
        if (countryId == null ) {
            throw GeneralException.countryIdCanNotBeNull();
        }
        if(!countryRepository.existsById(countryId)) {
            throw GeneralException.countryNotFound(countryId);
        }
    }
}
