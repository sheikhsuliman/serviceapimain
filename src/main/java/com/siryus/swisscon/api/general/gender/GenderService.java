package com.siryus.swisscon.api.general.gender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("genderService")
public class GenderService {

    private final GenderRepository genderRepository;

    @Autowired
    public GenderService(GenderRepository genderRepository) {
        this.genderRepository = genderRepository;
    }

    public Gender findById(Integer id) {
        return genderRepository.findById(id).orElse(null);
    }

    public List<Gender> findAll() {
        return genderRepository.findAll();
    }

}
