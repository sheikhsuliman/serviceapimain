package com.siryus.swisscon.api.util.entitytree;

import com.siryus.swisscon.api.general.reference.ReferenceType;

import java.util.List;

public interface EntityTreeService {
    String ENTITY_TREE_SERVICE = "EntityTreeService";

    EntityTreeNodeDTO createRoot(ReferenceType ownerReferenceType, Integer ownerReferenceId, String entityType, Integer entityId);
    EntityTreeNodeDTO addChild(Integer parentNodeId, String entityType, Integer entityId);

    EntityTreeNodeDTO getNode(Integer nodeId);

    List<EntityTreeNodeDTO> listRoots(ReferenceType ownerReferenceType, Integer ownerReferenceId, String... entityTypes);
    List<EntityTreeNodeDTO> listChildren(Integer parentNodeId);
    List<EntityTreeNodeDTO> listDescendents(Integer parentNodeId);

    void deleteNode(Integer nodeId);

    List<EntityTreeNodeDTO> listNodesByEntity(Integer rootNodeId, String entityType, Integer entityId);

    void dumpRoots();
    
    void dumpNodes();

    void deleteAllOwnedBy(ReferenceType referenceType, Integer referenceId);
}
