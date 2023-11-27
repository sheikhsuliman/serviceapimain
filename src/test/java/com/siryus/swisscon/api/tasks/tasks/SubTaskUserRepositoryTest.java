package com.siryus.swisscon.api.tasks.tasks;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.tasks.entity.SubTaskUserEntity;
import com.siryus.swisscon.api.tasks.repos.SubTaskUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SubTaskUserRepositoryTest extends AbstractMvcTestBase {

    private final SubTaskUserRepository subTaskUserRepository;

    @Autowired
    public SubTaskUserRepositoryTest(SubTaskUserRepository subTaskUserRepository) {
        this.subTaskUserRepository = subTaskUserRepository;
    }

    @Test
    public void testFindByUserAndProject() {
        Optional<SubTaskUserEntity> subTaskUser1 = subTaskUserRepository.findById(1);
        assertTrue(subTaskUser1.isPresent());

        List<SubTaskUserEntity> subTaskUsers = subTaskUserRepository.findByUserAndProject(subTaskUser1.get().getUser().getId(), 1);

        boolean userFound = subTaskUsers.stream().anyMatch(stu -> stu.getId().equals(subTaskUser1.get().getId()));
        assertTrue(userFound);
    }
}
