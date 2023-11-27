package com.siryus.swisscon.api.tasks;

import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.FailFastExtension;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileRepository;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.tasks.dto.CommentDTO;
import com.siryus.swisscon.api.tasks.dto.IdResponse;
import com.siryus.swisscon.api.tasks.dto.ListTaskIdsRequest;
import com.siryus.swisscon.api.tasks.dto.ListTasksRequest;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import com.siryus.swisscon.api.tasks.dto.SubTaskDTO;
import com.siryus.swisscon.api.tasks.dto.TaskChecklistItem;
import com.siryus.swisscon.api.tasks.dto.TaskStatus;
import com.siryus.swisscon.api.taskworklog.dto.RejectTaskRequest;
import com.siryus.swisscon.api.taskworklog.dto.TaskStatusDTO;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.siryus.swisscon.api.base.TestHelper.COMPANY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FailFastExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskEnd2EndHappyPathIT extends AbstractMvcTestBase {
    private static final String STOP_DOING_THINGS = "Stop doing things";
    private static final String TOP_LOCATION = "TOP";
    private static final String MAIN_TASK = "Main Task";
    private static final String TASK_TO_REJECT = "Task to reject";
    private static final String DIRECTORIAL_TASK_WITH_COMPANY = "Directorial task with company";
    private static final String SUB_TASK_1 = "Sub Task 1";
    private static final String DO_THINGS = "Do things";
    private static final String DO_THINGS_GOOD = "Do things good";
    private static final String DO_OTHER_THINGS = "Do other things";

    private Integer invitedUser1Id;
    private Integer invitedUser2Id;

    private TestHelper.ExtendedTestProject testProject = null;

    private final FileRepository fileRepository;

    @Autowired
    public TaskEnd2EndHappyPathIT(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    // Happy Path

    @Test
    @Order(0)
    void setupHappyPath() {
        testProject = testHelper.createExtendedProject();


        testHelper.nameTopLocationOfExtendedProject(testProject, TOP_LOCATION);

        TeamUserDTO[] projectTeam = testHelper.getProjectTeam(testProject.ownerCompany.asAdmin, testProject.projectId, testProject.contractorCompany.companyId);

        assertNotNull(projectTeam);
        assertEquals(3, projectTeam.length);
    }

    @Test
    @Order(1)
    void Given_projectAdmin_When_addMainTask_Then_success() {
        MainTaskDTO mainTaskDTO = testHelper.addContractualTask(testProject.ownerCompany.asAdmin, TestBuilder.testCreateMainTaskRequest(
                testProject.locationId(TOP_LOCATION),
                MAIN_TASK,
                testProject.contractorCompany.companyId
        ));

        assertNotNull(mainTaskDTO);
        assertEquals(MAIN_TASK, mainTaskDTO.getTitle());
        assertEquals(testProject.contractorCompany.companyId, mainTaskDTO.getCompanyId());
        assertEquals(1, mainTaskDTO.getLocationPath().size());
        assertEquals(TaskStatus.ASSIGNED, mainTaskDTO.getStatus());

        assertNotNull(mainTaskDTO.getDefaultSubTask());
        assertEquals(mainTaskDTO.getId(), mainTaskDTO.getDefaultSubTask().getMainTaskId());

        testProject.addMainTask(MAIN_TASK, mainTaskDTO.getId());

        testAssert.assertContainsNewEmptyDefaultMediaFolders(
                testHelper.listFiles(testProject.ownerCompany.asAdmin,
                        ReferenceType.MAIN_TASK,
                        mainTaskDTO.getId()),
                ReferenceType.MAIN_TASK,
                mainTaskDTO.getId());
    }

    @Test
    @Order(2)
    void Given_projectManager_When_addSubTask_Then_success() {
        Integer invitedUserId = testHelper.inviteUserAndResetPassword(
                testProject.contractorCompany.asOwner,
                TestBuilder.testTeamUserAddDTO(COMPANY_NAME, "INVITED_FIRST", "INVITED_LAST")
        ).getId();

        SubTaskDTO subTaskDTO1 = testHelper.addSubTask(testProject.contractorCompany.asOwner, TestBuilder.testCreateSubTaskRequest(
                testProject.mainTaskId(MAIN_TASK),
                SUB_TASK_1, r-> r.toBuilder().taskTeam(Collections.singletonList(invitedUserId)).build()
        ));

        assertNotNull(subTaskDTO1);
        assertEquals(SUB_TASK_1, subTaskDTO1.getTitle());
        assertEquals(testProject.mainTaskId(MAIN_TASK), subTaskDTO1.getMainTaskId());
        assertEquals(1, subTaskDTO1.getLocationPath().size());
        assertEquals(TaskStatus.ASSIGNED, subTaskDTO1.getStatus());

        testProject.addSubTask(SUB_TASK_1, subTaskDTO1.getId());

        testAssert.assertContainsNewEmptyDefaultMediaFolders(
                testHelper.listFiles(testProject.ownerCompany.asAdmin,
                        ReferenceType.SUB_TASK,
                        subTaskDTO1.getId()),
                ReferenceType.SUB_TASK,
                subTaskDTO1.getId());

        List<TeamUserDTO> subTaskTeam = testHelper.getSubTaskTeam(testProject.contractorCompany.asOwner, subTaskDTO1.getId());
        TestAssert.assertTeamContainsUser(subTaskTeam, invitedUserId);
    }

    @Test
    @Order(3)
    void Given_projectManager_When_addUserToSubTask_Then_success() {
        testHelper.addUserToSubTask(testProject.contractorCompany.asOwner, testProject.subTaskId(SUB_TASK_1), testProject.contractorCompany.workerId);

        TeamUserDTO[] subTaskTeam = testHelper.getLocationTeam(testProject.contractorCompany.asOwner, testProject.locationId(TOP_LOCATION));

        assertNotNull(subTaskTeam);
        Arrays.sort(subTaskTeam, Comparator.comparingInt(TeamUserDTO::getId));
        assertEquals(3, subTaskTeam.length);
        assertEquals(testProject.contractorCompany.companyId, subTaskTeam[0].getCompanyId());
        assertEquals(testProject.contractorCompany.ownerId, subTaskTeam[0].getId());
        assertEquals(testProject.contractorCompany.workerId, subTaskTeam[1].getId());
    }

    @Test
    @Order(4)
    void Given_projectManager_When_createChecklist_Then_success() {
        IdResponse response = testHelper.addMainTaskChecklistItem(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK), TestBuilder.testAddTaskChecklistItemRequest(DO_THINGS));
        assertNotNull(response);
        testHelper.addMainTaskChecklistItem(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK), TestBuilder.testAddTaskChecklistItemRequest(DO_OTHER_THINGS));
        testHelper.addMainTaskChecklistItem(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK), TestBuilder.testAddTaskChecklistItemRequest(STOP_DOING_THINGS));

        TaskChecklistItem[] checklistItems = testHelper.getMainTaskCheckList(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK));
        assertNotNull(checklistItems);
        assertEquals(3, checklistItems.length);
        assertEquals(DO_THINGS, checklistItems[0].getTitle());
        assertEquals(DO_OTHER_THINGS, checklistItems[1].getTitle());
        assertEquals(STOP_DOING_THINGS, checklistItems[2].getTitle());

        checklistItems = testHelper.getMainTaskCheckList(testProject.contractorCompany.asWorker, testProject.mainTaskId(MAIN_TASK));
        assertNotNull(checklistItems);
        assertEquals(3, checklistItems.length);
    }

    @Test
    @Order(5)
    void Given_assignedToSubTask_When_startTimer_Then_success() {
        TaskStatusDTO taskStatusDTO = testHelper.startTimer(testProject.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject.subTaskId(SUB_TASK_1), "Started"));
        assertNotNull(taskStatusDTO);
        assertEquals(TaskStatus.IN_PROGRESS, taskStatusDTO.getMainTaskStatus());
        assertEquals(2, taskStatusDTO.getSubTaskStatuses().size());
        assertEquals(TaskStatus.ASSIGNED, taskStatusDTO.getSubTaskStatuses().get(0)); // default sub task
        assertEquals(TaskStatus.IN_PROGRESS, taskStatusDTO.getSubTaskStatuses().get(1));
    }

    @Test
    @Order(6)
    void Given_assignedToSubTask_When_stopTimer_Then_success() {
        TaskStatusDTO taskStatusDTO = testHelper.stopTimer(testProject.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject.subTaskId(SUB_TASK_1), "Stopped"));

        assertNotNull(taskStatusDTO);
        assertEquals(TaskStatus.PAUSED, taskStatusDTO.getMainTaskStatus());
        assertEquals(2, taskStatusDTO.getSubTaskStatuses().size());
        assertEquals(TaskStatus.ASSIGNED, taskStatusDTO.getSubTaskStatuses().get(0)); // default sub task
        assertEquals(TaskStatus.PAUSED, taskStatusDTO.getSubTaskStatuses().get(1));
    }

    @Test
    @Order(7)
    void Given_assignedToSubTask_When_onCheckList_Then_success() {
        TaskChecklistItem[] originalChecklistItems = testHelper.getMainTaskCheckList(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK));

        assertFalse(originalChecklistItems[0].isChecked());
        assertFalse(originalChecklistItems[1].isChecked());
        assertFalse(originalChecklistItems[2].isChecked());

        testHelper.onCheckListItem(testProject.contractorCompany.asWorker, originalChecklistItems[0].getId());

        TaskChecklistItem[] checklistItems = testHelper.getMainTaskCheckList(testProject.contractorCompany.asWorker, testProject.mainTaskId(MAIN_TASK));

        assertTrue(checklistItems[0].isChecked());
        assertFalse(originalChecklistItems[1].isChecked());
        assertFalse(originalChecklistItems[2].isChecked());

        testHelper.onCheckListItem(testProject.contractorCompany.asOwner, originalChecklistItems[2].getId());

        checklistItems = testHelper.getMainTaskCheckList(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK));

        assertTrue(checklistItems[0].isChecked());
        assertFalse(checklistItems[1].isChecked());
        assertTrue(checklistItems[2].isChecked());
    }

    @Test
    @Order(8)
    void Given_assignedToProject_When_addComment_Then_success() {
        File attachment = testHelper.fileUploadTemporary(testProject.ownerCompany.asAdmin);

        testHelper.addCommentToSubTask(testProject.ownerCompany.asAdmin,
                testProject.subTaskId(SUB_TASK_1),
                TestBuilder.testCreateCommentDTO(DO_THINGS, dto->{
                    dto.setAttachment(attachment.getId());
                    return dto;
                }));
        testHelper.addCommentToSubTask(testProject.contractorCompany.asOwner, testProject.subTaskId(SUB_TASK_1), TestBuilder.testCreateCommentDTO(DO_OTHER_THINGS));
        testHelper.addCommentToSubTask(testProject.contractorCompany.asWorker, testProject.subTaskId(SUB_TASK_1), TestBuilder.testCreateCommentDTO(STOP_DOING_THINGS));

        CommentDTO[] comments = testHelper.getSubTaskComments(testProject.contractorCompany.asWorker, testProject.subTaskId(SUB_TASK_1));

        assertNotNull(comments);
        assertEquals(3, comments.length);
        assertEquals(testProject.ownerCompany.adminId, comments[0].getUser().getId());
        assertEquals(DO_THINGS, comments[0].getComment());
        TestAssert.assertAttachment(fileRepository.findById(attachment.getId()).orElseThrow(), ReferenceType.MAIN_TASK, testProject.mainTaskId(MAIN_TASK));

        assertEquals(testProject.contractorCompany.ownerId, comments[1].getUser().getId());
        assertEquals(DO_OTHER_THINGS, comments[1].getComment());
        assertEquals(testProject.contractorCompany.workerId, comments[2].getUser().getId());
        assertEquals(STOP_DOING_THINGS, comments[2].getComment());

        testHelper.updateSubTaskComment(testProject.contractorCompany.asOwner, comments[0].getId(), TestBuilder.testCreateCommentDTO(DO_THINGS_GOOD));

        comments = testHelper.getSubTaskComments(testProject.contractorCompany.asWorker, testProject.subTaskId(SUB_TASK_1));
        assertNotNull(comments);
        assertEquals(3, comments.length);

        assertEquals(DO_THINGS_GOOD, comments[0].getComment());
        assertNull(comments[0].getAttachment());

        testHelper.archiveSubTaskComment(testProject.contractorCompany.asOwner, comments[0].getId());

        comments = testHelper.getSubTaskComments(testProject.contractorCompany.asWorker, testProject.subTaskId(SUB_TASK_1));

        assertNotNull(comments);
        assertEquals(2, comments.length);
    }

    @Test
    @Order(9)
    void Given_projectManager_When_completeTaskWithAttachment_Then_success() {
        Integer attachmentId = testHelper
                .fileUploadTemporary(testProject.contractorCompany.asOwner).getId();

        TaskStatusDTO taskStatusDTO = testHelper
                .completeTask(testProject.contractorCompany.asOwner,
                        TestBuilder.testTaskWorklogRequest(
                                testProject.mainTaskId(MAIN_TASK),
                                null,
                                DO_THINGS,
                                dto->dto.toBuilder()
                                        .attachmentIDs(Collections.singletonList(attachmentId))
                                        .build()
                        )
                );

        assertNotNull(taskStatusDTO);
        assertEquals(TaskStatus.IN_REVIEW, taskStatusDTO.getMainTaskStatus());
        TestAssert.assertAttachment(fileRepository.findById(attachmentId).orElseThrow(), ReferenceType.MAIN_TASK, testProject.mainTaskId(MAIN_TASK));
    }

    @Test
    @Order(10)
    void Given_projectAdmin_When_approveTask_Then_success() {
        TaskStatusDTO taskStatusDTO = testHelper.approveTask(testProject.ownerCompany.asAdmin, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId(MAIN_TASK), null, DO_OTHER_THINGS));

        assertNotNull(taskStatusDTO);
        assertEquals(TaskStatus.COMPLETED, taskStatusDTO.getMainTaskStatus());
    }

    @Test
    @Order(11)
    void Given_projectAdmin_When_rejectTaskWithAttachments_Then_success() {
        MainTaskDTO mainTaskDTO = testHelper.addContractualTask(testProject.ownerCompany.asAdmin,
                TestBuilder.testCreateMainTaskRequest(testProject.locationId(TOP_LOCATION),
                TASK_TO_REJECT,
                testProject.contractorCompany.companyId));
        testProject.addMainTask(TASK_TO_REJECT, mainTaskDTO.getId());

        testHelper.startTimer(testProject.contractorCompany.asOwner, TestBuilder.testTaskWorklogRequest(mainTaskDTO.getId(), null, "Work Is Hard"));
        testHelper.completeTask(testProject.contractorCompany.asOwner, TestBuilder.testTaskWorklogRequest(mainTaskDTO.getId(), null, "Work Is Done"));

        Integer attachmentId = testHelper.fileUploadTemporary(testProject.ownerCompany.asAdmin).getId();

        RejectTaskRequest rejectTaskRequest = TestBuilder.testRejectTaskRequest(ZonedDateTime.now().plusDays(14).truncatedTo(ChronoUnit.SECONDS),
                mainTaskDTO.getId(),
                null,
                "Good Job!",
                dto -> dto.toBuilder()
                        .attachmentIDs(Collections.singletonList(attachmentId))
                        .build()
        );

        TaskStatusDTO statusDTO = testHelper.rejectTask(testProject.ownerCompany.asAdmin, rejectTaskRequest);

        assertNotNull(statusDTO);
        assertEquals(TaskStatus.REJECTED, statusDTO.getMainTaskStatus());
        assertEquals(
                rejectTaskRequest.getDueDate().withZoneSameInstant(ZoneId.of(ZoneOffset.UTC.getId())),
                statusDTO.getDueDate().withZoneSameInstant(ZoneId.of(ZoneOffset.UTC.getId()))
        );
        TestAssert.assertAttachment(fileRepository.findById(attachmentId).orElseThrow(), ReferenceType.MAIN_TASK, mainTaskDTO.getId());
    }
    
    @Test
    @Order(12)
    void Given_projectAdmin_When_nonContractualTaskWithCompanyIsAdded_Then_success() {
        MainTaskDTO mainTaskDTO = testHelper.addDirectorialTask(testProject.ownerCompany.asAdmin,
                TestBuilder.testCreateDirectorialTaskRequest(testProject.locationId(TOP_LOCATION), 
                DIRECTORIAL_TASK_WITH_COMPANY, 
                testProject.contractorCompany.companyId,
                dto -> dto.toBuilder().attachmentIDs(
                        Collections.singletonList(testHelper.fileUploadTemporary(testProject.ownerCompany.asAdmin).getId())                        
                ).build()));
        
        assertEquals(TaskStatus.ASSIGNED.name(), mainTaskDTO.getStatus().name());
    }

    @Test
    @Order(13)
    void Given_projectAdmin_When_nonContractualTaskWithoutCompanyIsAdded_Then_success() {
        MainTaskDTO mainTaskDTO = testHelper.addDirectorialTask(testProject.ownerCompany.asAdmin,
                TestBuilder.testCreateDirectorialTaskRequest(testProject.locationId(TOP_LOCATION), 
                DIRECTORIAL_TASK_WITH_COMPANY, 
                null,
                dto -> dto.toBuilder().attachmentIDs(
                        Collections.singletonList(testHelper.fileUploadTemporary(testProject.ownerCompany.asAdmin).getId())                        
                ).build()));

        
        assertEquals(TaskStatus.OPEN.name(), mainTaskDTO.getStatus().name());    
    }

    @Test
    @Order(14)
    void Given_twoUsersNotPartOfProject_When_listAvailableUsersForCreation_Then_showCorrectAvailableList() {
        invitedUser1Id = testHelper.inviteUserAndResetPassword(
                testProject.ownerCompany.asOwner,
                TestBuilder.testTeamUserAddDTO(COMPANY_NAME, "INVITED_FIRST_1", "INVITED_LAST_1")
        ).getId();
        invitedUser2Id = testHelper.inviteUserAndResetPassword(
                testProject.ownerCompany.asOwner,
                TestBuilder.testTeamUserAddDTO(COMPANY_NAME, "INVITED_FIRST_2", "INVITED_LAST_2")
        ).getId();

        List<TeamUserDTO> teamUserDTOS = testHelper.listMainTaskAvailableUsersForCreation(testProject.ownerCompany.asOwner, testProject.projectId,
                testProject.ownerCompany.companyId);

        TestAssert.assertAvailableUsersContainUser(teamUserDTOS, testProject.ownerCompany.ownerId, true);
        TestAssert.assertAvailableUsersContainUser(teamUserDTOS, testProject.ownerCompany.adminId, true);
        TestAssert.assertAvailableUsersContainUser(teamUserDTOS, invitedUser1Id, false);
        TestAssert.assertAvailableUsersContainUser(teamUserDTOS, invitedUser2Id, false);
    }

    @Test
    @Order(15)
    void Given_twoUsersOutOfProject_When_createNewTaskWith2UsersOutOfProject_Then_usersWhereSuccessfullyAdded() {
        MainTaskDTO mainTaskDTO = testHelper.addDirectorialTask(testProject.ownerCompany.asOwner,
                TestBuilder.testCreateDirectorialTaskRequest(testProject.locationId(TOP_LOCATION),
                        "new task",
                        testProject.ownerCompany.companyId,
                        dto -> dto.toBuilder().taskTeam(Arrays.asList(invitedUser1Id, invitedUser2Id)).build()));

        TeamUserDTO[] projectTeam = testHelper.getProjectTeam(testProject.ownerCompany.asOwner, testProject.projectId, testProject.ownerCompany.companyId);
        TestAssert.assertTeamContainsUser(Arrays.asList(projectTeam), invitedUser1Id);
        TestAssert.assertTeamContainsUser(Arrays.asList(projectTeam), invitedUser2Id);

        List<TeamUserDTO> taskTeam = testHelper.getMainTaskTeam(testProject.ownerCompany.asOwner, mainTaskDTO.getId());
        TestAssert.assertTeamContainsUser(taskTeam, invitedUser1Id);
        TestAssert.assertTeamContainsUser(taskTeam, invitedUser2Id);

        List<TeamUserDTO> teamUserDTOS = testHelper.listMainTaskAvailableUsersForCreation(testProject.ownerCompany.asOwner, testProject.projectId,
                testProject.ownerCompany.companyId);
        TestAssert.assertAvailableUsersContainUser(teamUserDTOS, invitedUser1Id, true);
        TestAssert.assertAvailableUsersContainUser(teamUserDTOS, invitedUser2Id, true);
    }

    @Test
    @Order(16)
    void Given_newCreatedTask_When_ListMainTasksForProject_Then_correctTasksAreReturned() {
        MainTaskDTO mainTaskDTO = testHelper.addDirectorialTask(testProject.ownerCompany.asAdmin,
                TestBuilder.testCreateDirectorialTaskRequest(testProject.locationId(TOP_LOCATION),
                        DIRECTORIAL_TASK_WITH_COMPANY,
                        testProject.ownerCompany.companyId));

        ListTasksRequest listTasksRequest = TestBuilder.testListTasksRequest();

        MainTaskDTO[] mainTaskDTOS = testHelper.listMainTasksForProject(testProject.ownerCompany.asOwner,
                testProject.projectId,
                TestBuilder.testListTasksRequest());
        assertTrue(Arrays.stream(mainTaskDTOS).anyMatch(dto->dto.getId().equals(mainTaskDTO.getId())));

        ListTasksRequest showDisabledTasksRequest = listTasksRequest.toBuilder().isDisabled(true).build();
        MainTaskDTO[] disabledTaskDTOS = testHelper.listMainTasksForProject(testProject.ownerCompany.asOwner,
                testProject.projectId,
                showDisabledTasksRequest);
        assertFalse(Arrays.stream(disabledTaskDTOS).anyMatch(dto->dto.getId().equals(mainTaskDTO.getId())));
    }

    @Test
    @Order(17)
    void Given_mutableDirectorialTask_When_updateDirectorialTask_Then_success() {
        MainTaskDTO originalMainTaskDTO = testHelper.addDirectorialTask(testProject.ownerCompany.asAdmin,
                TestBuilder.testCreateDirectorialTaskRequest(testProject.locationId(TOP_LOCATION),
                                                             DIRECTORIAL_TASK_WITH_COMPANY,
                                                             testProject.ownerCompany.companyId)
        );

        testHelper.updateDirectorialTask(testProject.ownerCompany.asAdmin,
                originalMainTaskDTO.getId(),
                TestBuilder.testUpdateDirectorialTaskRequest(
                        testProject.locationId(TOP_LOCATION),
                        originalMainTaskDTO.getTitle() + "UPDATED",
                        originalMainTaskDTO.getDescription()
                )
        );

        MainTaskDTO updatedTaskDTO = testHelper.getMainTask(testProject.ownerCompany.asAdmin,originalMainTaskDTO.getId());

        assertEquals(originalMainTaskDTO.getTitle() + "UPDATED", updatedTaskDTO.getTitle());
    }

    @Test
    @Order(18)
    void Given_SavedTasks_When_loadTasksByLocation_Then_success() {
        final Map<Integer, List<Integer>> mainAndSubTaskIds = testHelper
                .listMainAndSubTasksForLocations(testProject.ownerCompany.asOwner,
                        testProject.locationId(TOP_LOCATION), ListTaskIdsRequest.builder().build());
        assertTrue(mainAndSubTaskIds.containsKey(testProject.mainTaskId(MAIN_TASK).toString()));
        assertTrue(mainAndSubTaskIds.get(testProject.mainTaskId(MAIN_TASK).toString()).contains(testProject.subTaskId(SUB_TASK_1)));
    }

    @Test
    @Order(19)
    void Given_SavedTasks_When_loadSubTasksByMainTask_Then_success() {
        final List<Integer> subTaskIds = testHelper
                .listSubTaskIds(testProject.ownerCompany.asOwner, testProject.mainTaskId(MAIN_TASK));
        assertTrue(subTaskIds.contains(testProject.subTaskId(SUB_TASK_1)));
    }

    @Test
    @Order(20)
    void Given_SavedTasks_When_loadTasksByProject_Then_success() {
        final Map<Integer, List<Integer>> taskAndSubTaskIds = testHelper.listMainAndSubTasksForProject(testProject.ownerCompany.asOwner,
                testProject.projectId, ListTaskIdsRequest
                        .builder()
                        .tradeIds(Collections.singletonList(5))
                .build());
        assertTrue(taskAndSubTaskIds.containsKey("6"));
        assertTrue(taskAndSubTaskIds.containsKey("7"));
        assertFalse(taskAndSubTaskIds.containsKey("8"));
    }
}
