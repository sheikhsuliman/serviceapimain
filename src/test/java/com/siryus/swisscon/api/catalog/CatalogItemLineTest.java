package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.general.unit.Unit;
import com.siryus.swisscon.api.catalog.csvreader.CatalogItemLine;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CatalogItemLineTest {

    private static final String DOT = SnpHelper.SNP_SEPARATOR;

    private static final String ROOT = "100";
    private static final String CHAPTER = "200";
    private static final String VARIANT = "300";
    private static final String SECTION = "400";
    private static final String TASK = "500";
    private static final String SUBTASK = "1";
    private static final String TRADE_NAME = "trade";
    private static final String CHAPTER_NAME = "chapter";
    private static final String VARIANT_NAME = "variant";
    private static final String SECTION_NAME = "section";
    private static final String TASK_NAME = "task";
    private static final String SUB_TASK_NAME = "sub task";
    private static final String UNIT = "m2";

    private static final String COMPANY_ACTIVE = "1";
    private static final String COMPANY_CHECKLIST = "item_a\nitem_b\nitem_c";
    private static final String COMPANY_PRICE = "25.00";
    private static final String COMPANY_TASK_NAME = "company task";
    private static final String COMPANY_SUB_TASK_NAME = "company sub task";
    private static final String COMPANY_UNIT = "m3";
    private static final Integer COMPANY_ID = 1;

    private static CatalogItemLine GLOBAL_VARIATION_LINE;
    private static CatalogItemLine COMPANY_VARIATION_LINE;

    @BeforeEach
    public void initLine() {
        GLOBAL_VARIATION_LINE = CatalogItemLine.builder()
                .trade(ROOT).chapter(CHAPTER).variant(VARIANT).section(SECTION).task(TASK).subTask(SUBTASK)
                .tradeName(TRADE_NAME).chapterName(CHAPTER_NAME)
                .variantName(VARIANT_NAME).sectionName(SECTION_NAME)
                .taskName(TASK_NAME).subTaskName(SUB_TASK_NAME)
                .unit(UNIT).build();
        COMPANY_VARIATION_LINE = GLOBAL_VARIATION_LINE.toBuilder().companyActive(COMPANY_ACTIVE)
                .companyChecklist(COMPANY_CHECKLIST).companyPrice(COMPANY_PRICE).companyTaskName(COMPANY_TASK_NAME)
                .companySubTaskName(COMPANY_SUB_TASK_NAME).companyUnit(COMPANY_UNIT).build();
    }

    @Test
    public void Given_line_When_getSnpNumber_Then_returnCorrectSnp() {
        String expectedSnp = String.join(DOT, Arrays.asList(ROOT, CHAPTER, VARIANT, SECTION, TASK));
        assertEquals(expectedSnp, GLOBAL_VARIATION_LINE.getSnpNumber());
    }

    @Test
    public void Given_lineSnpWithTrailingZeros_When_getSnpNumber_Then_returnWithoutZeros() {
        CatalogItemLine line = CatalogItemLine.builder().trade("0100").chapter("000200").build();
        assertEquals("100.200", line.getSnpNumber());
    }

    @Test
    public void Given_line_When_isNodeAndNotLeaf_Then_returnCorrectBoolean() {
        assertFalse(GLOBAL_VARIATION_LINE.isNodeAndNotLeaf());

        GLOBAL_VARIATION_LINE.setSubTask(null);
        assertFalse(GLOBAL_VARIATION_LINE.isNodeAndNotLeaf());

        GLOBAL_VARIATION_LINE.setTask(null);
        assertTrue(GLOBAL_VARIATION_LINE.isNodeAndNotLeaf());
    }

    @Test
    public void Given_variationLine_When_getSnpAndVariationNumber_Then_returnFullSnp() {
        String expectedSnp = String.join(DOT, Arrays.asList(ROOT, CHAPTER, VARIANT, SECTION, TASK, SUBTASK));
        assertEquals(expectedSnp, GLOBAL_VARIATION_LINE.getSnpAndVariationNumber());
    }

    @Test
    public void Given_lines_When_isRootLine_Then_returnCorrectBoolean() {
        assertFalse(GLOBAL_VARIATION_LINE.isRootLine());
        assertTrue(CatalogItemLine.builder().trade(ROOT).build().isRootLine());
    }

    @Test
    public void Given_variationLine_When_getNameAtLevel_Then_ReturnCorrectName() {
        assertEquals(TRADE_NAME, GLOBAL_VARIATION_LINE.getNameAtLevel(0));
        assertEquals(CHAPTER_NAME, GLOBAL_VARIATION_LINE.getNameAtLevel(1));
        assertEquals(VARIANT_NAME, GLOBAL_VARIATION_LINE.getNameAtLevel(2));
        assertEquals(SECTION_NAME, GLOBAL_VARIATION_LINE.getNameAtLevel(3));
        assertEquals(TASK_NAME, GLOBAL_VARIATION_LINE.getNameAtLevel(4));
    }

    @Test
    public void Given_variationLine_When_getSnpAtLevel_Then_ReturnCorrectSnp() {
        assertEquals(String.join(DOT, Arrays.asList(ROOT, CHAPTER, VARIANT, SECTION, TASK, SUBTASK)), GLOBAL_VARIATION_LINE.getSnpAtLevel(5));
        assertEquals(String.join(DOT, Arrays.asList(ROOT, CHAPTER, VARIANT, SECTION, TASK)), GLOBAL_VARIATION_LINE.getSnpAtLevel(4));
        assertEquals(String.join(DOT, Arrays.asList(ROOT, CHAPTER, VARIANT, SECTION)), GLOBAL_VARIATION_LINE.getSnpAtLevel(3));
        assertEquals(String.join(DOT, Arrays.asList(ROOT, CHAPTER, VARIANT)), GLOBAL_VARIATION_LINE.getSnpAtLevel(2));
        assertEquals(String.join(DOT, Arrays.asList(ROOT, CHAPTER)), GLOBAL_VARIATION_LINE.getSnpAtLevel(1));
        assertEquals(String.join(DOT, Collections.singletonList(ROOT)), GLOBAL_VARIATION_LINE.getSnpAtLevel(0));
    }

    @Test
    public void Given_variationLine_When_toGlobalVariation_Then_convertToVariationCorrectly() {
        CatalogVariationEntity variation = GLOBAL_VARIATION_LINE.toVariation(Constants.GLOBAL_CATALOG_COMPANY_ID, Unit.builder().id(1).symbol(UNIT).build());
        assertEquals(String.join(DOT, Arrays.asList(ROOT, CHAPTER, VARIANT, SECTION, TASK)),variation.getSnp());
        assertEquals(1, variation.getUnit().getId());
        assertEquals(Integer.valueOf(SUBTASK), variation.getVariationNumber());
        assertEquals(TASK_NAME, variation.getTaskName());
        assertEquals(SUB_TASK_NAME, variation.getTaskVariation());
    }

    @Test
    public void Given_variationLine_When_toCompanyVariation_Then_convertToVariationCorrectly() {
        CatalogVariationEntity variation = COMPANY_VARIATION_LINE.toVariation(COMPANY_ID, Unit.builder().id(2).symbol(COMPANY_UNIT).build());
        assertEquals(String.join(DOT, Arrays.asList(ROOT, CHAPTER, VARIANT, SECTION, TASK)),variation.getSnp());
        assertEquals(2, variation.getUnit().getId());
        assertEquals(Integer.valueOf(SUBTASK), variation.getVariationNumber());
        assertEquals(COMPANY_TASK_NAME, variation.getTaskName());
        assertEquals(COMPANY_SUB_TASK_NAME, variation.getTaskVariation());
    }

    @Test
    public void Given_fields_When_toGlobalLine_Then_convertToLineCorrectly() {
        List<String> fields = Arrays.asList(ROOT, CHAPTER, VARIANT, SECTION, TASK, SUBTASK,
                TRADE_NAME, CHAPTER_NAME, VARIANT_NAME, SECTION_NAME, TASK_NAME, SUB_TASK_NAME,
                UNIT);
        CatalogItemLine lineFromFields = CatalogItemLine.toLine(fields);
        assertEqualsCatalogItemLine(GLOBAL_VARIATION_LINE, lineFromFields);
    }

    @Test
    public void Given_fields_When_toCompanyLine_Then_convertToLineCorrectly() {
        List<String> fields = Arrays.asList(ROOT, CHAPTER, VARIANT, SECTION, TASK, SUBTASK,
                TRADE_NAME, CHAPTER_NAME, VARIANT_NAME, SECTION_NAME, TASK_NAME, SUB_TASK_NAME,
                UNIT, COMPANY_ACTIVE, COMPANY_CHECKLIST, COMPANY_PRICE, COMPANY_TASK_NAME, COMPANY_SUB_TASK_NAME, COMPANY_UNIT);
        CatalogItemLine lineFromFields = CatalogItemLine.toLine(fields);
        assertEqualsCatalogItemLine(COMPANY_VARIATION_LINE, lineFromFields);
    }

    @Test
    public void Given_globalVariationAndParents_When_toLine_Then_convertToLineCorrectly() {
        Map<String, String> parentNames = createParentNames();

        CatalogVariationEntity variation = createGlobalCatalogEntity();

        CatalogItemLine catalogItemLine = CatalogItemLine.toLine(parentNames, variation, null);
        assertEqualsCatalogItemLine(GLOBAL_VARIATION_LINE, catalogItemLine);
    }

    @Test
    public void Given_companyVariationAndParents_When_toLine_Then_convertToLineCorrectly() {
        Map<String, String> parentNames = createParentNames();

        CatalogVariationEntity globalVariation = createGlobalCatalogEntity();
        CatalogVariationEntity companyVariation = createCompanyCatalogEntity();

        CatalogItemLine catalogItemLine = CatalogItemLine.toLine(parentNames, globalVariation, companyVariation);
        assertEqualsCatalogItemLine(COMPANY_VARIATION_LINE, catalogItemLine);
    }

    @Test
    public void Given_companyLine_When_isCompanyLine_Then_giveCorrectResult() {
        assertFalse(GLOBAL_VARIATION_LINE.isCompanyItemLine());
        assertTrue(COMPANY_VARIATION_LINE.isCompanyItemLine());
        assertTrue(CatalogItemLine.builder().companyActive("1").build().isCompanyItemLine());
    }

    private Map<String, String> createParentNames() {
        Map<String,String> parentNames = new HashMap<>();
        parentNames.put(String.join(DOT, Arrays.asList(ROOT, CHAPTER, VARIANT, SECTION)), SECTION_NAME);
        parentNames.put(String.join(DOT, Arrays.asList(ROOT, CHAPTER, VARIANT)), VARIANT_NAME);
        parentNames.put(String.join(DOT, Arrays.asList(ROOT, CHAPTER)), CHAPTER_NAME);
        parentNames.put(String.join(DOT, Collections.singletonList(ROOT)), TRADE_NAME);
        return parentNames;
    }

    private CatalogVariationEntity createGlobalCatalogEntity() {
        return CatalogVariationEntity.builder()
                .snp(String.join(DOT, Arrays.asList(ROOT, CHAPTER, VARIANT, SECTION, TASK)))
                .variationNumber(Integer.valueOf(SUBTASK))
                .taskName(TASK_NAME)
                .taskVariation(SUB_TASK_NAME)
                .companyId(Constants.GLOBAL_CATALOG_COMPANY_ID)
                .catalogNodeId(null)
                .active(true)
                .unit(Unit.builder().symbol(UNIT).id(1).build())
                .build();
    }

    private CatalogVariationEntity createCompanyCatalogEntity() {
        return createGlobalCatalogEntity().toBuilder()
                .unit(Unit.builder().symbol(COMPANY_UNIT).id(2).build())
                .checkList(COMPANY_CHECKLIST)
                .price(new BigDecimal(COMPANY_PRICE))
                .taskName(COMPANY_TASK_NAME)
                .taskVariation(COMPANY_SUB_TASK_NAME)
                .build();
    }

    private void assertEqualsCatalogItemLine(CatalogItemLine expectedLine, CatalogItemLine lineFromFields) {
        assertEquals(expectedLine.getSnpNumber(), lineFromFields.getSnpNumber());
        assertEquals(expectedLine.getTradeName(), lineFromFields.getTradeName());
        assertEquals(expectedLine.getChapterName(), lineFromFields.getChapterName());
        assertEquals(expectedLine.getVariantName(), lineFromFields.getVariantName());
        assertEquals(expectedLine.getTaskName(), lineFromFields.getTaskName());
        assertEquals(expectedLine.getSubTaskName(), lineFromFields.getSubTaskName());
        assertEquals(expectedLine.getUnit(), lineFromFields.getUnit());

        assertEquals(expectedLine.getCompanyActive(), lineFromFields.getCompanyActive());
        assertEquals(expectedLine.getCompanyChecklist(), lineFromFields.getCompanyChecklist());
        assertEquals(expectedLine.getCompanyPrice(), lineFromFields.getCompanyPrice());
        assertEquals(expectedLine.getCompanyTaskName(), lineFromFields.getCompanyTaskName());
        assertEquals(expectedLine.getCompanySubTaskName(), lineFromFields.getCompanySubTaskName());
        assertEquals(expectedLine.getCompanyUnit(), lineFromFields.getCompanyUnit());
    }

}
