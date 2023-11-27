package com.siryus.swisscon.api.util.entitytree.nestedsets;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "entity_tree_root")
class EntityTreeRoot {

    @Id
    private Integer id;

    @Column(name = "owner_reference_id")
    private Integer ownerReferenceId;

    @Column(name = "owner_reference_type")
    private String ownerReferenceType;

    public EntityTreeRoot() {
    }

    EntityTreeRoot(Integer id, String ownerReferenceType, Integer ownerReferenceId) {
        this.id = id;
        this.ownerReferenceId = ownerReferenceId;
        this.ownerReferenceType = ownerReferenceType;
    }

    Integer getId() {
        return id;
    }

    Integer getOwnerReferenceId() {
        return ownerReferenceId;
    }

    String getOwnerReferenceType() {
        return ownerReferenceType;
    }
}
