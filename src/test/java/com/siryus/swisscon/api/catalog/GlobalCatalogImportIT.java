package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.catalog.dto.CatalogImportReportDTO;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;
import com.siryus.swisscon.api.util.error.TestErrorResponse;
import io.restassured.specification.RequestSpecification;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Arrays;

import static com.siryus.swisscon.api.base.TestHelper.COMPANY_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_EMAIL;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_FIRST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_LAST_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GlobalCatalogImportIT extends AbstractMvcTestBase {

    public static final String CATALOGS_FOLDER = "test_catalogs";

    private static RequestSpecification asCompanyOwner;

    private final GlobalCatalogNodeTestRepository nodeTestRepository;
    private final CatalogVariationTestRepository variationTestRepository;

    @Autowired
    public GlobalCatalogImportIT(GlobalCatalogNodeTestRepository nodeTestRepository, CatalogVariationTestRepository variationTestRepository) {
        this.nodeTestRepository = nodeTestRepository;
        this.variationTestRepository = variationTestRepository;
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

    @BeforeAll
    public void init() {
        testHelper.signUp(TestBuilder.testSignupDTO(COMPANY_NAME, PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME));
        asCompanyOwner = testHelper.login(PROJECT_OWNER_EMAIL);
    }

    @BeforeEach()
    public void cleanTestCatalogItemsAndCounter() {
        variationTestRepository.deleteAll();
        nodeTestRepository.deleteAll();
    }

    @Test
    public void Given_CsvRealSample_When_Import_Then_Success() throws IOException {
        CatalogImportReportDTO catalogImportReportDTO = testHelper.catalogImport(asCompanyOwner, readBytes("sample_catalog.csv"));
        TestAssert.assertCatalogImportReportDTO(catalogImportReportDTO, 97, 0, 0, 11, 37);

        testAssert.assertPersistedGlobalCatalogNode("224", null, "Flachdach");
        testAssert.assertPersistedGlobalCatalogNode("224.100", "224", "Vorarbeiten");
        testAssert.assertPersistedGlobalCatalogNode("224.100.0", "224.100", null);
        testAssert.assertPersistedGlobalCatalogNode("224.100.0.110", "224.100.0", "Verschiedenes");

        testAssert.assertGlobalCatalogVariation("224.100.0.110.101", "Pflanztröge, Mobiliar und dgl. Entfernen und wieder aufstellen nach Aufwand.", "Meister", 2, "h");
    }

    @Test
    public void Given_CsvCorrect_When_Import_Then_Success() throws IOException {
        CatalogImportReportDTO catalogImportReportDTO = testHelper.catalogImport(asCompanyOwner, readBytes("correct.csv"));
        TestAssert.assertCatalogImportReportDTO(catalogImportReportDTO, 11, 0, 0, 6, 2);

        testAssert.assertPersistedGlobalCatalogNode("102", null, "Trade 102");
        testAssert.assertPersistedGlobalCatalogNode("102.0", "102", "Chapter 0");
        testAssert.assertPersistedGlobalCatalogNode("102.0.30", "102.0", "Variant 30");
        testAssert.assertPersistedGlobalCatalogNode("102.0.30.31", "102.0.30", "Section 31");
        testAssert.assertPersistedGlobalCatalogNode("102.0.30.31.100", "102.0.30.31", "102.0.30.31.100");
        testAssert.assertPersistedGlobalCatalogNode("102.0.80", "102.0", "Variant 80");
        testAssert.assertPersistedGlobalCatalogNode("102.0.80.31", "102.0.80", "Section 31");
        testAssert.assertPersistedGlobalCatalogNode("102.0.80.31.100", "102.0.80.31", "102.0.80.31.100");

        testAssert.assertGlobalCatalogVariation("102.0.30.31.100", "Position 100", "Description", 1, "FP");
        testAssert.assertGlobalCatalogVariation("102.0.30.31.100", "Position 100", "Description 2", 2, "FP");
        testAssert.assertGlobalCatalogVariation("102.0.80.31.100", "Position 100", "Description with äöüÄÖÜ", 1, "FP");
    }

    @Test
    public void Given_CsvCorrectMultipleTimes_When_Import_Then_Success() throws IOException {
        CatalogImportReportDTO firstCatalogUploadReportDTO = testHelper.catalogImport(asCompanyOwner, readBytes("correct.csv"));
        TestAssert.assertCatalogImportReportDTO(firstCatalogUploadReportDTO, 11, 0, 0, 6, 2);
        CatalogVariationEntity catalogVariation = variationTestRepository
                .findLatestBySnpAndVariationNumber("102.0.30.31.100", 1, Constants.GLOBAL_CATALOG_COMPANY_ID)
                .orElseThrow();

        // upload the same file again > all items should have been ignored
        CatalogImportReportDTO secondCatalogUploadReportDTO = testHelper.catalogImport(asCompanyOwner, readBytes("correct.csv"));
        TestAssert.assertCatalogImportReportDTO(secondCatalogUploadReportDTO, 11, 6, 2, 0, 0);

        // check that the edited variation was ignored and has no new reference to a new node id
        CatalogVariationEntity catalogVariation2 = variationTestRepository
                .findLatestBySnpAndVariationNumber("102.0.30.31.100", 1, Constants.GLOBAL_CATALOG_COMPANY_ID)
                .orElseThrow();
        assertEquals(catalogVariation.getCatalogNodeId(), catalogVariation2.getCatalogNodeId());

        // within the file 3 lines are changed > so 3 Items have to be updated
        CatalogImportReportDTO thirdCatalogUploadReportDTO = testHelper.catalogImport(asCompanyOwner, readBytes("correct_3_lines_changed.csv"));
        TestAssert.assertCatalogImportReportDTO(thirdCatalogUploadReportDTO, 11, 6, 2, 0, 0);

        testAssert.assertPersistedGlobalCatalogNode("102", null, "Trade 102");
        testAssert.assertPersistedGlobalCatalogNode("102.0", "102", "Chapter 0");
        testAssert.assertPersistedGlobalCatalogNode("102.0.30", "102.0", "Variant 30 changed");
        testAssert.assertPersistedGlobalCatalogNode("102.0.30.31", "102.0.30", "Section 31 changed");
        testAssert.assertPersistedGlobalCatalogNode("102.0.30.31.100", "102.0.30.31", "102.0.30.31.100");
        testAssert.assertPersistedGlobalCatalogNode("102.0.80", "102.0", "Variant 80");
        testAssert.assertPersistedGlobalCatalogNode("102.0.80.31", "102.0.80", "Section 31 changed");
        testAssert.assertPersistedGlobalCatalogNode("102.0.80.31.100", "102.0.80.31", "102.0.80.31.100");

        testAssert.assertGlobalCatalogVariation("102.0.30.31.100", "Position 100", "Description changed", 1, "FP");
        testAssert.assertGlobalCatalogVariation("102.0.30.31.100", "Position 100", "Description 2", 2, "FP");
        testAssert.assertGlobalCatalogVariation("102.0.80.31.100", "Position 100", "Description with äöüÄÖÜ", 1, "FP");
    }

    @Test
    public void Given_CsvPartiallyFilledMultipleTimes_When_Import_Then_Success() throws IOException {
        CatalogImportReportDTO firstCatalogUploadReportDTO = testHelper.catalogImport(asCompanyOwner, readBytes("correct_partial_1.csv"));
        TestAssert.assertCatalogImportReportDTO(firstCatalogUploadReportDTO, 4, 0, 0, 4, 0);
        testAssert.assertPersistedGlobalCatalogNode("102", null, "Trade 102");
        testAssert.assertPersistedGlobalCatalogNode("102.0", "102", "Chapter 0");
        testAssert.assertPersistedGlobalCatalogNode("102.0.30", "102.0", "Variant 30");
        testAssert.assertPersistedGlobalCatalogNode("102.0.30.31", "102.0.30", "Section 31");

        CatalogImportReportDTO secondCatalogUploadReportDTO = testHelper.catalogImport(asCompanyOwner, readBytes("correct_partial_2.csv"));
        TestAssert.assertCatalogImportReportDTO(secondCatalogUploadReportDTO, 7, 4, 0, 2, 2);
        testAssert.assertPersistedGlobalCatalogNode("102.0.30.31.100", "102.0.30.31", "102.0.30.31.100");
        testAssert.assertPersistedGlobalCatalogNode("102.0.80", "102.0", "Variant 80");
        testAssert.assertPersistedGlobalCatalogNode("102.0.80.31", "102.0.80", "Section 31");
        testAssert.assertPersistedGlobalCatalogNode("102.0.80.31.100", "102.0.80.31", "102.0.80.31.100");

        testAssert.assertGlobalCatalogVariation("102.0.30.31.100", "Position 100", "Description", 1, "FP");
        testAssert.assertGlobalCatalogVariation("102.0.30.31.100", "Position 100", "Description 2", 2, "FP");
        testAssert.assertGlobalCatalogVariation("102.0.80.31.100", "Position 100", "Description with äöüÄÖÜ", 1, "FP");

        CatalogImportReportDTO thirdCatalogUploadReportDTO = testHelper.catalogImport(asCompanyOwner, readBytes("correct_modified_position.csv"));
        TestAssert.assertCatalogImportReportDTO(thirdCatalogUploadReportDTO, 1, 4, 1, 0, 0);
        testAssert.assertGlobalCatalogVariation("102.0.30.31.100", "Position 100 changed", "Description", 1, "FP");
    }

    @Test
    public void Given_CsvWithAnInvalidUnit_When_Import_Then_Throw() throws IOException {
        importAndExpectInErrorResponseMessage("wrong_unit.csv",
                "Unit with symbol not found in DB",
                "NON_EXISTING_UNIT");
    }


    private void importAndExpectInErrorResponseMessage(String file, String... msgParts) throws IOException {
        testHelper.catalogImport(asCompanyOwner, readBytes(file),
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

    public static byte[] readBytes(String filename) throws IOException {
        String testFilePath = "/" + CATALOGS_FOLDER + "/" + filename;
        return TestBuilder.readBytes(testFilePath);
    }

}
