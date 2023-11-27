package com.siryus.swisscon.api.util.entitytree.nestedsets;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.entitytree.EntityTreeException;
import com.siryus.swisscon.api.util.entitytree.EntityTreeNodeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NestedSetsEntityTreeServiceTest {
    private static final String ENTITY_TYPE = "answer";
    private static final List<String> CONTAINER_TYPES = Collections.emptyList();

    private static final int ENTITY_ID = 42;

    private static final int REFERENCE_ID_1 = 1;
    private static final int NODE_13 = 13;
    private static final int NODE_14 = 14;

    @Mock
    private NestedSetsEntityTreeDAO dao;

    private NestedSetsEntityTreeService service = new NestedSetsEntityTreeService(CONTAINER_TYPES);

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.initMocks(this);
        service.setDao(dao);
        reset(dao);
    }

    @Test
    void When_createRoot_Then_callCreateRootNodeAndCreateRoot() {
        when(dao.createRootNode(ENTITY_TYPE, ENTITY_ID)).thenReturn(NODE_13);

        EntityTreeNodeDTO result = service.createRoot(ReferenceType.COMPANY, REFERENCE_ID_1, ENTITY_TYPE, ENTITY_ID);

        assertNotNull(result);
        assertEquals(NODE_13, (int) result.getId());
        assertNull(result.getParentNodeId());
        assertTrue(result.isLeaf());
        assertEquals(ENTITY_TYPE, result.getEntityType());
        assertEquals(ENTITY_ID, (int)result.getEntityId());

        ArgumentCaptor<String> entityTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> entityIdCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(dao).createRootNode(entityTypeCaptor.capture(), entityIdCaptor.capture());

        assertEquals(ENTITY_TYPE, entityTypeCaptor.getValue());
        assertEquals(ENTITY_ID, (int)entityIdCaptor.getValue());

        ArgumentCaptor<Integer> rootNodeIdCapture = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<ReferenceType> referenceTypeCapture = ArgumentCaptor.forClass(ReferenceType.class);
        ArgumentCaptor<Integer> referenceIdCapture = ArgumentCaptor.forClass(Integer.class);

        verify(dao).createRoot(rootNodeIdCapture.capture(), referenceTypeCapture.capture(), referenceIdCapture.capture());

        assertEquals(NODE_13, (int)rootNodeIdCapture.getValue());
        assertEquals(ReferenceType.COMPANY, referenceTypeCapture.getValue());
        assertEquals(REFERENCE_ID_1, (int)referenceIdCapture.getValue());
    }

    @Test
    void Given_parentIdIsNotValid_When_addChild_Then_throwException() {
        when(dao.getNode(NODE_13)).thenReturn(null);

        try {
            service.addChild(NODE_13, ENTITY_TYPE, ENTITY_ID);
            fail("Exception expected");
        } catch (EntityTreeException e) {
            // expected
        }
    }

    @Test
    void Given_parentIdIsValid_When_addChild_Then_createNewNodeAndShiftNodesRight() {
        when(dao.getRoot(NODE_13)).thenReturn(new EntityTreeRoot(NODE_13, ReferenceType.PROJECT.name(), 1));
        when(dao.getNode(NODE_13)).thenReturn(new ExtendedEntityTreeNode(NODE_13, NODE_13, null, 1, 2, ENTITY_TYPE, ENTITY_ID, true));
        when(dao.createNode(NODE_13, NODE_13, 2, 3, ENTITY_TYPE, ENTITY_ID)).thenReturn(NODE_14);

        EntityTreeNodeDTO result = service.addChild(NODE_13, ENTITY_TYPE, ENTITY_ID);

        assertNotNull(result);
        assertEquals(NODE_14, (int) result.getId());
        assertEquals(NODE_13, (int) result.getParentNodeId());
        assertTrue(result.isLeaf());
        assertEquals(ENTITY_TYPE, result.getEntityType());
        assertEquals(ENTITY_ID, (int)result.getEntityId());

        verify(dao, times(1)).lockTreeNodes(anyInt());

        ArgumentCaptor<Integer> rootNodeIdCapture = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> shiftPointCapture = ArgumentCaptor.forClass(Integer.class);

        verify(dao, times(0)).shiftNodesLeft(anyInt(), anyInt());
        verify(dao).shiftNodesRight(rootNodeIdCapture.capture(), shiftPointCapture.capture());

        assertEquals(NODE_13, (int)rootNodeIdCapture.getValue());
        assertEquals(2, (int)shiftPointCapture.getValue());

        ArgumentCaptor<Integer> rootIdCapture = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> parentIdCapture = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> leftCapture = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> rightCapture = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> entityTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> entityIdCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(dao, times(1)).createNode(rootIdCapture.capture(), parentIdCapture.capture(), leftCapture.capture(), rightCapture.capture(), entityTypeCaptor.capture(), entityIdCaptor.capture());

        assertEquals(NODE_13, (int)rootIdCapture.getValue());
        assertEquals(NODE_13, (int)parentIdCapture.getValue());
        assertEquals(2, (int)leftCapture.getValue());
        assertEquals(3, (int)rightCapture.getValue());
        assertEquals(ENTITY_TYPE, entityTypeCaptor.getValue());
        assertEquals(ENTITY_ID, (int)entityIdCaptor.getValue());
    }

    @Test
    void When_lookupRoots_Then_returnCorrectListOfTreeNodes() {
        when(dao.lookupRoots(ReferenceType.COMPANY, REFERENCE_ID_1, ENTITY_TYPE)).thenReturn(
                Arrays.asList(
                        new ExtendedEntityTreeNode(NODE_13, NODE_13, null, 1, 2, ENTITY_TYPE, ENTITY_ID, true),
                        new ExtendedEntityTreeNode(NODE_14, NODE_14, null, 1, 2, ENTITY_TYPE, ENTITY_ID, true)
                )
        );

        List<EntityTreeNodeDTO> result = service.listRoots(ReferenceType.COMPANY, REFERENCE_ID_1, ENTITY_TYPE);

        assertEquals(2, result.size());
        EntityTreeNodeDTO nodeDTO = result.get(0);

        assertNotNull(result);
        assertEquals(NODE_13, (int) nodeDTO.getId());
        assertNull(nodeDTO.getParentNodeId());
        assertTrue(nodeDTO.isLeaf());
        assertEquals(ENTITY_TYPE, nodeDTO.getEntityType());
        assertEquals(ENTITY_ID, (int)nodeDTO.getEntityId());

    }
}