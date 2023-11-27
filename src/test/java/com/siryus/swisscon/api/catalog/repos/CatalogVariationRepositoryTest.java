package com.siryus.swisscon.api.catalog.repos;

import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.general.unit.Unit;
import com.siryus.swisscon.api.general.unit.UnitService;
import com.siryus.swisscon.api.catalog.Constants;
import com.siryus.swisscon.api.catalog.SnpHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogVariationRepositoryTest  extends AbstractMvcTestBase {
    private static final String LEAF_SNP = "100.100.100.100.100";
    private static final String OTHER_LEAF_SNP = "100.100.100.100.200";

    private static final String TASK_1 = "Task 1";
    private static final String VARIATION_1 = "Variation 1";
    private static final String VARIATION_2 = "Variation 2";
    private static final String VARIATION_3 = "Variation 3";
    private static final String M_2 = "m2";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CatalogNodeRepository catalogNodeRepository;

    @Autowired
    private CatalogVariationRepository variationRepository;

    @Autowired
    private UnitService unitService;

    @Override
    protected boolean doMockLogin() {
        return false;
    }

    @Override
    protected Integer mockLocalLoginUserId() {
        return 1;
    }
    
    @BeforeEach
    void doBeforeEach() {
        deleteAllVariations();
        deleteAllNodes();
    }

    @Test
    void Given_repositoryEmpty_When_listVariations_Then_returnEmptyList() {
        List<CatalogVariationEntity> variations = variationRepository.listLatestVariations(Constants.GLOBAL_CATALOG_COMPANY_ID, 1);

        assertTrue(variations.isEmpty());
    }

    @Test
    void Given_variationsExistForNode_When_listVariations_Then_returnAllVariationsForThisNode() {
        CatalogNodeEntity node = newNode(LEAF_SNP, LEAF_SNP);
        newVariation(node, 1, TASK_1, VARIATION_1, unitService.findBySymbolName(M_2));
        newVariation(node, 2, TASK_1, VARIATION_2, unitService.findBySymbolName(M_2));

        CatalogNodeEntity otherNode = newNode(OTHER_LEAF_SNP, OTHER_LEAF_SNP);
        newVariation(otherNode, 1, TASK_1, VARIATION_1, unitService.findBySymbolName(M_2));

        List<CatalogVariationEntity> variations = variationRepository.listLatestVariations(Constants.GLOBAL_CATALOG_COMPANY_ID, node.getId());

        assertEquals(2, variations.size());

        assertEquals(TASK_1, variations.get(0).getTaskName());
        assertEquals(TASK_1, variations.get(1).getTaskName());

        assertEquals(VARIATION_1, variations.get(0).getTaskVariation());
        assertEquals(VARIATION_2, variations.get(1).getTaskVariation());

        assertEquals(M_2, variations.get(0).getUnit().getSymbol());

        assertTrue(variations.get(1).getVariationNumber() > variations.get(0).getVariationNumber());
    }

    @Test
    void Given_moreThanOneVersionExistsForSNP_When_findLatestInSnps_Then_returnLatestVersion() {
        CatalogNodeEntity node = newNode(LEAF_SNP, LEAF_SNP);
        newVariation(node, 1, TASK_1, VARIATION_1, unitService.findBySymbolName(M_2));
        newVariation(node, 2, TASK_1, VARIATION_2, unitService.findBySymbolName(M_2));

        CatalogNodeEntity newerNode = newNode(LEAF_SNP, LEAF_SNP);
        CatalogVariationEntity variationEntity = newVariation(newerNode, 1, TASK_1, VARIATION_3, unitService.findBySymbolName(M_2));

        List<Integer> variationIDs = variationRepository.findLatestInSnps(Collections.singletonList(LEAF_SNP), Constants.GLOBAL_CATALOG_COMPANY_ID);

        assertEquals(2, variationIDs.size());
        assertEquals(variationEntity.getId(), variationIDs.get(0));
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

    private CatalogVariationEntity newVariation(CatalogNodeEntity catalogNode, Integer variationNumber, String description, String variation, Unit unit) {
        return variationRepository.save(
                CatalogVariationEntity.builder()
                        .catalogNodeId(catalogNode.getId())
                        .snp(catalogNode.getSnp())
                        .variationNumber(variationNumber)
                        .taskName(description)
                        .taskVariation(variation)
                        .unit(unit)
                        .companyId(Constants.GLOBAL_CATALOG_COMPANY_ID)
                        .createdBy(1)
                        .createdDate(LocalDateTime.now())
                .build()
        );
    }

    private List<CatalogNodeEntity> nodes = new ArrayList<>();
    private CatalogNodeEntity register(CatalogNodeEntity newNode) {
        nodes.add(newNode);
        return newNode;
    }

    private List<CatalogVariationEntity> variations = new ArrayList<>();
    private CatalogVariationEntity register(CatalogVariationEntity newVariation) {
        variations.add(newVariation);
        return newVariation;
    }

    private void deleteAllNodes() {
        catalogNodeRepository.deleteAll(nodes);
        nodes.clear();
    }

    private void deleteAllVariations() {
        variationRepository.deleteAll(variations);
        variations.clear();
    }
}
