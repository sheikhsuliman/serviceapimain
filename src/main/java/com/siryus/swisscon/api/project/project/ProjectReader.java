package com.siryus.swisscon.api.project.project;

import com.siryus.swisscon.api.project.ProjectException;
import org.springframework.stereotype.Service;

@Service
public class ProjectReader {
    private final ProjectRepository repository;

    public ProjectReader(ProjectRepository repository) {
        this.repository = repository;
    }

    public void validateProjectId(Integer id) {
        if(!repository.existsById(id)) {
            throw ProjectException.projectNotFound(id);
        }
    }
}
