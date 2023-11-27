package com.siryus.swisscon.soa;

import com.siryus.swisscon.api.project.project.ProjectDTO;
import com.siryus.swisscon.api.project.project.ProjectService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/soa/")
public class ProjectSoaController {
    private final ProjectService projectService;

    @Autowired
    public ProjectSoaController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping(path="projects/{projectId}", produces="application/json")
    @ApiOperation(value = "Retrieve project details", notes = "This is only for non-sensitive information")
    public ProjectDTO getProject(@PathVariable Integer projectId) {
        return projectService.findProjectById(projectId);
    }

}
