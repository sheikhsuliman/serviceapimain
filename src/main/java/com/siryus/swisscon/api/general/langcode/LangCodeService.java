package com.siryus.swisscon.api.general.langcode;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("langCodeService")
public class LangCodeService {

    private final LangCodeRepository langCodeRepository;

    @Autowired
    public LangCodeService(LangCodeRepository langCodeRepository) {
        this.langCodeRepository = langCodeRepository;
    }

    public List<LangCode> findAll() {
        return langCodeRepository.findAll();
    }

    public LangCode findById(String id) {
        return langCodeRepository.findById(id).orElse(null);
    }

}
