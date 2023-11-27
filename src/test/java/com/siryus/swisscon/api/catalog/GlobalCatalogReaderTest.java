package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.dto.CatalogNodeDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogVariationDTO;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalCatalogReaderTest extends AbstractCatalogTestBase {

    private final GlobalCatalogReader reader = new GlobalCatalogReader(catalogNodeRepository, variationRepository,
            new CatalogValidator(catalogNodeRepository, variationRepository));

    @BeforeEach
    void doBeforeEach() {
        setupDefaultScenario();
    }

    @Test
    void When_listRoots_Then_returnListOfRoots() {
        List<CatalogNodeDTO> roots = reader.listRoots();

        assertNotNull(roots);
        assertEquals(ROOT_NODES_LVL_1.size(), roots.size());

        assertFalse(roots.get(0).isLeaf());
        assertEquals(ROOT_NODES_LVL_1.get(0).getName(), roots.get(0).getName());
        assertEquals(ROOT_NODES_LVL_1.get(0).getSnp(), roots.get(0).getSnp());

        assertTrue(roots.get(1).isLeaf());
        assertEquals(ROOT_NODES_LVL_1.get(1).getName(), roots.get(1).getName());
        assertEquals(ROOT_NODES_LVL_1.get(1).getSnp(), roots.get(1).getSnp());
    }

    @Test
    void Given_validParentSnp_When_listChildren_Then_returnListOfChildren() {
        List<CatalogNodeDTO> children = reader.listChildren(TRADE_1_SNP);

        assertNotNull(children);
        assertEquals(CHILDREN_NODES_LVL_2.size(), children.size());

        assertFalse(children.get(0).isLeaf());
        assertEquals(CHILDREN_NODES_LVL_2.get(0).getName(), children.get(0).getName());
        assertEquals(CHILDREN_NODES_LVL_2.get(0).getSnp(), children.get(0).getSnp());

        assertTrue(children.get(1).isLeaf());
        assertEquals(CHILDREN_NODES_LVL_2.get(1).getName(), children.get(1).getName());
        assertEquals(CHILDREN_NODES_LVL_2.get(1).getSnp(), children.get(1).getSnp());
    }

    @Test
    void Given_validParenSnpWithNoChildren_When_listChildren_Then_returnEmptyList() {
        List<CatalogNodeDTO> children = reader.listChildren(TRADE_2_SNP);

        assertNotNull(children);
        assertTrue(children.isEmpty());

    }

    @Test
    void Given_invalidParenSnpWithNoChildren_When_listChildren_Then_throw() {
        assertThrows( LocalizedResponseStatusException.class, () -> {
            reader.listChildren(INVALID_SNP);
        });
    }

    @Test
    void Given_parentIdIsLevel5_When_listChildren_Then_returnNodeWithVariations() {
        List<CatalogNodeDTO> children = reader.listChildren(MAIN_TASK_1_SNP);

        assertNotNull(children);
        assertEquals(1, children.size());

        List<CatalogVariationDTO> variations = children.get(0).getVariations();
        assertNotNull(variations);
        assertEquals(2, variations.size());

        assertEquals(VARIATIONS.get(0).getVariationNumber(), variations.get(0).getVariationNumber());
        assertEquals(VARIATIONS.get(1).getVariationNumber(), variations.get(1).getVariationNumber());

        assertEquals(VARIATIONS.get(0).getTaskName(), variations.get(0).getDescription());
        assertEquals(VARIATIONS.get(1).getTaskName(), variations.get(1).getDescription());
    }

    @Test
    void Given_leafSNP_When_expand_Then_returnAllSiblingsAncestorsAndAncestorsSiblings() {
        List<CatalogNodeDTO> expandedNodes = reader.expand(TASK_1_SNP);

        assertNotNull(expandedNodes);

        assertEquals(2, expandedNodes.size());

        List<CatalogNodeDTO> level2 = expandedNodes.get(0).getChildren();
        assertNotNull(level2);
        assertEquals(2, level2.size());

        List<CatalogNodeDTO> level3 =level2.get(0).getChildren();
        assertNotNull(level3);
        assertEquals(1, level3.size());

        List<CatalogNodeDTO> level4 =level3.get(0).getChildren();
        assertNotNull(level4);
        assertEquals(1, level4.size());

        List<CatalogNodeDTO> level5 =level4.get(0).getChildren();
        assertNotNull(level5);
        assertEquals(1, level5.size());

        List<CatalogVariationDTO> variations = level5.get(0).getVariations();
        assertNotNull(variations);
        assertEquals(2, variations.size());
    }
}
