package com.siryus.swisscon.api.company.numworkersofcompany;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("numWorkersOfCompanyService")
public class NumWorkersOfCompanyService {

    private final NumWorkersOfCompanyRepository numWorkersOfCompanyRepository;

    @Autowired
    public NumWorkersOfCompanyService(NumWorkersOfCompanyRepository numWorkersOfCompanyRepository) {
        this.numWorkersOfCompanyRepository = numWorkersOfCompanyRepository;
    }

    public NumWorkersOfCompany findById(Integer id) {
        return this.numWorkersOfCompanyRepository.findById(id).orElse(null);
    }

    public List<NumWorkersOfCompany> findAll() {
        return numWorkersOfCompanyRepository.findAll();
    }
}
