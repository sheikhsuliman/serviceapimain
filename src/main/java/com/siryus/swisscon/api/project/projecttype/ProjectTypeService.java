package com.siryus.swisscon.api.project.projecttype;

import com.siryus.swisscon.api.project.ProjectException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("projectTypeService")
public class ProjectTypeService {

    private final ProjectTypeRepository projectTypeRepository;

    @Autowired
    public ProjectTypeService(ProjectTypeRepository projectTypeRepository) {
        this.projectTypeRepository = projectTypeRepository;
    }

    public List<ProjectType> findAll() {
        return projectTypeRepository.findAll();
    }

    public void validateProjectTypeId(Integer projectTypeId) {
        if(!projectTypeRepository.existsById(projectTypeId)) {
            throw ProjectException.projectTypeNotFound(projectTypeId);
        }
    }
}
