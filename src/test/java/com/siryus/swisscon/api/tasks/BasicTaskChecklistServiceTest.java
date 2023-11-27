package com.siryus.swisscon.api.tasks;

import com.siryus.swisscon.api.base.AbstractUnitTestBase;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;
import com.siryus.swisscon.api.tasks.dto.AddTaskChecklistItemRequest;
import com.siryus.swisscon.api.tasks.dto.EditTaskChecklistItemRequest;
import com.siryus.swisscon.api.tasks.dto.IdResponse;
import com.siryus.swisscon.api.tasks.dto.TaskChecklistItem;
import com.siryus.swisscon.api.tasks.entity.SubTaskCheckListEntity;
import com.siryus.swisscon.api.tasks.entity.SubTaskEntity;
import com.siryus.swisscon.api.tasks.repos.SubTaskCheckListRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BasicTaskChecklistServiceTest extends AbstractUnitTestBase {

    private static final Integer VALID_WORKER_ID = 2122;

    private static final Integer MAIN_TASK_ID = 13;
    private static final Integer INVALID_MAIN_TASK_ID = 1;
    private static final Integer DEFAULT_SUB_TASK_ID = 42;
    private static final Integer SUB_TASK_ID = 666;
    private static final Integer INVALID_SUB_TASK_ID = 2;
    private static final SubTaskEntity subTask = SubTaskEntity.builder().id(DEFAULT_SUB_TASK_ID).build();

    private static final Integer DEFAULT_ID = 999;
    private static final String JUST_DO_IT = "Just DO It";
    private static final String JUST_DONT_DO_IT = "Don't DO It";

    private static final Integer VALID_SUB_TASK_CHECKLIST_ID =4;


    private static List<SubTaskCheckListEntity> DEFAULT_SUB_TASK_CHECKLIST = Arrays.asList(
            new SubTaskCheckListEntity(1, DEFAULT_SUB_TASK_ID, "Do sub things"),
            new SubTaskCheckListEntity(2, DEFAULT_SUB_TASK_ID, "Do other sub things"),
            new SubTaskCheckListEntity(3, DEFAULT_SUB_TASK_ID, "Do sub things again")
    );

    private static List<SubTaskCheckListEntity> SUB_TASK_CHECKLIST = Arrays.asList(
            new SubTaskCheckListEntity(VALID_SUB_TASK_CHECKLIST_ID, SUB_TASK_ID,"Hail Satan"),
            new SubTaskCheckListEntity(5,SUB_TASK_ID, "Hail Satan"),
            new SubTaskCheckListEntity(6,SUB_TASK_ID, "Order Wanton Soup")
    );

    @Mock
    SubTaskRepository subTaskRepository;

    @Mock
    SubTaskCheckListRepository subTaskCheckListRepository;

    BasicTaskChecklistService service;


    @BeforeEach
    void setupMocks() {
        MockitoAnnotations.initMocks(this);

        service = new BasicTaskChecklistService();
        service.setSubTaskRepository(subTaskRepository);
        service.setSubTaskCheckListRepository(subTaskCheckListRepository);

        when(subTaskRepository.getDefaultSubTask(MAIN_TASK_ID)).thenReturn(Optional.of(subTask));
        when(subTaskRepository.getDefaultSubTask(INVALID_MAIN_TASK_ID)).thenReturn(Optional.empty());
        when(subTaskRepository.existsById(DEFAULT_SUB_TASK_ID)).thenReturn(true);
        when(subTaskRepository.existsById(SUB_TASK_ID)).thenReturn(true);
        when(subTaskRepository.existsById(INVALID_SUB_TASK_ID)).thenReturn(false);

        when(subTaskCheckListRepository.findAllBySubTaskId(DEFAULT_SUB_TASK_ID)).thenReturn(DEFAULT_SUB_TASK_CHECKLIST);
        when(subTaskCheckListRepository.findAllBySubTaskId(SUB_TASK_ID)).thenReturn(SUB_TASK_CHECKLIST);
        when(subTaskCheckListRepository.findAllBySubTaskId(INVALID_SUB_TASK_ID)).thenReturn(Collections.emptyList());

        when(subTaskCheckListRepository.save(any(SubTaskCheckListEntity.class))).then((Answer<SubTaskCheckListEntity>) invocation -> {
            SubTaskCheckListEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(DEFAULT_ID);
            }
            return entity;
        });

        when(subTaskCheckListRepository.findById(VALID_SUB_TASK_CHECKLIST_ID)).thenReturn(Optional.of(SUB_TASK_CHECKLIST.get(0)));
    }

    @AfterEach
    void clearThings() {
        unMockApplicationUser();
    }

    @Test
    void Given_validMainTaskID_When_getMainTaskChecklistItems_Then_returnDefaultSubTaskList() {
        List<TaskChecklistItem> result = service.getMainTaskChecklistItems(MAIN_TASK_ID);

        assertEquals(DEFAULT_SUB_TASK_CHECKLIST.size(), result.size());
        assertEquals(DEFAULT_SUB_TASK_CHECKLIST.get(0).getTitle(), result.get(0).getTitle());
    }

    @Test
    void Given_invalidMainTaskID_When_getMainTaskChecklistItems_Then_throw() {
        assertThrows(LocalizedResponseStatusException.class, () -> {
            service.getMainTaskChecklistItems(INVALID_MAIN_TASK_ID);
        });
    }

    @Test
    void Given_validSubTaskID_When_getSubTaskChecklistItems_Then_returnSubTaskList() {
        List<TaskChecklistItem> result = service.getSubTaskChecklistItems(SUB_TASK_ID);

        assertEquals(SUB_TASK_CHECKLIST.size(), result.size());
        assertEquals(SUB_TASK_CHECKLIST.get(0).getTitle(), result.get(0).getTitle());
    }

    @Test
    void Given_invalidSubTaskId_When_getSubTaskChecklistItems_Then_throw() {
        assertThrows(LocalizedResponseStatusException.class, () -> {
            service.getSubTaskChecklistItems(INVALID_SUB_TASK_ID);
        });
    }

    @Test
    void Given_validSubTask_When_addChecklistItem_Then_saveEntity() {
        IdResponse result = service.addSubTaskChecklistItem(SUB_TASK_ID, AddTaskChecklistItemRequest.builder().title(JUST_DO_IT).build());

        ArgumentCaptor<SubTaskCheckListEntity> saveEntityCaptor = ArgumentCaptor.forClass(SubTaskCheckListEntity.class);

        verify(subTaskCheckListRepository).save(saveEntityCaptor.capture());

        assertEquals(DEFAULT_ID, result.getId());
        assertEquals(JUST_DO_IT, saveEntityCaptor.getValue().getTitle());
        assertEquals(SUB_TASK_ID, saveEntityCaptor.getValue().getSubTaskId());
    }

    @Test
    void Given_validMainTask_When_addChecklistItem_Then_saveEntity() {
        IdResponse result = service.addMainTaskChecklistItem(MAIN_TASK_ID, AddTaskChecklistItemRequest.builder().title(JUST_DO_IT).build());

        ArgumentCaptor<SubTaskCheckListEntity> saveEntityCaptor = ArgumentCaptor.forClass(SubTaskCheckListEntity.class);

        verify(subTaskCheckListRepository).save(saveEntityCaptor.capture());

        assertEquals(DEFAULT_ID, result.getId());
        assertEquals(JUST_DO_IT, saveEntityCaptor.getValue().getTitle());
        assertEquals(DEFAULT_SUB_TASK_ID, saveEntityCaptor.getValue().getSubTaskId());
    }

    @Test
    void Given_validCheklistId_When_editTaskChecklistItem_Then_saveUpdatedEntity() {
        service.editTaskChecklistItem(VALID_SUB_TASK_CHECKLIST_ID, new EditTaskChecklistItemRequest(JUST_DONT_DO_IT));

        ArgumentCaptor<SubTaskCheckListEntity> saveEntityCaptor = ArgumentCaptor.forClass(SubTaskCheckListEntity.class);
        verify(subTaskCheckListRepository).save(saveEntityCaptor.capture());

        assertEquals(VALID_SUB_TASK_CHECKLIST_ID, saveEntityCaptor.getValue().getId());
        assertEquals(JUST_DONT_DO_IT, saveEntityCaptor.getValue().getTitle());
    }

    @Test
    void Given_validChecklistId_When_onCTaskCheckListItem_Then_saveCheckedEntity() {
        mockApplicationUser(VALID_WORKER_ID);

        service.onTaskChecklistItem(VALID_SUB_TASK_CHECKLIST_ID);

        ArgumentCaptor<SubTaskCheckListEntity> saveEntityCaptor = ArgumentCaptor.forClass(SubTaskCheckListEntity.class);

        verify(subTaskCheckListRepository).save(saveEntityCaptor.capture());

        assertEquals(VALID_SUB_TASK_CHECKLIST_ID, saveEntityCaptor.getValue().getId());
        assertNotNull(saveEntityCaptor.getValue().getCheckedDate());
        assertEquals(VALID_WORKER_ID, saveEntityCaptor.getValue().getCheckedBy());
    }

    @Test
    void Given_validChecklistId_When_deleteTaskChecklistItem_Then_deleteCorrectEntity() {
        service.deleteTaskChecklistItem(VALID_SUB_TASK_CHECKLIST_ID);

        ArgumentCaptor<SubTaskCheckListEntity> deleteEntityCaptor = ArgumentCaptor.forClass(SubTaskCheckListEntity.class);

        verify(subTaskCheckListRepository).delete(deleteEntityCaptor.capture());

        assertEquals(VALID_SUB_TASK_CHECKLIST_ID, deleteEntityCaptor.getValue().getId());
    }
}