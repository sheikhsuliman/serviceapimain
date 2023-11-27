package com.siryus.swisscon.api.catalog;

import org.junit.jupiter.api.Test;

import static com.siryus.swisscon.api.catalog.SnpHelper.compare;
import static com.siryus.swisscon.api.catalog.SnpHelper.getSnpNumberLevel;
import static com.siryus.swisscon.api.catalog.SnpHelper.getSnpParts;
import static com.siryus.swisscon.api.catalog.SnpHelper.parentSnps;
import static org.junit.jupiter.api.Assertions.*;

class SnpHelperTest {
    private static final String VALID_SNP = "10.10.10";
    private static final String VALID_SIBLING_SNP = "10.10.20";

    private static final String INVALID_SNP_NON_NUMBER = "10.A.10";

    private static final String INVALID_SNP_TOO_LONG = "10.10.10.10.10.10";

    private static final String NORMAL_SNP = "10.10.10";
    private static final String NOT_NORMAL_SNP = "010.00010.10";
    private static final String SNP_WITH_0 = "10.0.10";

    private static final String ROOT_SNP = "100";
    private static final String GROUP_SNP = "100.200";
    private static final String VARIANT_SNP = "100.200.300";
    private static final String SECTION_SNP = "100.200.300.400";
    private static final String TASK_SNP = "100.200.300.400.500";

    @Test
    void Given_validSnp_When_isValidSnp_Then_returnTrue() {
        assertTrue(SnpHelper.isValidSnp(VALID_SNP));
    }

    @Test
    void Given_invalidSnp_When_isValidSnp_Then_returnFalse() {
        assertFalse(SnpHelper.isValidSnp(INVALID_SNP_NON_NUMBER));
        assertFalse(SnpHelper.isValidSnp(INVALID_SNP_TOO_LONG));
    }

    @Test
    void Given_normalSnp_When_normalize_Then_returnSameValue() {
        assertEquals(NORMAL_SNP, SnpHelper.normalize(NORMAL_SNP));
    }

    @Test
    void Given_notNormalSnp_When_normalize_Then_returnNormalValue() {
        assertEquals(NORMAL_SNP, SnpHelper.normalize(NOT_NORMAL_SNP));
    }

    @Test
    void Given_snpWith0_When_normalize_Then_preserve0() {
        assertEquals(SNP_WITH_0, SnpHelper.normalize(SNP_WITH_0));
    }

    @Test
    void Given_rootSnp_When_parentSnp_Then_returnNull() {
        assertNull(SnpHelper.parentSnp(ROOT_SNP));
    }

    @Test
    void Given_rootChildSnp_When_parentSnp_Then_returnRootSnp() {
        assertEquals(ROOT_SNP, SnpHelper.parentSnp(GROUP_SNP));
    }

    @Test
    void Given_sameSnp_When_compare_Then_return0() {
        assertEquals(0, compare(NORMAL_SNP, NORMAL_SNP));
    }

    @Test
    void Given_parentAndChild_When_compare_Then_returnParentLessThenChild() {
        assertTrue(compare(ROOT_SNP, GROUP_SNP) < 0);

        assertTrue(compare(GROUP_SNP, ROOT_SNP) > 0);
    }

    @Test
    void Given_siblings_When_compare_Then_returnSiblingWithSmallestLastNumberIsLess() {
        assertTrue(compare(VALID_SNP, VALID_SIBLING_SNP) < 0);
        assertTrue(compare(VALID_SIBLING_SNP, VALID_SNP) > 0);
    }

    @Test
    void Given_emptySnp_When_compare_Then_returnItsLessThanAnyOther() {
        assertTrue(compare("", ROOT_SNP) < 0);
        assertTrue(compare(ROOT_SNP, "") > 0);
    }

    @Test
    void Given_differentSnps_When_parentSnps_Then_returnCorrectParents() {
        assertEquals("[" + SECTION_SNP + ", " + VARIANT_SNP + ", " +  GROUP_SNP + ", " + ROOT_SNP + "]", parentSnps(TASK_SNP).toString());
        assertEquals("[" + VARIANT_SNP + ", " +  GROUP_SNP + ", " + ROOT_SNP + "]", parentSnps(SECTION_SNP).toString());
        assertEquals("[" + GROUP_SNP + ", " + ROOT_SNP + "]", parentSnps(VARIANT_SNP).toString());
        assertEquals("[" + ROOT_SNP + "]", parentSnps(GROUP_SNP).toString());
        assertEquals("[]", parentSnps(ROOT_SNP).toString());
    }

    @Test
    void Given_different_snps_When_snpNumberLevel_Then_returnCorrectLevels() {
        assertEquals(4, getSnpNumberLevel(TASK_SNP));
        assertEquals(3, getSnpNumberLevel(SECTION_SNP));
        assertEquals(2, getSnpNumberLevel(VARIANT_SNP));
        assertEquals(1, getSnpNumberLevel(GROUP_SNP));
        assertEquals(0, getSnpNumberLevel(ROOT_SNP));
    }

    @Test
    void Given_different_snps_When_snpParts_Then_returnCorrectSnpParts() {
        assertEquals("[100, 200, 300, 400, 500]", getSnpParts(TASK_SNP).toString());
        assertEquals("[100, 200, 300, 400]", getSnpParts(SECTION_SNP).toString());
        assertEquals("[100, 200, 300]", getSnpParts(VARIANT_SNP).toString());
        assertEquals("[100, 200]", getSnpParts(GROUP_SNP).toString());
        assertEquals("[100]", getSnpParts(ROOT_SNP).toString());
    }

}
