package com.siryus.swisscon.api.tasks;

import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.FailFastExtension;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.file.file.FileRepository;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import com.siryus.swisscon.api.tasks.dto.SubTaskDTO;
import com.siryus.swisscon.api.tasks.dto.TaskStatus;
import com.siryus.swisscon.api.taskworklog.dto.TaskStatusDTO;
import com.siryus.swisscon.security.SecurityException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(FailFastExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskEnd2EndUnhappyPathIT extends AbstractMvcTestBase {
    private static final String OTHER_OWNER_COMPANY = "Build Beautiful Buildings";
    private static final String OTHER_CONTRACTOR_COMPANY = "Excavation By Wheelbarrow";

    private static final String STOP_DOING_THINGS = "Stop doing things";
    private static final String TOP_LOCATION = "TOP";
    private static final String MAIN_TASK = "Main Task";
    private static final String SUB_TASK_1 = "Sub Task 1";
    private static final String MAIN_TASK_1 = "Main Task 1";
    private static final String MAIN_TASK_2 = "Main Task 2";
    private static final String SUB_TASK_2 = "Sub Task 2";
    private static final String DO_THINGS = "Do things";
    private static final String DO_OTHER_THINGS = "Do other things";

    private static final Integer INVALID_ID = 666;

    private final FileRepository fileRepository;

    private TestHelper.ExtendedTestProject testProject1 = null;
    private TestHelper.ExtendedTestProject testProject2 = null;

    @Autowired
    public TaskEnd2EndUnhappyPathIT(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Test
    @Order(0)
    void setupUnHappyPath() {
        testProject1 = testHelper.createExtendedProject();

        testHelper.addLocationToExtendedProject(testProject1, TOP_LOCATION);

        TeamUserDTO[] projectTeam1 = testHelper.getProjectTeam(testProject1.ownerCompany.asAdmin, testProject1.projectId, testProject1.contractorCompany.companyId);

        assertNotNull(projectTeam1);
        assertEquals(3, projectTeam1.length);

        MainTaskDTO mainTaskDTO = testHelper.addContractualTask(testProject1.ownerCompany.asAdmin, TestBuilder.testCreateMainTaskRequest(
                testProject1.locationId(TOP_LOCATION),
                MAIN_TASK,
                testProject1.contractorCompany.companyId
        ));
        testProject1.addMainTask(MAIN_TASK, mainTaskDTO.getId());

        SubTaskDTO subTaskDTO1 = testHelper.addSubTask(testProject1.contractorCompany.asOwner, TestBuilder.testCreateSubTaskRequest(
                testProject1.mainTaskId(MAIN_TASK),
                SUB_TASK_1
        ));
        testProject1.addSubTask(SUB_TASK_1, subTaskDTO1.getId());

        mainTaskDTO = testHelper.addContractualTask(testProject1.ownerCompany.asAdmin, TestBuilder.testCreateMainTaskRequest(
                testProject1.locationId(TOP_LOCATION),
                MAIN_TASK_1,
                testProject1.contractorCompany.companyId
        ));
        testProject1.addMainTask(MAIN_TASK_1, mainTaskDTO.getId());


        testProject2 = testHelper.createExtendedProject(OTHER_OWNER_COMPANY, OTHER_CONTRACTOR_COMPANY);

        testHelper.addLocationToExtendedProject(testProject2, TOP_LOCATION);

        testHelper.getProjectTeam(testProject2.ownerCompany.asAdmin, testProject2.projectId, testProject2.contractorCompany.companyId);
    }

    @Test
    @Order(1)
    void Given_contractorCompanyNotPartOfProject_When_addMainTask_Then_CONFLICT() {
        testHelper.addContractualTask(
            testProject1.ownerCompany.asAdmin,
            TestBuilder.testCreateMainTaskRequest(
                testProject1.locationId(TOP_LOCATION),
                MAIN_TASK,
                testProject2.contractorCompany.companyId
            ),
            r -> { r.assertThat().statusCode(HttpStatus.CONFLICT.value()); return null; }
        );
    }

    @Test
    @Order(2)
    void Given_invalidLocation_When_addMainTask_Then_FORBIDDEN(){
        testHelper.addContractualTask(
                testProject1.ownerCompany.asAdmin,
                TestBuilder.testCreateMainTaskRequest(
                        testProject2.locationId(TOP_LOCATION),
                        MAIN_TASK,
                        testProject1.contractorCompany.companyId
                ),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );
    }

    @Test
    @Order(3)
    void Given_workerIsNotPartOfProject_When_addUserToSubTask_Then_FORBIDDEN() {
        testHelper.addUserToSubTask(
            testProject1.contractorCompany.asOwner, testProject1.subTaskId(SUB_TASK_1), testProject2.contractorCompany.workerId,
            r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); }
        );
    }

    @Test
    @Order(4)
    void Given_workerIsNotAssignedToSubTask_When_startTimer_Then_FORBIDDEN() {
        testHelper.startTimer(
            testProject1.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject1.subTaskId(SUB_TASK_1), "Started"),
            r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null;}
        );
    }

    @Test
    @Order(5)
    void Given_workerHaveNotStartAnyTimers_When_stopTimer_Then_CONFLICT() {
        testHelper.addUserToSubTask(testProject1.contractorCompany.asOwner, testProject1.subTaskId(SUB_TASK_1), testProject1.contractorCompany.workerId);

        testHelper.stopTimer(
                testProject1.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject1.subTaskId(SUB_TASK_1), "Stopped"),
                r -> { r.assertThat().statusCode(HttpStatus.CONFLICT.value()); return null;}
        );
    }

    @Test
    @Order(6)
    void Given_workerHaveStartedTimer_When_startTimerOnSameSubTask_Then_CONFLICT() {
        testHelper.startTimer(testProject1.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject1.subTaskId(SUB_TASK_1), "Started"));
        testHelper.startTimer(
                testProject1.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject1.subTaskId(SUB_TASK_1), "Started"),
                r -> { r.assertThat().statusCode(HttpStatus.CONFLICT.value()); return  null; }
        );
    }

    @Test
    @Order(7)
    void Given_invalidCheckListItemId_When_onCheckList_Then_FORBIDDEN() {
        testHelper.onCheckListItem(
                testProject1.contractorCompany.asWorker, INVALID_ID,
                r -> r.assertThat().statusCode(HttpStatus.FORBIDDEN.value())
        );
    }

    @Test
    @Order(8)
    void Given_taskInAssignedStatus_When_completeTask_Then_CONFLICT() {
        testHelper.completeTask(
            testProject1.contractorCompany.asOwner, TestBuilder.testTaskWorklogRequest(testProject1.mainTaskId(MAIN_TASK_1), null, DO_THINGS),
            r -> { r.assertThat().statusCode(HttpStatus.CONFLICT.value()); return null; }
        );
    }

    @Test
    @Order(9)
    void Given_taskInReviewStatus_When_startTimer_Then_CONFLICT() {
        testHelper.completeTask(testProject1.contractorCompany.asOwner, TestBuilder.testTaskWorklogRequest(testProject1.mainTaskId(MAIN_TASK), null, DO_THINGS));
        testHelper.startTimer(
            testProject1.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject1.subTaskId(SUB_TASK_1), "Started"),
            r -> { r.assertThat().statusCode(HttpStatus.CONFLICT.value()); return null;}
        );
    }

    @Test
    @Order(10)
    void Given_taskInCompleteStatus_When_startTimer_Then_CONFLICT() {
        testHelper.approveTask(testProject1.ownerCompany.asAdmin, TestBuilder.testTaskWorklogRequest(testProject1.mainTaskId(MAIN_TASK), null, DO_OTHER_THINGS));
        testHelper.startTimer(
                testProject1.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject1.subTaskId(SUB_TASK_1), "Started"),
                r -> { r.assertThat().statusCode(HttpStatus.CONFLICT.value()); return null;}
        );
    }

    @Test
    @Order(11)
    void Give_taskInCompleteStatus_When_completeTask_Then_CONFLICT() {
        testHelper.completeTask(
            testProject1.contractorCompany.asOwner, TestBuilder.testTaskWorklogRequest(testProject1.mainTaskId(MAIN_TASK), null, DO_THINGS),
            r -> { r.assertThat().statusCode(HttpStatus.CONFLICT.value()); return  null;}
        );
    }

    @Test
    @Order(12)
    void Given_subTaskIsCompletedButMainTaskIsNot_When_startTimer_Then_success() {
        MainTaskDTO mainTaskDTO = testHelper.addContractualTask(testProject1.ownerCompany.asAdmin, TestBuilder.testCreateMainTaskRequest(
                testProject1.locationId(TOP_LOCATION),
                MAIN_TASK_2,
                testProject1.contractorCompany.companyId
        ));
        testProject1.addMainTask(MAIN_TASK_2, mainTaskDTO.getId());

        SubTaskDTO subTaskDTO = testHelper.addSubTask(testProject1.contractorCompany.asOwner, TestBuilder.testCreateSubTaskRequest(
                testProject1.mainTaskId(MAIN_TASK_2),
                SUB_TASK_2
        ));
        testProject1.addSubTask(SUB_TASK_2, subTaskDTO.getId());

        testHelper.addUserToSubTask(testProject1.contractorCompany.asOwner, testProject1.subTaskId(SUB_TASK_2), testProject1.contractorCompany.workerId);

        testHelper.startTimer(testProject1.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject1.subTaskId(SUB_TASK_2), "Start"));
        testHelper.stopTimer(testProject1.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject1.subTaskId(SUB_TASK_2), "Stop"));

        Integer attachmentId = testHelper.fileUploadTemporary(testProject1.contractorCompany.asWorker).getId();
        TaskStatusDTO statusDTO1 = testHelper
                .completeSubTask(testProject1.contractorCompany.asWorker,
                        TestBuilder
                                .testTaskWorklogRequest(null, testProject1.subTaskId(SUB_TASK_2), "Complete", dto->dto
                                        .toBuilder()
                                        .attachmentIDs(Collections.singletonList(attachmentId))
                                .build()));

        TestAssert.assertAttachment(fileRepository.findById(attachmentId).orElseThrow(), ReferenceType.MAIN_TASK, testProject1.mainTaskId(MAIN_TASK_2));

        assertNotNull(statusDTO1);
        assertEquals(TaskStatus.PAUSED, statusDTO1.getMainTaskStatus());
        assertEquals(TaskStatus.COMPLETED, statusDTO1.getSubTaskStatuses().get(1));

        TaskStatusDTO statusDTO2 = testHelper.startTimer(testProject1.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject1.subTaskId(SUB_TASK_2), "Start"));

        assertNotNull(statusDTO2);
        assertEquals(TaskStatus.IN_PROGRESS, statusDTO2.getMainTaskStatus());
        assertEquals(TaskStatus.IN_PROGRESS, statusDTO2.getSubTaskStatuses().get(1));
    }

    @Test
    @Order(13)
    void Given_assignedUserIsNotPartOfCompany_When_addMainTask_Then_CONFLICT() {
        testHelper.addContractualTask(
                testProject1.ownerCompany.asAdmin,
                TestBuilder.testCreateMainTaskRequest(
                        testProject1.locationId(TOP_LOCATION),
                        MAIN_TASK,
                        testProject1.contractorCompany.companyId,

                        r -> r.toBuilder().taskTeam(Collections.singletonList(testProject2.contractorCompany.anotherWorkerId)).build()
                ),
                r -> {
                    TestAssert.assertError(HttpStatus.FORBIDDEN,
                            SecurityException.USER_IS_NOT_PART_OF_CURRENT_USER_COMPANY.getErrorCode(), r);
                    return null;
                }
        );
    }
}
