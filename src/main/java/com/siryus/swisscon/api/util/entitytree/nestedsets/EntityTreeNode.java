package com.siryus.swisscon.api.util.entitytree.nestedsets;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "entity_tree_node")
public class EntityTreeNode {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Integer id;

    @Column(name = "root_id")
    private Integer rootId;

    @Column(name = "parent_id")
    private Integer parentNodeId;

    @Column(name = "left_edge")
    private Integer leftEdge;

    @Column(name = "right_edge")
    private Integer rightEdge;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private Integer entityId;


    public EntityTreeNode() {
    }

    EntityTreeNode(String entityType, Integer entityId) {
        this(null, null, 1, 2, entityType, entityId);
    }

    EntityTreeNode(Integer rootId, Integer parentNodeId, Integer leftEdge, Integer rightEdge, String entityType, Integer entityId) {
        this(null, rootId, parentNodeId, leftEdge, rightEdge, entityType, entityId);
    }

    EntityTreeNode(Integer id, Integer rootId, Integer parentNodeId, Integer leftEdge, Integer rightEdge, String entityType, Integer entityId) {
        this.id = id;
        this.rootId = rootId;
        this.parentNodeId = parentNodeId;
        this.leftEdge = leftEdge;
        this.rightEdge = rightEdge;
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public Integer getId() {
        return id;
    }

    Integer getParentNodeId() {
        return parentNodeId;
    }

    Integer getRootId() {
        return rootId;
    }

    Integer getLeftEdge() {
        return leftEdge;
    }

    Integer getRightEdge() {
        return rightEdge;
    }

    String getEntityType() {
        return entityType;
    }

    Integer getEntityId() {
        return entityId;
    }

    void setRootId(Integer rootId) {
        this.rootId = rootId;
    }

    boolean isEmptyNode() {
        return leftEdge + 1 == rightEdge;
    }
}
