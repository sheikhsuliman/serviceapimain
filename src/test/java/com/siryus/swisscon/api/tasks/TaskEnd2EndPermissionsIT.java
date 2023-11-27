package com.siryus.swisscon.api.tasks;

import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.FailFastExtension;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import com.siryus.swisscon.api.tasks.dto.SubTaskDTO;
import com.siryus.swisscon.api.tasks.dto.TaskChecklistItem;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(FailFastExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskEnd2EndPermissionsIT extends AbstractMvcTestBase {
    private static final String OTHER_CONTRACTOR_COMPANY = "Blow Me Down Demolisher";
    private static final String STOP_DOING_THINGS = "Stop doing things";
    private static final String TOP_LOCATION = "TOP";
    private static final String MAIN_TASK = "Main Task";
    private static final String SUB_TASK_1 = "Sub Task 1";
    private static final String SUB_TASK_2 = "Sub Task 2";
    private static final String DO_THINGS = "Do things";
    private static final String DO_OTHER_THINGS = "Do other things";

    private TestHelper.ExtendedTestProject testProject = null;
    private TestHelper.TestProjectContractorCompany otherContractor = null;

    @Test
    @Order(0)
    void setupPath() {
        testProject = testHelper.createExtendedProject();

        testHelper.addLocationToExtendedProject(testProject, TOP_LOCATION);

        TeamUserDTO[] projectTeam = testHelper.getProjectTeam(testProject.ownerCompany.asAdmin, testProject.projectId, testProject.contractorCompany.companyId);

        assertNotNull(projectTeam);
        assertEquals(3, projectTeam.length);

        MainTaskDTO mainTaskDTO = testHelper.addContractualTask(testProject.ownerCompany.asAdmin, TestBuilder.testCreateMainTaskRequest(
                testProject.locationId(TOP_LOCATION),
                MAIN_TASK,
                testProject.contractorCompany.companyId
        ));
        testProject.addMainTask(MAIN_TASK, mainTaskDTO.getId());

        SubTaskDTO subTaskDTO1 = testHelper.addSubTask(testProject.contractorCompany.asOwner, TestBuilder.testCreateSubTaskRequest(
                testProject.mainTaskId(MAIN_TASK),
                SUB_TASK_1
        ));
        testProject.addSubTask(SUB_TASK_1, subTaskDTO1.getId());

        testHelper.addUserToSubTask(testProject.contractorCompany.asOwner, testProject.subTaskId(SUB_TASK_1), testProject.contractorCompany.workerId);

        SubTaskDTO subTaskDTO2 = testHelper.addSubTask(testProject.contractorCompany.asOwner, TestBuilder.testCreateSubTaskRequest(
                testProject.mainTaskId(MAIN_TASK),
                SUB_TASK_2
        ));
        testProject.addSubTask(SUB_TASK_2, subTaskDTO2.getId());

        otherContractor = testHelper.createProjectContractorCompany(OTHER_CONTRACTOR_COMPANY);
    }

    @Test
    @Order(1)
    void Given_notProjectAdmin_When_addMainTask_Then_FORBIDDEN() {
        testHelper.addContractualTask(testProject.contractorCompany.asOwner, TestBuilder.testCreateMainTaskRequest(
                testProject.locationId(TOP_LOCATION),
                MAIN_TASK,
                testProject.contractorCompany.companyId
            ),
            r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );
        testHelper.addContractualTask(testProject.contractorCompany.asWorker, TestBuilder.testCreateMainTaskRequest(
                testProject.locationId(TOP_LOCATION),
                MAIN_TASK,
                testProject.contractorCompany.companyId
                ),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );
        testHelper.addContractualTask(otherContractor.asOwner, TestBuilder.testCreateMainTaskRequest(
                testProject.locationId(TOP_LOCATION),
                MAIN_TASK,
                testProject.contractorCompany.companyId
                ),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );
    }

    @Test
    @Order(2)
    void Given_notProjectManager_When_addSubTask_Then_FORBIDDEN() {
        testHelper.addSubTask(testProject.contractorCompany.asWorker, TestBuilder.testCreateSubTaskRequest(
                testProject.mainTaskId(MAIN_TASK),
                SUB_TASK_1
            ),
            r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );
        testHelper.addSubTask(otherContractor.asOwner, TestBuilder.testCreateSubTaskRequest(
                testProject.mainTaskId(MAIN_TASK),
                SUB_TASK_1
                ),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );
    }

    @Test
    @Order(3)
    void Given_notPartOfProject_When_listMainTasksXXX_Then_FORDIDDEN() {
        testHelper.listMainTasksForProject(
                otherContractor.asOwner, testProject.projectId, TestBuilder.testListTasksRequest(),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );

        testHelper.listMainTasksForLocation(
                otherContractor.asOwner, testProject.locationId(TOP_LOCATION),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );
    }

    @Test
    @Order(4)
    void Given_notProjectManager_When_assignWorkerToSubTask_Then_FORBIDDEN() {
        testHelper.addUserToSubTask(
                testProject.contractorCompany.asWorker, testProject.subTaskId(SUB_TASK_1), testProject.contractorCompany.workerId,
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); }
        );

        testHelper.addUserToSubTask(
                otherContractor.asOwner, testProject.subTaskId(SUB_TASK_1), testProject.contractorCompany.workerId,
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); }
        );
    }

    @Test
    @Order(5)
    void Given_notProjectManager_When_addChecklistItem_Then_FORBIDDEN() {
        testHelper.addMainTaskChecklistItem(
                testProject.contractorCompany.asWorker, testProject.mainTaskId(MAIN_TASK), TestBuilder.testAddTaskChecklistItemRequest(DO_THINGS),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );
        testHelper.addMainTaskChecklistItem(
                otherContractor.asOwner, testProject.mainTaskId(MAIN_TASK), TestBuilder.testAddTaskChecklistItemRequest(DO_THINGS),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );
    }

    @Test
    @Order(6)
    void Given_notAssignedToTask_When_startTimer_Then_FORBIDDEN() {
        testHelper.startTimer(
                testProject.ownerCompany.asAdmin, TestBuilder.testTaskWorklogRequest(null, testProject.subTaskId(SUB_TASK_1), "Started"),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );
        testHelper.startTimer(
                otherContractor.asOwner, TestBuilder.testTaskWorklogRequest(null, testProject.subTaskId(SUB_TASK_1), "Started"),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );
    }

    @Test
    @Order(7)
    void Given_notAssignedToTask_When_stopTimer_Then_FORBIDDEN() {
        testHelper.stopTimer(
                testProject.ownerCompany.asAdmin, TestBuilder.testTaskWorklogRequest(null, testProject.subTaskId(SUB_TASK_1), "Started"),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );
        testHelper.stopTimer(
                otherContractor.asOwner, TestBuilder.testTaskWorklogRequest(null, testProject.subTaskId(SUB_TASK_1), "Started"),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );
    }

    @Test
    @Order(8)
    void Given_notAssignedToTask_When_onCheckList_Then_FORBIDDEN() {
        testHelper.addMainTaskChecklistItem(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK), TestBuilder.testAddTaskChecklistItemRequest(DO_THINGS));
        testHelper.addMainTaskChecklistItem(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK), TestBuilder.testAddTaskChecklistItemRequest(DO_OTHER_THINGS));
        testHelper.addMainTaskChecklistItem(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK), TestBuilder.testAddTaskChecklistItemRequest(STOP_DOING_THINGS));

        TaskChecklistItem[] originalChecklistItems = testHelper.getMainTaskCheckList(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK));
        /*
            TODO: In current form, PROJECT_OWNER/PROJECT_ADMIN allowed to do it... but IMHO they should not
        testHelper.onCheckListItem(
                testProject.ownerCompany.asAdmin, originalChecklistItems[0].getId(),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); }
        );
         */
        testHelper.onCheckListItem(
                otherContractor.asOwner, originalChecklistItems[0].getId(),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); }
        );
    }

    @Test
    @Order(9)
    void Given_notPartOfProjectTeam_When_getSubTaskCommentsOrAddComment_Then_FORBIDDEN() {
        testHelper.addCommentToSubTask(testProject.ownerCompany.asAdmin, testProject.subTaskId(SUB_TASK_1), TestBuilder.testCreateCommentDTO(DO_THINGS));
        testHelper.addCommentToSubTask(testProject.contractorCompany.asOwner, testProject.subTaskId(SUB_TASK_1), TestBuilder.testCreateCommentDTO(DO_OTHER_THINGS));
        testHelper.addCommentToSubTask(testProject.contractorCompany.asWorker, testProject.subTaskId(SUB_TASK_1), TestBuilder.testCreateCommentDTO(STOP_DOING_THINGS));

        testHelper.getSubTaskComments(
                otherContractor.asOwner, testProject.subTaskId(SUB_TASK_1),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );

        testHelper.addCommentToSubTask(
                otherContractor.asOwner, testProject.subTaskId(SUB_TASK_1), TestBuilder.testCreateCommentDTO(DO_THINGS),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); }
        );
    }

    @Test
    @Order(10)
    void Given_notProjectManagerOrProjectWorker_When_completeTask_Then_FORBIDDEN() {
        testHelper.startTimer(testProject.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject.subTaskId(SUB_TASK_1), "Started"));
        testHelper.stopTimer(testProject.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject.subTaskId(SUB_TASK_1), "Stopped"));

        testHelper.completeTask(
                testProject.ownerCompany.asAdmin, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId(MAIN_TASK), null, DO_THINGS),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null;}
        );

        testHelper.completeTask(
                otherContractor.asOwner, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId(MAIN_TASK), null, DO_THINGS),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null;}
        );
    }

    @Test
    @Order(11)
    void Given_notProjectOwnerOrAdmin_When_reviewTask_Then_FORBIDDEN() {
        testHelper.completeTask(testProject.contractorCompany.asOwner, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId(MAIN_TASK), null, DO_THINGS));

        testHelper.approveTask(
                testProject.contractorCompany.asOwner, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId(MAIN_TASK), null, DO_OTHER_THINGS),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );
        testHelper.rejectTaskContractor(
                testProject.contractorCompany.asOwner, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId(MAIN_TASK), null, DO_OTHER_THINGS),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );
        testHelper.approveTask(
                otherContractor.asOwner, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId(MAIN_TASK), null, DO_OTHER_THINGS),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );
        testHelper.rejectTaskContractor(
                otherContractor.asOwner, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId(MAIN_TASK), null, DO_OTHER_THINGS),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );
    }
}
