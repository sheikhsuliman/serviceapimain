package com.siryus.swisscon.api.util.entitytree.nestedsets;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.entitytree.EntityTreeException;
import com.siryus.swisscon.api.util.entitytree.EntityTreeNodeDTO;
import com.siryus.swisscon.api.util.entitytree.EntityTreeService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component(EntityTreeService.ENTITY_TREE_SERVICE)
@Scope(SCOPE_PROTOTYPE)
public class NestedSetsEntityTreeService implements EntityTreeService {
    private final List<String> containerEntityTypes;
    private NestedSetsEntityTreeDAO dao;

    NestedSetsEntityTreeService() {
        this(Collections.emptyList());
    }

    NestedSetsEntityTreeService(List<String> containerEntityTypes) {
        this.containerEntityTypes = containerEntityTypes;
    }

    @Inject
    void setDao(NestedSetsEntityTreeDAO dao) {
        this.dao = dao;
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public EntityTreeNodeDTO createRoot(ReferenceType ownerReferenceType, Integer ownerReferenceId, String entityType, Integer entityId) {
        Integer newNodeId = dao.createRootNode(entityType, entityId);

        dao.createRoot(newNodeId, ownerReferenceType, ownerReferenceId);

        return new EntityTreeNodeDTO(ownerReferenceType, ownerReferenceId, newNodeId, null, true, true, entityType, entityId);
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public EntityTreeNodeDTO addChild(Integer parentNodeId, String entityType, Integer entityId) {
        EntityTreeNode parentNode = dao.getNode(parentNodeId);

        if(parentNode == null) {
            throw EntityTreeException.nodeNotFound(parentNodeId);
        }

        EntityTreeRoot root = dao.getRoot(parentNode.getRootId());

        dao.lockTreeNodes(parentNode.getRootId());

        parentNode = dao.getNode(parentNodeId);

        dao.shiftNodesRight(parentNode.getRootId(), parentNode.getRightEdge());

        Integer newNodeId = dao.createNode(parentNode.getRootId(), parentNodeId, parentNode.getRightEdge(), parentNode.getRightEdge()+1, entityType, entityId);

        return new EntityTreeNodeDTO(ReferenceType.valueOf(root.getOwnerReferenceType()), root.getOwnerReferenceId(), newNodeId, parentNodeId, true, true, entityType, entityId);
    }

    @Override
    public EntityTreeNodeDTO getNode(Integer nodeId) {
        ExtendedEntityTreeNode node = dao.getNode(nodeId);

        if(node == null) {
            throw EntityTreeException.nodeNotFound(nodeId);
        }
        EntityTreeRoot root = dao.getRoot(node.getRootId());

        return new EntityTreeNodeDTO(ReferenceType.valueOf(root.getOwnerReferenceType()), root.getOwnerReferenceId(), node.getId(), node.getParentNodeId(), node.isLeafNode(), node.isEmptyNode(), node.getEntityType(), node.getEntityId());
    }
    
    @Override
    public List<EntityTreeNodeDTO> listRoots(ReferenceType ownerReferenceType, Integer ownerReferenceId, String... entityTypes) {
        List<ExtendedEntityTreeNode> rootNodes = containerEntityTypes.isEmpty()
            ? dao.lookupRoots(ownerReferenceType, ownerReferenceId, entityTypes)
                : dao.lookupRoots(ownerReferenceType, ownerReferenceId, containerEntityTypes, entityTypes);

        return rootNodes.stream().map(n -> new EntityTreeNodeDTO(ownerReferenceType, ownerReferenceId, n.getId(), null, n.isLeafNode(), n.isEmptyNode(), n.getEntityType(), n.getEntityId())).collect(Collectors.toList());
    }

    @Override
    public List<EntityTreeNodeDTO> listChildren(Integer parentNodeId) {
        ExtendedEntityTreeNode node = dao.getNode(parentNodeId);
        if (node == null) {
            throw EntityTreeException.nodeNotFound(parentNodeId);
        }
        EntityTreeRoot root = dao.getRoot(node.getRootId());

        List<ExtendedEntityTreeNode> childrenNodes = containerEntityTypes.isEmpty()
            ? dao.lookupNodeChildren(parentNodeId)
                : dao.lookupNodeChildren(containerEntityTypes, parentNodeId);

        return childrenNodes.stream().map(n -> new EntityTreeNodeDTO(ReferenceType.valueOf(root.getOwnerReferenceType()), root.getOwnerReferenceId(), n.getId(), parentNodeId, n.isLeafNode(), n.isEmptyNode(), n.getEntityType(), n.getEntityId())).collect(Collectors.toList());
    }

    @Override
    public List<EntityTreeNodeDTO> listDescendents(Integer parentNodeId) {
        EntityTreeNode parentNode = dao.getNode(parentNodeId);
        if (parentNode == null) {
            throw EntityTreeException.nodeNotFound(parentNodeId);
        }
        EntityTreeRoot root = dao.getRoot(parentNode.getRootId());

        List<ExtendedEntityTreeNode> descendentsNodes = containerEntityTypes.isEmpty()
                ? dao.lookupNodesBetween(parentNode.getRootId(), parentNode.getLeftEdge(), parentNode.getRightEdge())
                : dao.lookupNodesBetween(parentNode.getRootId(), containerEntityTypes, parentNode.getLeftEdge(), parentNode.getRightEdge());

        return descendentsNodes.stream().map(n -> new EntityTreeNodeDTO(
                ReferenceType.valueOf(root.getOwnerReferenceType()),
                root.getOwnerReferenceId(),
                n.getId(),
                parentNodeId,
                n.isLeafNode(),
                n.isEmptyNode(),
                n.getEntityType(),
                n.getEntityId())
        ).collect(Collectors.toList());
    }

    @Override
    public List<EntityTreeNodeDTO> listNodesByEntity(Integer rootNodeId, String entityType, Integer entityId) {
        ExtendedEntityTreeNode node = dao.getNode(rootNodeId);
        if (node == null) {
            throw EntityTreeException.nodeNotFound(rootNodeId);
        }
        EntityTreeRoot root = dao.getRoot(node.getRootId());

        List<ExtendedEntityTreeNode> nodes = containerEntityTypes.isEmpty()
                ? dao.lookupNodesByEntity(rootNodeId, entityType, entityId)
                : dao.lookupNodesByEntity(containerEntityTypes, rootNodeId, entityType, entityId);

        return nodes.stream().map(n -> new EntityTreeNodeDTO(
                ReferenceType.valueOf(root.getOwnerReferenceType()),
                root.getOwnerReferenceId(),
                n.getId(),
                n.getParentNodeId(),
                n.isLeafNode(),
                n.isEmptyNode(),
                n.getEntityType(),
                n.getEntityId())
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void deleteNode(Integer nodeId) {
        EntityTreeNode node = dao.getNode(nodeId);

        if (! node.isEmptyNode()) {
            throw EntityTreeException.nodeIsNotEmpty();
        }

        dao.lockTreeNodes(node.getRootId());

        node = dao.getNode(nodeId);

        if (node.getParentNodeId() == null) {
            dao.deleteRootNode(nodeId);
        }
        dao.shiftNodesLeft(node.getRootId(), node.getRightEdge());
        dao.deleteNode(nodeId);
    }

    @Transactional
    @Override
    public void deleteAllOwnedBy(ReferenceType referenceType, Integer referenceId) {
        listRoots(referenceType, referenceId).forEach(this::deleteRootAndNodes);
    }

    private void deleteRootAndNodes(EntityTreeNodeDTO root) {
        dao.deleteNodesWithRoot(root.getId());
        dao.deleteRootNode(root.getId());
    }

    @Override
    public void dumpRoots() {
        dao.dumpRootsCreationScript();
    }
    
    @Override
    public void dumpNodes() {
        dao.dumpNodesCreationScript();
    }
}
