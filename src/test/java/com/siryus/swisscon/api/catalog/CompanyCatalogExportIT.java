package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.catalog.csvreader.CatalogItemLine;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.siryus.swisscon.api.base.TestHelper.COMPANY_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_EMAIL;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_FIRST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_LAST_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompanyCatalogExportIT extends AbstractMvcTestBase {

    private static RequestSpecification asCompanyOwner;
    private static Integer companyId;

    private final GlobalCatalogNodeTestRepository nodeTestRepository;

    @Autowired
    public CompanyCatalogExportIT(GlobalCatalogNodeTestRepository nodeTestRepository) {
        this.nodeTestRepository = nodeTestRepository;
    }

    @BeforeAll
    public void init() throws IOException {
        companyId = testHelper
                .signUp(TestBuilder.testSignupDTO(COMPANY_NAME, PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME))
                .getCompanyId();
        asCompanyOwner = testHelper.login(PROJECT_OWNER_EMAIL);
        testHelper.catalogImport(asCompanyOwner, GlobalCatalogImportIT.readBytes("export_sample.csv"));
        testHelper.companyCatalogImport(asCompanyOwner, companyId, GlobalCatalogImportIT.readBytes("export_sample_import.csv"));
    }

    @BeforeEach
    public void reactiveImportedTestData() {
        nodeTestRepository.enableAll();
    }

    @Test
    public void Given_ImportedCompanyCatalog_When_ExportWithGlobalVariations_Then_Success() {
        String csv = testHelper.companyCatalogExport(asCompanyOwner, companyId, "102", true);
        String[] lines = splitCsvLines(csv);
        assertEquals(9, lines.length);

        List<String> companyLine = Arrays.asList("102", "0", "30", "31", "100", "1", "Trade 102", "Chapter 0", "Variant 30",
                "Section 31", "Position 100", "Description", "FP", "1", "", "20.00", "Position 100", "Description", "m2");
        List<String> globalLine = Arrays.asList("102", "0", "30", "31", "200", "1", "Trade 102", "Chapter 0", "Variant 30",
                "Section 31", "Position 200", "Description", "FP");

        TestAssert.assertContainsLine(companyLine, lines);
        TestAssert.assertContainsLine(globalLine, lines);
    }

    @Test
    public void Given_ImportedCompanyCatalog_When_ExportWithoutGlobalVariations_Then_Success() {
        String csv = testHelper.companyCatalogExport(asCompanyOwner, companyId, "102", false);
        String[] lines = splitCsvLines(csv);
        assertEquals(4, lines.length);

        List<String> line1 = Arrays.asList("102", "0", "30", "31", "100", "1", "Trade 102", "Chapter 0", "Variant 30",
                "Section 31", "Position 100", "Description", "FP", "1", "", "20.00", "Position 100", "Description", "m2");
        List<String> line2 = Arrays.asList("102", "0", "30", "31", "100", "2", "Trade 102", "Chapter 0", "Variant 30", "Section 31",
                "Position 100", "Description 2", "FP", "1", "a,b,c", "15.50", "task", "Description 2", "FP");
        List<String> line3 = Arrays.asList("102", "0", "30", "32", "100", "1", "Trade 102", "Chapter 0", "Variant 30",
                "Section 32", "Position 100", "Description", "FP", "0", "", "", "Position 100", "variant", "FP");

        TestAssert.assertContainsLine(line1, lines);
        TestAssert.assertContainsLine(line2, lines);
        TestAssert.assertContainsLine(line3, lines);
    }

    @Test
    public void Given_ImportedCompanyCatalog_When_ExportEmpty_Then_SuccessAndAllHeadersArePresent() {
        String csv = testHelper.companyCatalogExport(asCompanyOwner, companyId, Constants.EMPTY_CATALOG, false);
        String[] lines = splitCsvLines(csv);
        assertEquals(1, lines.length);

        TestAssert.assertContainsLine(Arrays.asList(CatalogItemLine.getHeader()), lines);
    }

    @Test
    public void Given_ImportedCompanyCatalog_When_ExportFull_Then_Success() {
        String csv = testHelper.companyCatalogExport(asCompanyOwner, companyId, Constants.FULL_CATALOG, false);
        String[] lines = splitCsvLines(csv);

        List<String> line1 = Arrays.asList("102", "0", "30", "31", "100", "1", "Trade 102", "Chapter 0", "Variant 30",
                "Section 31", "Position 100", "Description", "FP", "1", "", "20.00", "Position 100", "Description", "m2");

        TestAssert.assertContainsLine(Arrays.asList(CatalogItemLine.getHeader()), lines);
        TestAssert.assertContainsLine(line1, lines);
    }


    private String[] splitCsvLines(String export) {
        return export.split("\n");
    }

}
