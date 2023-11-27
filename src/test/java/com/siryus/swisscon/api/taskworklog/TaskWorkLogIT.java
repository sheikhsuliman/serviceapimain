/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.taskworklog;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileRepository;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import com.siryus.swisscon.api.tasks.dto.SubTaskDTO;
import com.siryus.swisscon.api.taskworklog.dto.EventHistoryDTO;
import com.siryus.swisscon.api.taskworklog.dto.MainTaskDurationDTO;
import com.siryus.swisscon.api.taskworklog.dto.SubTaskDurationDTO;
import com.siryus.swisscon.api.taskworklog.dto.TaskStatusDTO;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskWorkLogIT extends AbstractMvcTestBase {
    private static final String MAIN_TASK = "Main Task";
    private static final String SUB_TASK_1 = "Sub Task 1";

    private final FileRepository fileRepository;

    private TestHelper.ExtendedTestProject testProject;

    @Autowired
    public TaskWorkLogIT(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @BeforeEach
    void doBeforeEach() {
        cleanDatabase();

        testProject = testHelper.createExtendedProject();

        testHelper.addLocationToExtendedProject(testProject, "TOP");
        MainTaskDTO mainTaskDTO = testHelper.addContractualTask(testProject.ownerCompany.asAdmin, TestBuilder.testCreateMainTaskRequest(testProject.locationId("TOP"), MAIN_TASK, testProject.contractorCompany.companyId));
        testProject.addMainTask(MAIN_TASK, mainTaskDTO.getId());
        SubTaskDTO subTaskDTO = testHelper.addSubTask(testProject.contractorCompany.asOwner, TestBuilder.testCreateSubTaskRequest(testProject.mainTaskId(MAIN_TASK), SUB_TASK_1));
        testProject.addSubTask(SUB_TASK_1, subTaskDTO.getId());

        testHelper.addUserToSubTask(testProject.contractorCompany.asOwner, testProject.subTaskId(SUB_TASK_1), testProject.contractorCompany.workerId);
    }

    private void createWorkLogs() {
        Random random=new Random(0);

        offsetClock(Duration.ofHours(-10L));
        testHelper.startTimer(testProject.contractorCompany.asOwner, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId(MAIN_TASK), null, "start"));
        offsetClock(Duration.ofSeconds(1 + random.nextInt(10)));
        testHelper.startTimer(testProject.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject.subTaskId(SUB_TASK_1), "start"));
        offsetClock(Duration.ofSeconds(1 + random.nextInt(10)));
        testHelper.stopTimer(testProject.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject.subTaskId(SUB_TASK_1), "stop"));
        offsetClock(Duration.ofSeconds(1 + random.nextInt(10)));
        testHelper.stopTimer(testProject.contractorCompany.asOwner, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId(MAIN_TASK), null, "stop"));
        offsetClock(Duration.ofSeconds(1 + random.nextInt(10)));
        testHelper.startTimer(testProject.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject.subTaskId(SUB_TASK_1), "start"));
        offsetClock(Duration.ofSeconds(1 + random.nextInt(10)));
        testHelper.stopTimer(testProject.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject.subTaskId(SUB_TASK_1), "stop"));
        offsetClock(Duration.ofSeconds(1 + random.nextInt(10)));
        testHelper.startTimer(testProject.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject.subTaskId(SUB_TASK_1), "start"));
        offsetClock(Duration.ofSeconds(1 + random.nextInt(10)));
        testHelper.startTimer(testProject.contractorCompany.asOwner, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId(MAIN_TASK), null, "start"));
        offsetClock(Duration.ofSeconds(1 + random.nextInt(10)));
        testHelper.stopTimer(testProject.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject.subTaskId(SUB_TASK_1), "stop"));
        offsetClock(Duration.ofSeconds(1 + random.nextInt(10)));
        testHelper.stopTimer(testProject.contractorCompany.asOwner, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId(MAIN_TASK), null, "stop"));
    }

    @Test
    public void Given_validMainTaskId_When_getMainTaskDuration_Then_returnCorrectResult() {
        createWorkLogs();

        MainTaskDurationDTO durationDTO = testHelper.getMainTaskDuration(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK));

        assertNotNull(durationDTO);
        assertEquals(testProject.mainTaskId(MAIN_TASK), durationDTO.getMainTaskId());
        assertEquals(51, durationDTO.getTotalDurationInSeconds());
        assertEquals(2, durationDTO.getSubTasksDuration().size());
    }

    @Test
    public void Given_validSubTaskId_When_getSubTaskDuration_Then_returnCorrectResult() {
        createWorkLogs();

        SubTaskDurationDTO durationDTO = testHelper.getSubTaskDuration(testProject.contractorCompany.asOwner, testProject.subTaskId(SUB_TASK_1));

        assertNotNull(durationDTO);
        assertEquals(testProject.subTaskId(SUB_TASK_1), durationDTO.getSubTaskId());
        assertEquals(19, durationDTO.getTotalDurationInSeconds());
        assertEquals(1, durationDTO.getWorkersWorklogs().size());
    }

    @Test
    void Given_validSubTaskIdAndWorkerId_When_getSubTaskWorkerDuration_Then_returnCorrectResult() {
        createWorkLogs();

        SubTaskDurationDTO durationDTO = testHelper.getSubTaskWorkerDuration(testProject.contractorCompany.asOwner, testProject.subTaskId(SUB_TASK_1), testProject.contractorCompany.workerId);

        assertNotNull(durationDTO);
        assertEquals(testProject.subTaskId(SUB_TASK_1), durationDTO.getSubTaskId());
        assertEquals(19, durationDTO.getTotalDurationInSeconds());
        assertEquals(1, durationDTO.getWorkersWorklogs().size());
        assertEquals(testProject.contractorCompany.workerId, durationDTO.getWorkersWorklogs().get(0).getWorker().getId());
    }

    @Test
    void Given_timerWasCancelled_When_getSubTaskWorkerDuration_Then_timerIsNotPartOfTotalDuration() {
        createWorkLogs();

        SubTaskDurationDTO initialDurationDTO = testHelper.getSubTaskWorkerDuration(testProject.contractorCompany.asOwner, testProject.subTaskId(SUB_TASK_1), testProject.contractorCompany.workerId);

        testHelper.cancelSubTaskTimerWorkLog(testProject.contractorCompany.asOwner, initialDurationDTO.getSubTaskId(), initialDurationDTO.getWorkersWorklogs().get(0).getTimeWorklogs().get(0).getStartTimeWorklogId());

        SubTaskDurationDTO updatedDurationDTO = testHelper.getSubTaskWorkerDuration(testProject.contractorCompany.asOwner, testProject.subTaskId(SUB_TASK_1), testProject.contractorCompany.workerId);

        assertNotNull(updatedDurationDTO);
        assertEquals(
                initialDurationDTO.getWorkersWorklogs().get(0).getTimeWorklogs().get(0).getFullDurationInSeconds(),
                initialDurationDTO.getTotalDurationInSeconds() - updatedDurationDTO.getTotalDurationInSeconds()
        );
    }
    /**
     * Tests whether on a test project with 2 workers, attachments are saved when associated with the right event type
     */
    @Test
    public void Given_eventHasAttachments_When_startTimerOnAssignedTask_Then_attachmentsShouldBeSaved() {
        File file1 = testHelper.fileUploadTemporary(testProject.contractorCompany.asWorker);
        File file2 = testHelper.fileUploadTemporary(testProject.contractorCompany.asWorker);

        TaskStatusDTO result = testHelper.startTimer(
            testProject.contractorCompany.asWorker,         
            TestBuilder.testTaskWorklogRequest(
                    testProject.mainTaskId(MAIN_TASK), testProject.subTaskId(SUB_TASK_1),  "Work Is Hard",
                dto -> dto.toBuilder().attachmentIDs(Arrays.asList(file1.getId(), file2.getId())).build()
            )
        );

        assertNotNull(result, "Start timer event failed");
        assertTrue(result.getAttachments() != null && result.getAttachments().size() == 2 , "Wrong number of attachments, expected 1");
        assertEquals(result.getAttachments().get(0).getFileId(), file1.getId(), "Invalid attachment id for first file");
        assertEquals(result.getAttachments().get(1).getFileId(), file2.getId(), "Invalid attachment id for second file");
        
        List<EventHistoryDTO> history = testHelper.mainTaskWorkLogHistory(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK));
        
        assertTrue(history != null && !history.isEmpty(), "History has attachments");
        assertTrue(history.stream().anyMatch(e -> 
            e.getFiles().size() == 2 && 
            e.getFiles().get(0).getFileId().equals(file1.getId()) &&
            e.getFiles().get(1).getFileId().equals(file2.getId())), "Attachments not found");

        TestAssert.assertAttachment(fileRepository.findById(file1.getId()).orElseThrow(), ReferenceType.MAIN_TASK, testProject.mainTaskId(MAIN_TASK));
        TestAssert.assertAttachment(fileRepository.findById(file2.getId()).orElseThrow(), ReferenceType.MAIN_TASK, testProject.mainTaskId(MAIN_TASK));
    }

    /**
     * Check that all events which should appear in Activity Log do appear on task complete.
     * The events we are looking for w.r. to task completion are:
     * - Start of Main Task
     * - Aggregations of Start / Stop on sub-tasks
     * - Complete Main Task
     */
    @Test
    public void Given_workerStopsWork_When_managerCompletesTask_Then_historyHasReportedEvents() {
        List<EventHistoryDTO> history = testHelper.mainTaskWorkLogHistory(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK));
        assertTrue(history == null || history.isEmpty(), "Non empty task log history");

        TaskStatusDTO result = testHelper.startTimer(
                testProject.contractorCompany.asWorker,
                TestBuilder.testTaskWorklogRequest(testProject.mainTaskId(MAIN_TASK), testProject.subTaskId(SUB_TASK_1), "Work Is Hard")
        );

        assertNotNull(result, "Start timer failed");

        history = testHelper.mainTaskWorkLogHistory(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK));
        assertNotNull(history, "Null result for activity log history");

        assertEquals(1, history.stream().filter(
                h -> h.getType() == WorkLogEventType.START_TASK
        ).count(), "START_TASK event not found");

        assertEquals(1, history.stream().filter(
                h -> h.getType() == WorkLogEventType.START_TIMER && h.getDuration() != null && !h.getStartCompleted()
        ).count(), "START_TIMER event not found");

        result = testHelper.stopTimer(
                testProject.contractorCompany.asWorker,
                TestBuilder.testTaskWorklogRequest(testProject.mainTaskId(MAIN_TASK), testProject.subTaskId(SUB_TASK_1), "Work Is Hard")
        );

        assertNotNull(result, "Stop timer failed");

        result = testHelper.startTimer(
                testProject.contractorCompany.asWorker,
                TestBuilder.testTaskWorklogRequest(testProject.mainTaskId(MAIN_TASK), testProject.subTaskId(SUB_TASK_1), "Work Is Hard")
        );

        assertNotNull(result, "Start timer failed");

        history = testHelper.mainTaskWorkLogHistory(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK));

        // We should see a 1 x START_TASK and 2 x START_TIMER, one of which is complete

        assertNotNull(history, "No activity logs returned");
        assertEquals(1, history.stream().filter(h -> h.getType() == WorkLogEventType.START_TASK).count(), "START_TASK event not found");
        assertEquals(2, history.stream().filter(h -> h.getType() == WorkLogEventType.START_TIMER).count(), "START_TIMER events not found");

        assertEquals(1, history.stream().filter(
                h -> h.getStartCompleted() != null && h.getStartCompleted() && h.getType() == WorkLogEventType.START_TIMER
        ).count(), "Completed start event not found");

        assertEquals(1, history.stream().filter(
                h -> h.getStartCompleted() != null && !h.getStartCompleted() && h.getType() == WorkLogEventType.START_TIMER
        ).count(), "Incomplete start event not found");

        assertEquals(2, history.stream().filter(
                h -> h.getDuration() != null
        ).count(), "Duration is not set on expected items");

        result = testHelper.completeTask(
                testProject.contractorCompany.asOwner,
                TestBuilder.testTaskWorklogRequest(testProject.mainTaskId(MAIN_TASK), null, "Work Is Done")
        );

        assertNotNull(result, "Task completion failed");

        history = testHelper.mainTaskWorkLogHistory(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK));

        assertEquals(1, history.stream().filter(h -> h.getType() == WorkLogEventType.START_TASK).count(), "START_TASK event not found");
        assertEquals(1, history.stream().filter(h -> h.getType() == WorkLogEventType.COMPLETE_TASK).count(), "COMPLETE_TASK event not found");

        assertEquals(2, history.stream().filter(
                h -> h.getStartCompleted() != null && h.getStartCompleted() && h.getType() == WorkLogEventType.START_TIMER
        ).count(), "Completed start events not found");

        assertTrue(
                history.stream().noneMatch(h -> h.getStartCompleted() != null && !h.getStartCompleted() && h.getType() == WorkLogEventType.START_TIMER),
                "Incomplete start events present"
        );

        assertEquals(2, history.stream().filter(h -> h.getDuration() != null).count(), "Duration is not set on expected items");
    }
}
