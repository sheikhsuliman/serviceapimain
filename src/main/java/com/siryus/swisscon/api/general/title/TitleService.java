package com.siryus.swisscon.api.general.title;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TitleService {

    private final TitleRepository repository;

    @Autowired
    public TitleService(TitleRepository repository) {
        this.repository = repository;
    }

    public TitleEntity findById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public List<TitleEntity> findAll() {
        return repository.findAll();
    }

}
