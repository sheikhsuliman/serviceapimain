package com.siryus.swisscon.api.util.entitytree.nestedsets;

class ExtendedEntityTreeNode extends EntityTreeNode {

    private final Boolean leafNode;

    ExtendedEntityTreeNode(EntityTreeNode node, Boolean leafNode) {
        this(node.getId(), node.getRootId(), node.getParentNodeId(), node.getLeftEdge(), node.getRightEdge(), node.getEntityType(), node.getEntityId(), leafNode);
    }

    ExtendedEntityTreeNode(Integer id, Integer rootId, Integer parentNodeId, Integer leftEdge, Integer rightEdge, String entityType, Integer entityId, Boolean leafNode) {
        super(id, rootId, parentNodeId, leftEdge, rightEdge, entityType, entityId);
        this.leafNode = leafNode;
    }

    Boolean isLeafNode() {
        return leafNode;
    }
}
