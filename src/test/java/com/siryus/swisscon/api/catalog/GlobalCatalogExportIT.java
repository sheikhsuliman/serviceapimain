package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.catalog.csvreader.CatalogItemLine;
import com.siryus.swisscon.api.util.error.TestErrorResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.siryus.swisscon.api.base.TestHelper.COMPANY_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_EMAIL;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_FIRST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_LAST_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GlobalCatalogExportIT extends AbstractMvcTestBase {

    private static RequestSpecification asCompanyOwner;

    private final GlobalCatalogNodeTestRepository nodeTestRepository;

    @Autowired
    public GlobalCatalogExportIT(GlobalCatalogNodeTestRepository nodeTestRepository) {
        this.nodeTestRepository = nodeTestRepository;
    }

    @BeforeAll
    public void init() throws IOException {
        testHelper
                .signUp(TestBuilder.testSignupDTO(COMPANY_NAME, PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME));
        asCompanyOwner = testHelper.login(PROJECT_OWNER_EMAIL);
        testHelper.catalogImport(asCompanyOwner, GlobalCatalogImportIT.readBytes("export_sample.csv"));
    }

    @BeforeEach
    public void reactiveImportedTestData() {
        nodeTestRepository.enableAll();
    }

    @Test
    public void Given_Variation_When_ExportTrades_Then_SuccessAndHeadersCorrect() {
        List<String> expectedLine = Arrays.asList("102", "0", "30", "31", "100", "1",
                "Trade 102", "Chapter 0", "Variant 30", "Section 31", "Position 100", "Description",
                "FP");

        String export = testHelper.catalogExport(asCompanyOwner, "102",
                r -> {
                    TestAssert.assertHeaders(r, "102");
                    return r.extract().response().body().prettyPrint();
                });

        String[] lines = getLinesWithoutHeader(export);

        assertEquals(8, lines.length);
        TestAssert.assertContainsLine(expectedLine, lines);
        assertSortingOfAllLines(lines);
    }

    @Test
    public void Given_Variation_When_ExportGroup_Then_Success() {
        List<String> expectedLine = Arrays.asList("102", "1", "30", "31", "100", "1",
                "Trade 102", "Chapter 1", "Variant 30", "Section 31", "Position 100", "Description",
                "FP");

        String[] lines = export("102.1");
        assertEquals(3, lines.length);
        TestAssert.assertContainsLine(expectedLine, lines);
    }

    @Test
    public void Given_Variation_When_ExportVariant_Then_Success() {
        List<String> expectedLine = Arrays.asList("102", "0", "31", "30", "100", "1",
                "Trade 102", "Chapter 0", "Variant 31", "Section 30", "Position 100", "Description",
                "FP");

        String[] lines = export("102.0.31");
        assertEquals(1, lines.length);
        TestAssert.assertContainsLine(expectedLine, lines);
    }

    @Test
    public void Given_Variation_When_ExportSection_Then_Success() {
        List<String> expectedLine = Arrays.asList("102", "0", "30", "31", "200", "1",
                "Trade 102", "Chapter 0", "Variant 30", "Section 31", "Position 200", "Description",
                "FP");

        String[] lines = export("102.0.30.31");
        assertEquals(3, lines.length);
        TestAssert.assertContainsLine(expectedLine, lines);
    }

    @Test
    public void Given_Variation_When_ExportTask_Then_Success() {
        List<String> expectedLine = Arrays.asList("102", "0", "30", "31", "100", "2",
                "Trade 102", "Chapter 0", "Variant 30", "Section 31", "Position 100", "Description 2",
                "FP");

        String[] lines = export("102.0.30.31.100");
        assertEquals(2, lines.length);
        TestAssert.assertContainsLine(expectedLine, lines);
    }

    @Test
    public void Given_NonExistingVariation_When_Export_Then_Throw() {
        exportAndExpectInErrorResponseMessage("9999",
                "Item or it's ancestor does not exist for SNP",
                "9999");
    }

    @Test
    public void Given_DisabledTrade_When_Export_Then_SpecificChildrenAreNotInCsv() {
        testHelper.deleteCatalogNode(asCompanyOwner, "102");
        exportAndExpectInErrorResponseMessage("102",
                "Item or it's ancestor does not exist for SNP",
                "102");
    }

    @Test
    public void Given_DisabledGroup_When_Export_Then_SpecificChildrenAreNotInCsv() {
        testHelper.deleteCatalogNode(asCompanyOwner, "102.1");
        String[] lines = export("102");
        assertEquals(5, lines.length);
    }

    @Test
    public void Given_DisabledVariant_When_Export_Then_SpecificChildrenAreNotInCsv() {
        testHelper.deleteCatalogNode(asCompanyOwner, "102.0.31");
        String[] lines = export("102");
        assertEquals(7, lines.length);
    }

    @Test
    public void Given_DisabledSection_When_Export_Then_SpecificChildrenAreNotInCsv() {
        testHelper.deleteCatalogNode(asCompanyOwner, "102.1.30.32");
        String[] lines = export("102");
        assertEquals(6, lines.length);
    }

    @Test
    public void Given_DisabledTask_When_Export_Then_SpecificChildrenAreNotInCsv() {
        testHelper.deleteCatalogNode(asCompanyOwner, "102.0.30.31.100");
        String[] lines = export("102");
        assertEquals(6, lines.length);
    }

    @Test
    public void Given_Catalog_When_ExportAll_Then_Success() {
        String[] lines = export(Constants.FULL_CATALOG);

        TestAssert.assertContainsVariation(lines, "102.0.30.31.100.1");
        TestAssert.assertContainsVariation(lines, "102.0.30.31.100.2");
        TestAssert.assertContainsVariation(lines, "102.0.30.32.100.1");
        TestAssert.assertContainsVariation(lines, "102.0.31.30.100.1");
        TestAssert.assertContainsVariation(lines, "102.1.30.31.100.1");
        TestAssert.assertContainsVariation(lines, "102.1.30.32.100.1");
        TestAssert.assertContainsVariation(lines, "102.1.30.32.200.1");
        TestAssert.assertContainsVariation(lines, "102.0.30.31.200.1");

        TestAssert.assertContainsVariation(lines, "255.0.20.20.300.1");
        TestAssert.assertContainsVariation(lines, "255.0.30.20.300.1");
    }

    @Test
    public void Given_Catalog_When_ExportEmpty_Then_Success() {
        String csv = testHelper.catalogExport(asCompanyOwner, Constants.EMPTY_CATALOG);
        String[] lines = splitCsvLines(csv);
        assertEquals(1, lines.length);
        String[] fields = lines[0].split(String.valueOf(Constants.CSV_SEPARATOR));
        assertArrayEquals(fields, CatalogItemLine.getHeader());
    }

    private void exportAndExpectInErrorResponseMessage(String scope, String... msgParts) {
        testHelper.catalogExport(asCompanyOwner, scope,
                r -> {
                    TestErrorResponse error = r.assertThat()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .extract()
                            .as(TestErrorResponse.class);
                    Arrays.asList(msgParts)
                            .forEach(p -> assertThat(error.getReason(), containsString(p)));
                    return null;
                });
    }

    private String[] export(String s) {
        String export = testHelper.catalogExport(asCompanyOwner, s);
        return getLinesWithoutHeader(export);
    }

    private String[] getLinesWithoutHeader(String export) {
        String[] lines = splitCsvLines(export);
        return Arrays.copyOfRange(lines, 1, lines.length);
    }

    private String[] splitCsvLines(String export) {
        return export.split("\n");
    }

    private void assertSortingOfAllLines(String[] lines) {
        assertThat(lines[0], containsString("102;0;30;31;100;1"));
        assertThat(lines[1], containsString("102;0;30;31;100;2"));
        assertThat(lines[2], containsString("102;0;30;31;200;1"));
        assertThat(lines[3], containsString("102;0;30;32;100;1"));
        assertThat(lines[4], containsString("102;0;31;30;100;1"));
        assertThat(lines[5], containsString("102;1;30;31;100;1"));
        assertThat(lines[6], containsString("102;1;30;32;100;1"));
        assertThat(lines[7], containsString("102;1;30;32;200;1"));
    }

}
