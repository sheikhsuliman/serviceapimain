package com.siryus.swisscon.api.general.country;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryRepository extends JpaRepository<Country, Integer> {

    @Query("select country from Country country where country.type = ?1")
    List<Country> getCountryByType(String type);

}
