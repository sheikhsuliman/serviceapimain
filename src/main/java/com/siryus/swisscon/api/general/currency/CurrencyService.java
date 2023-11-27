package com.siryus.swisscon.api.general.currency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("currencyService")
public class CurrencyService  {

    private final CurrencyRepository currencyRepository;

    @Autowired
    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    public List<Currency> findAll() {
        return currencyRepository.findAll();
    }

    public Currency findById(String id) {
        return currencyRepository.findById(id).orElse(null);
    }

}
