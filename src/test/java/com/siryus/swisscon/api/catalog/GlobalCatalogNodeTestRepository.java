package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.repos.CatalogNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface GlobalCatalogNodeTestRepository extends JpaRepository<CatalogNodeEntity, Integer> {

    @Modifying
    @Transactional
    @Query("UPDATE CatalogNodeEntity node SET node.disabled = null")
    void enableAll();

}
