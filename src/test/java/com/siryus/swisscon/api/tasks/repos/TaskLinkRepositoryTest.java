package com.siryus.swisscon.api.tasks.repos;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.project.project.ProjectRepository;
import com.siryus.swisscon.api.tasks.entity.TaskLinkEntity;
import com.siryus.swisscon.api.tasks.entity.TaskLinkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskLinkRepositoryTest extends AbstractMvcTestBase {

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    MainTaskRepository mainTaskRepository;

    @Autowired
    TaskLinkRepository repository;

    @BeforeEach
    void doBeforeEach() {
        repository.deleteAll();
        mockApplicationUser(1);
    }

    @Test
    void Given_thereIsNoAnyLinks_When_findTaskLinks_Then_returnEmptyList() {
        assertTrue(repository.findTaskLinksInProject(1).isEmpty());
        assertTrue(repository.findTaskLinksWithSourceTask(1).isEmpty());
        assertTrue(repository.findTaskLinksWithDestinationTask(1).isEmpty());
    }

    @Test
    void Given_thereAreLinks_When_findTaskLinks_Then_returnAllLinks() {
        newLink(TaskLinkType.BLOCKS, 1, 1, 2);
        newLink(TaskLinkType.DUPLICATES, 1, 1, 2);

        assertEquals(2, repository.findTaskLinksInProject(1).size());
        assertEquals(2, repository.findTaskLinksWithSourceTask(1).size());
        assertEquals(2, repository.findTaskLinksWithDestinationTask(2).size());
    }

    @Transactional
    @Test
    void Given_twoLinks_When_disableTaskLink_Then_findTaskLinksReturnOne() {
        TaskLinkEntity entityToDelete = newLink(TaskLinkType.BLOCKS, 1, 1, 2);
        newLink(TaskLinkType.DUPLICATES, 1, 1, 2);

        repository.disableTaskLink(entityToDelete.getId());

        assertEquals(1, repository.findTaskLinksInProject(1).size());
        assertEquals(1, repository.findTaskLinksWithSourceTask(1).size());
        assertEquals(1, repository.findTaskLinksWithDestinationTask(2).size());
    }

    @Transactional
    @Test
    void Given_allLinksHaveTask_When_disableAllLinksWithTask_Then_returnNoLinks() {
        newLink(TaskLinkType.BLOCKS, 1, 1, 2);
        newLink(TaskLinkType.DUPLICATES, 1, 2, 1);

        repository.disableAllLinksWithTask(2);

        assertTrue(repository.findTaskLinksInProject(1).isEmpty());
        assertTrue(repository.findTaskLinksWithSourceTask(1).isEmpty());
        assertTrue(repository.findTaskLinksWithDestinationTask(1).isEmpty());
    }

    private TaskLinkEntity newLink(TaskLinkType type, Integer projectId, Integer srcTaskId, Integer dstTaskId) {
        return repository.save(TaskLinkEntity.builder()
                .linkType(type)
                .project(projectRepository.getOne(projectId))
                .sourceTask(mainTaskRepository.getOne(srcTaskId))
                .destinationTask(mainTaskRepository.getOne(dstTaskId))
                .build());
    }
}