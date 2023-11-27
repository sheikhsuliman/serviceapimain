package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.catalog.dto.CatalogImportReportDTO;
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

public class CompanyCatalogImportIT extends AbstractMvcTestBase {

    private static final String CATALOGS_FOLDER = "test_catalogs";

    private static RequestSpecification asCompanyOwner;
    private static Integer companyId;

    private final GlobalCatalogNodeTestRepository nodeTestRepository;
    private final CatalogVariationTestRepository variationTestRepository;

    @Autowired
    public CompanyCatalogImportIT(GlobalCatalogNodeTestRepository nodeTestRepository, CatalogVariationTestRepository variationTestRepository) {
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
        companyId = testHelper
                .signUp(TestBuilder.testSignupDTO(COMPANY_NAME, PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME))
                .getCompanyId();
        asCompanyOwner = testHelper.login(PROJECT_OWNER_EMAIL);
    }

    @BeforeEach()
    public void cleanTestCatalogItemsAndCounter() throws IOException {
        variationTestRepository.deleteAll();
        nodeTestRepository.deleteAll();
        testHelper.catalogImport(asCompanyOwner, readBytes("correct.csv"));
    }


    @Test
    public void Given_CsvWithCompanyVariations_When_Import_Then_Success() throws IOException {
        CatalogImportReportDTO firstCatalogUploadReportDTO = testHelper.companyCatalogImport(asCompanyOwner, companyId, readBytes("company_correct.csv"));
        TestAssert.assertCatalogImportReportDTO(firstCatalogUploadReportDTO, 4, 0, 2, 0, 0);

        testAssert.assertCompanyCatalogVariation(companyId, "102.0.30.31.100", "Overriden Task_1", "Overriden Sub Task_1", 1, "m2", true, "12.50", "item_a\nitem_b");
        testAssert.assertCompanyCatalogVariation(companyId, "102.0.30.31.100", "Position 100", "Description 2", 2, "m3", true, "5.00", "a\nb\nc");
        testAssert.assertCompanyCatalogVariation(companyId, "102.0.30.31.100", "Position 100", "Overriden Sub Task_3", 3, "FP", false, null, null);
        testAssert.assertCompanyCatalogVariation(companyId, "102.0.80.31.100", "Overriden Task_4", "Description 4", 1, "FP", true, "3.62", null);
    }

    @Test
    public void Given_CsvWithCompanyVariationsAndNoUnit_When_Import_Then_Throw() throws IOException {
        importAndExpectInErrorResponseMessage("company_no_units.csv", "The unit of a position has to be set");
    }

    @Test
    public void Given_CsvWithCompanyVariationsAndInvalidPrice_When_Import_Then_Throw() throws IOException {
        importAndExpectInErrorResponseMessage("company_invalid_price.csv", "price should be a valid number");
    }

    @Test
    public void Given_CsvWithCompanyVariationsAndIsActiveIsNot0Or1_When_Import_Then_Throw() throws IOException {
        importAndExpectInErrorResponseMessage("company_invalid_is_active.csv", "isActive has to bet set to 1 or 0");
    }

    @Test
    public void Given_CsvWithoutAnyCompanyVariations_When_Import_Then_NoVariationsWhereSaved() throws IOException {
        CatalogImportReportDTO firstCatalogUploadReportDTO = testHelper.companyCatalogImport(asCompanyOwner, companyId, readBytes("correct.csv"));
        TestAssert.assertCatalogImportReportDTO(firstCatalogUploadReportDTO, 11, 0, 0, 0, 0);
    }

    private void importAndExpectInErrorResponseMessage(String file, String... msgParts) throws IOException {
        testHelper.companyCatalogImport(asCompanyOwner, companyId, readBytes(file),
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

    private static byte[] readBytes(String filename) throws IOException {
        String testFilePath = "/" + CATALOGS_FOLDER + "/" + filename;
        return TestBuilder.readBytes(testFilePath);
    }

}
