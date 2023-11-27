package com.siryus.swisscon.api.company.companylegaltype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("companyLegalTypeService")
public class CompanyLegalTypeService {

    private final CompanyLegalTypeRepository companyLegalTypeRepository;

    @Autowired
    public CompanyLegalTypeService(CompanyLegalTypeRepository companyLegalTypeRepository) {
        this.companyLegalTypeRepository = companyLegalTypeRepository;
    }

    public List<CompanyLegalType> findAll() {
        return companyLegalTypeRepository.findAll();
    }

    public CompanyLegalType findById(Integer id) {
        return companyLegalTypeRepository.findById(id).orElse(null);
    }

}
