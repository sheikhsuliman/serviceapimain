package com.siryus.swisscon.api.util.entitytree;

import com.siryus.swisscon.api.general.reference.ReferenceType;

public class EntityTreeNodeDTO {
    private final ReferenceType ownerReferenceType;
    private final Integer ownerReferenceId;
    private final Integer id;
    private final Integer parentNodeId;
    private final boolean leaf;
    private final boolean empty;

    private final String entityType;
    private final Integer entityId;

    public EntityTreeNodeDTO(ReferenceType ownerReferenceType, Integer ownerReferenceId, Integer id, Integer parentNodeId, boolean leaf, boolean empty, String entityType, Integer entityId) {
        this.ownerReferenceType = ownerReferenceType;
        this.ownerReferenceId = ownerReferenceId;
        this.id = id;
        this.parentNodeId = parentNodeId;
        this.leaf = leaf;
        this.empty = empty;
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public ReferenceType getOwnerReferenceType() {
        return ownerReferenceType;
    }

    public Integer getOwnerReferenceId() {
        return ownerReferenceId;
    }

    public Integer getId() {
        return id;
    }

    public Integer getParentNodeId() {
        return parentNodeId;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public boolean isEmpty() {
        return empty;
    }
    public String getEntityType() {
        return entityType;
    }

    public Integer getEntityId() {
        return entityId;
    }
}
