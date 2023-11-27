package com.siryus.swisscon.api.util.entitytree.nestedsets;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
class NestedSetsEntityTreeDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(NestedSetsEntityTreeDAO.class);
    @PersistenceContext
    private EntityManager em;


    void lockTreeNodes(Integer rootId) {
        em.find(EntityTreeNode.class, rootId, LockModeType.PESSIMISTIC_WRITE);
    }

    EntityTreeRoot getRoot(Integer rootId) {
        return em.find(EntityTreeRoot.class, rootId);
    }

    void createRoot(Integer id, ReferenceType ownerReferenceType, Integer ownerReferenceId) {
        EntityTreeRoot root = new EntityTreeRoot(id, ownerReferenceType.name(), ownerReferenceId);

        em.persist(root);
    }

    Integer createRootNode(String entityType, Integer entityId) {
        EntityTreeNode newRootNode = new EntityTreeNode(entityType, entityId);
        em.persist(newRootNode);

        newRootNode.setRootId(newRootNode.getId());
        em.merge(newRootNode);

        return newRootNode.getId();
    }

    Integer createNode(Integer rootId, Integer parentNodeId, Integer left, Integer right, String entityType, Integer entityId) {
        EntityTreeNode newNode = new EntityTreeNode(rootId, parentNodeId, left, right, entityType, entityId);
        em.persist(newNode);

        return newNode.getId();
    }

    void shiftNodesLeft(Integer rootNodeId, Integer shiftPoint) {
        em.createNativeQuery(
                "UPDATE entity_tree_node " +
                        "   SET " +
                        "       left_edge = CASE WHEN left_edge >= :shiftPoint THEN left_edge - 2 ELSE left_edge END, " +
                        "       right_edge = CASE WHEN right_edge >= :shiftPoint THEN right_edge - 2 ELSE right_edge END" +
                        "   WHERE " +
                        "       ( left_edge >= :shiftPoint OR right_edge >= :shiftPoint )" +
                        "       AND root_id = :rootNodeId"
        )
                .setParameter("shiftPoint", shiftPoint)
                .setParameter("rootNodeId", rootNodeId)
                .executeUpdate();
    }

    void shiftNodesRight(Integer rootNodeId, Integer shiftPoint) {
        em.createNativeQuery(
                "UPDATE entity_tree_node " +
                        "   SET " +
                        "       left_edge = CASE WHEN left_edge >= :shiftPoint THEN left_edge + 2 ELSE left_edge END, " +
                        "       right_edge = CASE WHEN right_edge >= :shiftPoint THEN right_edge + 2 ELSE right_edge END" +
                        "   WHERE " +
                        "       ( left_edge >= :shiftPoint OR right_edge >= :shiftPoint )" +
                        "       AND root_id = :rootNodeId"
        )
                .setParameter("shiftPoint", shiftPoint)
                .setParameter("rootNodeId", rootNodeId)
                .executeUpdate();
    }

    ExtendedEntityTreeNode getNode(Integer nodeId) {
        EntityTreeNode node = em.find(EntityTreeNode.class, nodeId);

        if (node == null) {
            return null;
        }

        return new ExtendedEntityTreeNode(node, false);
    }

    ExtendedEntityTreeNode getNode(List<String> containerTypes, Integer nodeId) {
        try {
            Object[] r = (Object[]) em.createNativeQuery("select node.*,  COALESCE(child.count, 0) = 0 as leaf_node " +
                    "FROM entity_tree_node node " +
                    "LEFT JOIN ( " +
                    "    SELECT parent_id, count(id) FROM entity_tree_node where parent_id = :nodeId and entity_type in (:containerTypes) group by parent_id" +
                    ") child ON (child.parent_id = node.id) " +
                    "WHERE node.id = :nodeId"
            )
                    .setParameter("nodeId", nodeId)
                    .setParameter("containerTypes", containerTypes)
                    .getSingleResult();

            return new ExtendedEntityTreeNode(
                    (Integer) r[0], (Integer) r[1], (Integer) r[2], (Integer) r[3], (Integer) r[4], (String) r[5], (Integer) r[6], (Boolean) r[7]
            );
        } catch (NoResultException e) {
            return null;
        }
    }

    void deleteNode(Integer nodeId) {
        EntityTreeNode node = em.find(EntityTreeNode.class, nodeId);
        em.remove(node);
    }

    void deleteRootNode(Integer nodeId) {
        EntityTreeRoot root = em.find(EntityTreeRoot.class, nodeId);
        em.remove(root);
    }


    void deleteNodesWithRoot(Integer rootId) {
        em.createNativeQuery("DELETE FROM entity_tree_node WHERE root_id = :rootId")
                .setParameter("rootId", rootId)
                .executeUpdate();
    }

    List<ExtendedEntityTreeNode> lookupRoots(ReferenceType ownerReferenceType, Integer ownerReferenceId, String... entityTypes) {
        return extractListOfExtendedEntityTreeNodes(
                em.createNativeQuery(
                "SELECT n.*, (n.left_edge + 1 = n.right_edge) as leaf_node " +
                        "FROM entity_tree_node n JOIN entity_tree_root r ON r.id = n.id " +
                        "WHERE r.owner_reference_type = :ownerReferenceType AND r.owner_reference_id = :ownerReferenceId AND n.entity_type in (:entityTypes) " +
                        "ORDER by n.id ASC"
                )
                        .setParameter("ownerReferenceType", ownerReferenceType.name())
                        .setParameter("ownerReferenceId", ownerReferenceId)
                        .setParameter("entityTypes", Arrays.asList(entityTypes))
        );
    }

    List<ExtendedEntityTreeNode> lookupRoots(ReferenceType ownerReferenceType, Integer ownerReferenceId, List<String> containerTypes, String... entityTypes) {
        return extractListOfExtendedEntityTreeNodes(
                em.createNativeQuery(
                "SELECT node.*, COALESCE(child.count, 0) = 0 as leaf_node " +
                        "FROM entity_tree_node node " +
                        "  JOIN entity_tree_root root ON root.id = node.id " +
                        "  LEFT JOIN ( " +
                        "    select parent_id, count(id) count from entity_tree_node where entity_type in (:containerTypes) group by parent_id" +
                        "    ) child on (node.id = child.parent_id) " +
                        "WHERE root.owner_reference_type = :ownerReferenceType AND root.owner_reference_id = :ownerReferenceId AND node.entity_type in (:entityTypes) " +
                        "ORDER BY node.id ASC"
                )
                        .setParameter("ownerReferenceType", ownerReferenceType.name())
                        .setParameter("ownerReferenceId", ownerReferenceId)
                        .setParameter("containerTypes", containerTypes)
                        .setParameter("entityTypes", Arrays.asList(entityTypes))
        );
    }

    List<ExtendedEntityTreeNode> lookupNodeChildren(Integer parentNodeId) {
        return extractListOfExtendedEntityTreeNodes(
                em.createNativeQuery(
                "SELECT  n.*, (n.left_edge + 1 = n.right_edge) as leaf_node FROM entity_tree_node n WHERE n.parent_id = :parentNodeId " +
                        "ORDER BY n.id ASC"
                )
                        .setParameter("parentNodeId", parentNodeId)

        );
    }

    List<ExtendedEntityTreeNode> lookupNodeChildren(List<String> containerTypes, Integer parentNodeId) {
        return extractListOfExtendedEntityTreeNodes(
                em.createNativeQuery(
                "SELECT  node.*, COALESCE(child.count, 0) = 0 as leaf_node  " +
                        "FROM entity_tree_node node " +
                        "  LEFT JOIN LATERAL ( " +
                        "    select parent_id, count(id) count from entity_tree_node where parent_id = node.id and entity_type in (:containerTypes) group by parent_id" +
                        "    ) child on (node.id = child.parent_id) " +
                        "WHERE node.parent_id = :parentNodeId " +
                        "ORDER BY node.id ASC"
                )
                        .setParameter("containerTypes", containerTypes)
                        .setParameter("parentNodeId", parentNodeId)
        );
    }

    List<ExtendedEntityTreeNode> lookupNodesByEntity(Integer rootNodeId, String entityType, Integer entityId) {
        return extractListOfExtendedEntityTreeNodes(
                em.createNativeQuery(
                "SELECT  n.*, (n.left_edge + 1 = n.right_edge) as leaf_node FROM entity_tree_node n " +
                        "WHERE n.root_id = :rootNodeId and n.entity_type = :entityType and n.entity_id = :entityId"
                )
                        .setParameter("rootNodeId", rootNodeId)
                        .setParameter("entityType", entityType)
                        .setParameter("entityId", entityId)
        );
    }

    List<ExtendedEntityTreeNode> lookupNodesByEntity(List<String> containerTypes, Integer rootNodeId, String entityType, Integer entityId) {
        return extractListOfExtendedEntityTreeNodes(
                em.createNativeQuery(
                "SELECT  n.*, COALESCE(child.count, 0) = 0 as leaf_node  " +
                        "FROM entity_tree_node n " +
                        "  LEFT JOIN ( " +
                        "    select parent_id, count(id) count from entity_tree_node where root_id = :rootNodeId and entity_type in (:containerTypes) group by parent_id" +
                        "    ) child on (n.id = child.parent_id) " +
                        "WHERE n.root_id = :rootNodeId and n.entity_type = :entityType and n.entity_id = :entityId"
                )
                        .setParameter("containerTypes", containerTypes)
                        .setParameter("rootNodeId", rootNodeId)
                        .setParameter("entityType", entityType)
                        .setParameter("entityId", entityId)
        );
    }

    List<ExtendedEntityTreeNode> lookupNodesBetween(Integer rootNodeId, Integer leftEdge, Integer rightEdge) {
        return extractListOfExtendedEntityTreeNodes(
                em.createNativeQuery(
                        "SELECT  n.*, (n.left_edge + 1 = n.right_edge) as leaf_node FROM entity_tree_node n " +
                                "WHERE n.root_id = :rootNodeId AND n.left_edge >= :leftEdge AND n.right_edge <= :rightEdge"
                )
                        .setParameter("rootNodeId", rootNodeId)
                        .setParameter("leftEdge", leftEdge)
                        .setParameter("rightEdge", rightEdge)
        );
    }
 
    List<ExtendedEntityTreeNode> lookupNodesBetween(Integer rootNodeId, List<String> containerTypes, Integer leftEdge, Integer rightEdge) {
        return extractListOfExtendedEntityTreeNodes(
                em.createNativeQuery(
                        "SELECT  n.*, COALESCE(child.count, 0) = 0 as leaf_node  " +
                                "FROM entity_tree_node n " +
                                "  LEFT JOIN ( " +
                                "    select parent_id, count(id) as count from entity_tree_node where root_id = :rootNodeId and entity_type in (:containerTypes) group by parent_id" +
                                "    ) child on (n.id = child.parent_id) " +
                                "WHERE n.root_id = :rootNodeId AND n.left_edge >= :leftEdge and n.right_edge <= :rightEdge"
                )
                        .setParameter("rootNodeId", rootNodeId)
                        .setParameter("containerTypes", containerTypes)
                        .setParameter("leftEdge", leftEdge)
                        .setParameter("rightEdge", rightEdge)
        );
    }

    List<Integer> listPathToRoot(Integer edge) {
        return (List<Integer>) em.createNativeQuery("SELECT n.id FROM entity_tree_node n WHERE n.left_edge < :edge and n.right_edge > :edge order by n.left_edge ASC")
                .setParameter("edge", edge)
                .getResultList().stream().map( o -> (Integer)o ).collect(Collectors.toList());
    }

    void dump() {
        LOGGER.info("--");
        em.createNativeQuery("SELECT * FROM entity_tree_node ORDER BY id DESC", EntityTreeNode.class)
                .getResultList().stream().forEach(n -> {
            EntityTreeNode node = (EntityTreeNode) n;
            LOGGER.info("{}:{} - L: {} R: {} - {}:{}", node.getRootId(), node.getId(), node.getLeftEdge(), node.getRightEdge(), node.getEntityType(), node.getEntityId());
        });
        LOGGER.info("--");
    }
    
    void dumpRootsCreationScript() {
        String nl = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();
        sb.append("INSERT INTO entity_tree_root(id, entity_owner_reference, entity_owner_id) VALUES ");
        sb.append(nl);
        
        List result = em.createNativeQuery("SELECT * FROM entity_tree_root ORDER BY id", EntityTreeRoot.class)
                .getResultList();
        
        for(int i = 0; i < result.size(); i++)
        {
            if (i != 0) {
                sb.append(",");
            }            
            
            sb.append(nl);
            EntityTreeRoot node = (EntityTreeRoot) result.get(i);
            sb.append("(" + node.getId() + ", \"" + node.getOwnerReferenceType() + "\", " + node.getOwnerReferenceId() +")");
        }
        
        LOGGER.info("--");
        LOGGER.info(sb.toString());
        LOGGER.info("--");
    }

    void dumpNodesCreationScript() {        
        String nl = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();
        sb.append("INSERT INTO entity_tree_node(id, root_id, parent_id, left_edge, right_edge, entity_type, entity_id) VALUES ");
        sb.append(nl);
        
        List result = em.createNativeQuery("SELECT * FROM entity_tree_node ORDER BY id", EntityTreeNode.class)
                .getResultList();
        
        for(int i = 0; i < result.size(); i++)
        {
            if (i != 0) {
                sb.append(",");
            }            
            
            sb.append(nl);
            EntityTreeNode node = (EntityTreeNode) result.get(i);
            sb.append("(" + node.getId() + "," + node.getRootId() + "," + 
                            node.getParentNodeId() + ", " + node.getLeftEdge() + ", " + node.getRightEdge() +
                            ", \"" + node.getEntityType() + "\"" + ", " + node.getEntityId() + ")");
        }
        
        LOGGER.info("--");
        LOGGER.info(sb.toString());
        LOGGER.info("--");
    }
    
    
    private List<ExtendedEntityTreeNode> extractListOfExtendedEntityTreeNodes(Query query) {
        return (List<ExtendedEntityTreeNode>) query.getResultList().stream().map(o -> {
            Object[] r = (Object[]) o;
            return new ExtendedEntityTreeNode(
                    (Integer) r[0], (Integer) r[1], (Integer) r[2], (Integer) r[3], (Integer) r[4], (String) r[5], (Integer) r[6], (Boolean) r[7]
            );
        }).collect(Collectors.toList());
    }
}
