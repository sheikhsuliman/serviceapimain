package com.siryus.swisscon.api.base.helpers;

import com.siryus.swisscon.api.file.file.FileController;
import com.siryus.swisscon.api.catalog.dto.CatalogImportReportDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogNodeDTO;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.springframework.http.HttpStatus;

import java.util.function.Function;

import static com.siryus.swisscon.api.base.AbstractMvcTestBase.endPoint;
import static io.restassured.RestAssured.given;

public class Catalog {
    public CatalogImportReportDTO globalCatalogImport(RequestSpecification spec, byte[] fileContent) {
        return globalCatalogImport(spec, fileContent,
                r -> r.assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .as(CatalogImportReportDTO.class));
    }

    public CatalogImportReportDTO globalCatalogImport(RequestSpecification spec, byte[] fileContent, Function<ValidatableResponse, CatalogImportReportDTO> responseValidator) {
        return responseValidator.apply(given().spec(spec).contentType("multipart/form-data")
                .multiPart(new MultiPartSpecBuilder(fileContent)
                        .controlName(FileController.MULTIFORM_FILE_IDENTIFIER)
                        .build())
                .when()
                .post(endPoint("/new-catalog/import"))
                .then());
    }

    public String globalCatalogExport(RequestSpecification spec, String scope) {
        return globalCatalogExport(spec, scope,
                r -> r.assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .response().body().prettyPrint());
    }

    public String globalCatalogExport(RequestSpecification spec, String scope, Function<ValidatableResponse, String> responseValidator) {
        return responseValidator.apply(given().spec(spec)
                .when()
                .pathParam("scope", scope)
                .get(endPoint("/new-catalog/export/{scope}"))
                .then());
    }

    public CatalogImportReportDTO companyCatalogImport(RequestSpecification spec, Integer companyId, byte[] fileContent) {
        return companyCatalogImport(spec, companyId, fileContent,
                r -> r.assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .as(CatalogImportReportDTO.class));
    }

    public CatalogImportReportDTO companyCatalogImport(RequestSpecification spec, Integer companyId, byte[] fileContent, Function<ValidatableResponse, CatalogImportReportDTO> responseValidator) {
        return responseValidator.apply(given().spec(spec).contentType("multipart/form-data")
                .multiPart(new MultiPartSpecBuilder(fileContent)
                        .controlName(FileController.MULTIFORM_FILE_IDENTIFIER)
                        .build())
                .when()
                .pathParam("companyId", companyId)
                .post(endPoint("/company-catalog/{companyId}/import"))
                .then());
    }

    public String companyCatalogExport(RequestSpecification spec, Integer companyId, String scope, boolean showGlobal) {
        return companyCatalogExport(spec, companyId, scope, showGlobal,
                r -> r.assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .response().body().prettyPrint());
    }

    public String companyCatalogExport(RequestSpecification spec, Integer companyId, String scope, boolean showGlobal, Function<ValidatableResponse, String> responseValidator) {
        return responseValidator.apply(given().spec(spec)
                .when()
                .pathParam("companyId", companyId)
                .pathParam("scope", scope)
                .queryParam("showGlobal", showGlobal)
                .get(endPoint("/company-catalog/{companyId}/export/{scope}"))
                .then());
    }

    public CatalogNodeDTO deleteCatalogNode(RequestSpecification spec, String snp) {
        return deleteCatalogNode(spec, snp,
                r -> r.assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .as(CatalogNodeDTO.class));
    }

    public CatalogNodeDTO deleteCatalogNode(RequestSpecification spec, String snp, Function<ValidatableResponse, CatalogNodeDTO> responseValidator) {
        return responseValidator.apply(given().spec(spec)
                .when()
                .pathParam("snp", snp)
                .post(endPoint("/new-catalog/node/{snp}/delete"))
                .then());
    }
}
