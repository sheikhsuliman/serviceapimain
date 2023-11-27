package com.siryus.swisscon.api.tasks.tasks;

import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.tasks.entity.SubTaskEntity;
import com.siryus.swisscon.api.tasks.entity.SubTaskUserEntity;
import com.siryus.swisscon.api.tasks.repos.SubTaskRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskUserRepository;
import com.siryus.swisscon.api.util.error.TestErrorResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;

public class RemoveTaskUserIT extends AbstractMvcTestBase {

    private static final String PATH = BASE_PATH + "/tasks/";

    private final SubTaskRepository subTaskRepository;
    private final SubTaskUserRepository subTaskUserRepository;
    private final UserRepository userRepository;

    private static User user1;
    private static User user2;
    private static SubTaskEntity testSubTask;
    private static SubTaskUserEntity testSubTaskUser;

    @Autowired
    public RemoveTaskUserIT(SubTaskRepository subTaskRepository, SubTaskUserRepository subTaskUserRepository, UserRepository userRepository) {
        this.subTaskRepository = subTaskRepository;
        this.subTaskUserRepository = subTaskUserRepository;
        this.userRepository = userRepository;
    }
    
    @Test
    public void testDeleteMainTaskUser() {
        requestPostWithPath(PATH + "task/" + 1 + "/remove-user?user=" + user1.getId());

        // check if the user is not part of the team anymore
        Optional<SubTaskUserEntity> subTaskUserOpt = subTaskUserRepository.findByUserAndSubTask(user1.getId(), 1);
        assertFalse(subTaskUserOpt.isPresent());
    }
    
    @Test
    public void testDeleteMainTaskUserWhichIsNotInTeam() {
        TestErrorResponse error = given()
                .spec(loginSpec())
                .post(PATH + "task/" + 1 + "/remove-user?user=" + user2.getId())
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.CONFLICT.value()))
                .extract().as(TestErrorResponse.class);

        assertNotNull(error.getReason());
        assertTrue(error.getReason().contains("is not part of team of sub task with"));
    }

    @Test
    public void testDeleteSubTaskUser() {
        requestPostWithPath(PATH + "sub-task/" + 1 + "/remove-user?user=" + user1.getId());

        // check if the user is not part of the team anymore
        Optional<SubTaskUserEntity> subTaskUserOpt = subTaskUserRepository.findByUserAndSubTask(user1.getId(), 1);
        assertFalse(subTaskUserOpt.isPresent());
    }

    @Test
    public void testDeleteSubTaskUserWhichIsNotInTeam() {
        TestErrorResponse error = given()
                .spec(loginSpec())
                .post(PATH + "sub-task/" + 1 + "/remove-user?user=" + user2.getId())
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.CONFLICT.value()))
                .extract().as(TestErrorResponse.class);

        assertNotNull(error.getReason());
        assertTrue(error.getReason().contains("is not part of team of sub task with"));
    }

    @BeforeEach()
    public void initTestData() {
        Optional<User> userOpt1 = userRepository.findById(1);
        Optional<User> userOpt2 = userRepository.findById(2);
        assert userOpt1.isPresent();
        assert userOpt2.isPresent();
        user1 = userOpt1.get();
        user2 = userOpt2.get();

        Optional<SubTaskEntity> subTaskOpt = subTaskRepository.findById(1);
        assert subTaskOpt.isPresent();
        testSubTask = subTaskOpt.get();

        testSubTaskUser = createSubTaskUserEntity(userOpt1.get(), testSubTask);
    }

    private SubTaskUserEntity createSubTaskUserEntity(User user, SubTaskEntity subTaskEntity) {
        SubTaskUserEntity tempSubTaskUserEntity = SubTaskUserEntity.builder()
                .subTask(subTaskEntity)
                .user(user)
                .createdBy(user.getId())
                .createdDate(LocalDateTime.now())
                .build();
        return subTaskUserRepository.save(tempSubTaskUserEntity);
    }

    private void requestPostWithPath(String path) {
        given()
                .spec(loginSpec())
                .post(path)
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.OK.value()));
    }

    @AfterEach()
    public void cleanTestData() {
        Optional.ofNullable(testSubTaskUser).ifPresent(t -> deleteIfExists(subTaskUserRepository, t.getId()));
        testSubTaskUser = null;
        testSubTask = null;
        user1 = null;
        user2 = null;
    }

}
