package com.siryus.swisscon.api.project.project;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.file.file.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArchiveProjectIT extends AbstractMvcTestBase  {
    private static final AtomicInteger round = new AtomicInteger(0);
    private static final String COMPANY_NAME = "Company ";
    private static final String PROJECT_2 = "Project 2.";
    private static final String PROJECT_3 = "Project 3.";
    private static final String PROJECT_4 = "Project 4.";

    private static TestHelper.TestProjectOwnerCompany projectOwnerCompany;

    private final ProjectRepository projectRepository;
    private final FileRepository fileRepository;

    private ProjectBoardDTO project1;
    private ProjectBoardDTO project2;
    private ProjectBoardDTO project3;

    @Autowired
    public ArchiveProjectIT(ProjectRepository projectRepository, FileRepository fileRepository) {
        this.projectRepository = projectRepository;
        this.fileRepository = fileRepository;
    }

    @BeforeEach
    public void initTest() {
        projectOwnerCompany = testHelper.createProjectOwnerCompany(COMPANY_NAME + round.incrementAndGet());
        project1 = testHelper.createProject(projectOwnerCompany.asOwner, TestBuilder.testNewProjectDTO(PROJECT_2 + round.incrementAndGet()));
        project2 = testHelper.createProject(projectOwnerCompany.asOwner, TestBuilder.testNewProjectDTO(PROJECT_3 + round.incrementAndGet()));
        project3 = testHelper.createProject(projectOwnerCompany.asOwner, TestBuilder.testNewProjectDTO(PROJECT_4 + round.incrementAndGet()));
    }

    @Test
    void Given_initialState_When_getProjects_Then_returnAllProjects() {
        var projects = testHelper.getProjects(projectOwnerCompany.asOwner);

        assertEquals(4, projects.size());
    }

    @Test
    void Given_initialState_When_getArchivedProjects_Then_returnNoProjects() {
        var projects = testHelper.getArchivedProjects(projectOwnerCompany.asOwner);

        assertEquals(0, projects.size());
    }

    @Test
    void Given_twoProjectDeleted_When_getProjects_Then_returnOnlyNonDeletedProjects() {
        testHelper.archiveProject(projectOwnerCompany.asOwner, project1.getId());
        testHelper.archiveProject(projectOwnerCompany.asOwner, project2.getId());

        var projects = testHelper.getProjects(projectOwnerCompany.asOwner);

        assertEquals(2, projects.size());
        assertTrue(projects.stream().noneMatch( p -> p.getId().equals(project1.getId()) || p.getId().equals(project2.getId())));

        var archivedProjects = testHelper.getArchivedProjects(projectOwnerCompany.asOwner);
        assertEquals(2, archivedProjects.size());
        assertTrue(archivedProjects.stream().noneMatch( p -> ( !p.getId().equals(project1.getId()) && !p.getId().equals(project2.getId()))));
    }

    @Test
    void Given_twoProjectDeletedThenOneOfThemRestored_When_getProjects_Then_returnOnlyNonDeletedProjects() {
        testHelper.archiveProject(projectOwnerCompany.asOwner, project1.getId());
        testHelper.archiveProject(projectOwnerCompany.asOwner, project2.getId());

        testHelper.restoreProject(projectOwnerCompany.asOwner, project1.getId());

        var projects = testHelper.getProjects(projectOwnerCompany.asOwner);

        assertEquals(3, projects.size());
        assertTrue(projects.stream().noneMatch( p -> p.getId().equals(project2.getId())));

        var archivedProjects = testHelper.getArchivedProjects(projectOwnerCompany.asOwner);
        assertEquals(1, archivedProjects.size());
        assertTrue(archivedProjects.stream().noneMatch( p -> !p.getId().equals(project2.getId())));
    }
}
