package com.siryus.swisscon.api.catalog.repos;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.catalog.Constants;
import com.siryus.swisscon.api.catalog.SnpHelper;
import com.siryus.swisscon.api.catalog.dto.SnpAndId;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogNodeRepositoryTest extends AbstractMvcTestBase {
    private static final String TRADE_1_NAME = "Flat Roof";
    private static final String TRADE_1_SNP = "100";
    private static final String TRADE_2_NAME = "No Roof";
    private static final String TRADE_2_SNP = "200";
    private static final String GROUP_1_NAME = "Waterproofing membranes";
    private static final String GROUP_1_SNP = TRADE_1_SNP + ".100";

    @Autowired
    private CatalogNodeRepository catalogNodeRepository;

    @Autowired
    private CatalogVariationRepository variationRepository;

    @Override
    protected boolean doMockLogin() {
        return false;
    }

    @Override
    protected Integer mockLocalLoginUserId() {
        return 1;
    }

    @Override
    protected Flyway customizeFlyWay(Flyway flyway) {
        return Flyway.configure().configuration(flyway.getConfiguration())
                .locations(
                        "classpath:db/migrations/common",
                        "classpath:db/migrations/data/common",
                        "classpath:db/migrations/data/test-minimum"
                )
                .load();
    }

    @BeforeEach
    void doBeforeEach() {
        variationRepository.deleteAll();
        catalogNodeRepository.deleteAll();
    }

    @Test
    void Given_oneRoot_When_listLatestIdsOfRootNodes_Then_returnRootNodeId() {
        CatalogNodeEntity node100 = newNode(TRADE_1_SNP, TRADE_1_NAME);

        List<SnpAndId> rootNodeIDs = catalogNodeRepository.listLatestIdsOfRootNodesForCompany(Constants.GLOBAL_CATALOG_COMPANY_ID);

        assertNotNull(rootNodeIDs);
        assertEquals(1, rootNodeIDs.size());
        assertEquals(node100.getId(), rootNodeIDs.get(0).getId());
    }

    @Test
    void Given_moreThanOneVersionOfRootExists_When_listLatestIdsOfRootNodes_Then_returnLatest() {
        newNode(TRADE_1_SNP, TRADE_1_NAME);
        CatalogNodeEntity newVersion = newNode(TRADE_1_SNP, TRADE_1_NAME.toLowerCase());
        List<SnpAndId> rootNodeIDs = catalogNodeRepository.listLatestIdsOfRootNodesForCompany(Constants.GLOBAL_CATALOG_COMPANY_ID);

        assertNotNull(rootNodeIDs);
        assertEquals(1, rootNodeIDs.size());
        assertEquals(newVersion.getId(), rootNodeIDs.get(0).getId());
    }

    @Test
    void Given_oneChild_When_listLatestIdsOfChildNodes_Then_returnOneNode() {
        newNode(TRADE_1_SNP, TRADE_1_NAME);
        CatalogNodeEntity node100_200 = newNode(GROUP_1_SNP, GROUP_1_NAME);

        List<SnpAndId> rootNodeIDs = catalogNodeRepository.listLatestIdsOfChildNodesForCompany(Constants.GLOBAL_CATALOG_COMPANY_ID, TRADE_1_SNP);

        assertNotNull(rootNodeIDs);
        assertEquals(1, rootNodeIDs.size());
        assertEquals(node100_200.getId(), rootNodeIDs.get(0).getId());
    }

    @Test
    void Given_modeThenOneVersionOfChild_When_listLatestIdsOfChildNodes_Then_returnLatestVersion() {
        newNode(TRADE_1_SNP, TRADE_1_NAME);
        newNode(GROUP_1_SNP, GROUP_1_NAME);
        CatalogNodeEntity node100_200 = newNode(GROUP_1_SNP, GROUP_1_NAME.toLowerCase());

        List<SnpAndId> rootNodeIDs = catalogNodeRepository.listLatestIdsOfChildNodesForCompany(Constants.GLOBAL_CATALOG_COMPANY_ID, TRADE_1_SNP);

        assertNotNull(rootNodeIDs);
        assertEquals(1, rootNodeIDs.size());
        assertEquals(node100_200.getId(), rootNodeIDs.get(0).getId());
    }

    @Test
    void Given_nodeDoesNotExist_When_findLatestWithSnp_Then_returnEmpty() {
        Optional<CatalogNodeEntity> nodeEntity = catalogNodeRepository.findLatestWithSnp(TRADE_1_SNP);

        assertTrue(nodeEntity.isEmpty());
    }

    @Test
    void Given_moreThanOneVersionOfNodeExist_When_findLatestWithSnp_Then_returnLatest() {
        newNode(TRADE_1_SNP, TRADE_1_NAME);
        CatalogNodeEntity newVersion = newNode(TRADE_1_SNP, TRADE_1_NAME.toLowerCase());

        Optional<CatalogNodeEntity> nodeEntity = catalogNodeRepository.findLatestWithSnp(TRADE_1_SNP);

        assertTrue(nodeEntity.isPresent());
        assertEquals(newVersion.getId(), nodeEntity.get().getId());
    }

    @Test
    void Given_someRootNodesAreDisabled_When_findByIdInAndDisabledIsNullRootNodes_Then_returnOnlyNotDisabled() {
        newNode(TRADE_1_SNP, TRADE_1_NAME);
        newNode(TRADE_2_SNP, TRADE_2_NAME);
        newNode(TRADE_2_SNP, TRADE_2_NAME, true);

        List<CatalogNodeEntity> rootEntities = catalogNodeRepository.findByIdInAndDisabledIsNull(
                catalogNodeRepository.listLatestIdsOfRootNodesForCompany(Constants.GLOBAL_CATALOG_COMPANY_ID)
                .stream().map(SnpAndId::getId).collect(Collectors.toList())
        );

        assertNotNull(rootEntities);
        assertEquals(1, rootEntities.size());
        assertEquals(TRADE_1_SNP, rootEntities.get(0).getSnp());
    }

    @Test
    void Given_allNodes_When_listAllLatest_Then_returnAlLLatestNodes() {
        newNode(TRADE_1_SNP, TRADE_1_NAME);
        newNode(TRADE_2_SNP, TRADE_2_NAME);
        newNode(TRADE_2_SNP, TRADE_2_NAME, true);
        newNode(GROUP_1_SNP, GROUP_1_NAME);

        List<Integer> allLatestIds = catalogNodeRepository.listAllLatest(Constants.GLOBAL_CATALOG_COMPANY_ID);
        List<CatalogNodeEntity> allLatestNodes = catalogNodeRepository.findAllById(allLatestIds);

        assertEquals(TRADE_1_SNP, allLatestNodes.get(0).getSnp());
        assertEquals(TRADE_2_SNP, allLatestNodes.get(1).getSnp());
        assertNotNull(allLatestNodes.get(1).getDisabled());
        assertEquals(GROUP_1_SNP, allLatestNodes.get(2).getSnp());
    }

    private CatalogNodeEntity newNode(String snp, String name) {
        return newNode(snp, name, false);
    }
    private CatalogNodeEntity newNode(String snp, String name, boolean deleted) {
        return catalogNodeRepository.save(
                CatalogNodeEntity.builder()
                        .snp(snp)
                        .parentSnp(SnpHelper.parentSnp(snp))
                        .name(name)
                        .companyId(Constants.GLOBAL_CATALOG_COMPANY_ID)
                        .createdBy(1)
                        .createdDate(LocalDateTime.now())
                        .disabled(deleted ? LocalDateTime.now() : null)
                    .build()
        );
    }
}

