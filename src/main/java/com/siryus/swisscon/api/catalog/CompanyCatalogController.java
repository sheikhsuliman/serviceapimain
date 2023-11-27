package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.csvreader.CatalogItemLineWriter;
import com.siryus.swisscon.api.catalog.dto.CatalogImportReportDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogNodeDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogVariationsRequest;
import com.siryus.swisscon.api.file.file.FileController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

@RestController
@RequestMapping("/api/rest/company-catalog")
@Api( tags = {"Catalog:company"})
class CompanyCatalogController {
    private final CompanyCatalogReader catalogReader;
    private final CompanyCatalogUpdater catalogUpdater;
    private final CompanyCatalogUploadService catalogUploadService;
    private final CompanyCatalogDownloadService catalogDownloadService;

    @Autowired
    CompanyCatalogController(
            CompanyCatalogReader catalogReader,
            CompanyCatalogUpdater catalogUpdater,
            CompanyCatalogUploadService catalogUploadService,
            CompanyCatalogDownloadService catalogDownloadService
    ) {
        this.catalogReader = catalogReader;
        this.catalogUpdater = catalogUpdater;
        this.catalogUploadService = catalogUploadService;
        this.catalogDownloadService = catalogDownloadService;
    }

    @ApiOperation(
            value = "List all root nodes for given company catalog. " +
                    "NOTE: This will return nodes which are disabled in given company catalog " +
                    "(these nodes will have non-null value in disabled field)",
            tags = "Catalog:company"
    )
    @GetMapping(path = "{companyId}/list/roots")
    List<CatalogNodeDTO> listRoots(@PathVariable Integer companyId) {
        return catalogReader.listRoots(companyId);
    }

    @ApiOperation(
            value = "List all children nodes for given parent node in given company catalog" +
                    "NOTE: This will return nodes which are disabled in given company catalog " +
                    "(these nodes will have non-null value in disabled field)",
            tags = "Catalog:company"
    )
    @GetMapping(path = "{companyId}/list/children/{parentSnp}")
    List<CatalogNodeDTO> listChildren(@PathVariable Integer companyId, @PathVariable("parentSnp") String parentSnp) {
        return catalogReader.listChildren(companyId, parentSnp);
    }

    @ApiOperation(
            value = "Return expanded tree of node with given snp in given company catalog",
            tags = "Catalog:company"
    )
    @GetMapping(path = "{companyId}/expand/{snp}")
    List<CatalogNodeDTO> expand(@PathVariable Integer companyId, @PathVariable("snp") String snp) {
        return catalogReader.expand(companyId, snp);
    }

    @ApiOperation(
            value = "Archive given node in given company catalog",
            tags = "Catalog:company"
    )
    @PostMapping(path="{companyId}/node/{snp}/archive")
    @PreAuthorize("hasPermission(#companyId, 'COMPANY', 'COMPANY_CATALOG_EDIT')")
    CatalogNodeDTO archiveCatalogNode(@PathVariable Integer companyId, @PathVariable String snp) {
        return catalogUpdater.archiveCatalogNode(companyId, snp);
    }

    @ApiOperation(
            value = "Restore given node in given company catalog",
            tags = "Catalog:company"
    )
    @PostMapping(path="{companyId}/node/{snp}/restore")
    @PreAuthorize("hasPermission(#companyId, 'COMPANY', 'COMPANY_CATALOG_EDIT')")
    CatalogNodeDTO restoreCatalogNode(@PathVariable Integer companyId, @PathVariable String snp) {
        return catalogUpdater.restoreCatalogNode(companyId, snp);
    }

    @ApiOperation(
            value = "Restore given node in given company catalog",
            tags = "Catalog:company"
    )
    @PostMapping(path="{companyId}/node/{snp}/restore/{variationNumber}")
    @PreAuthorize("hasPermission(#companyId, 'COMPANY', 'COMPANY_CATALOG_EDIT')")
    CatalogNodeDTO restoreCatalogVariation(@PathVariable Integer companyId, @PathVariable String snp, @PathVariable Integer variationNumber) {
        return catalogUpdater.restoreCatalogVariation(companyId, snp, variationNumber);
    }

    @ApiOperation(
            value = "Update leaf variations in given company catalog",
            tags = "Catalog:company"
    )
    @PostMapping(path="{companyId}/variations/edit")
    @PreAuthorize("hasPermission(#companyId, 'COMPANY', 'COMPANY_CATALOG_EDIT')")
    CatalogNodeDTO editCatalogVariations(@PathVariable Integer companyId, @RequestBody CatalogVariationsRequest request) {
        return catalogUpdater.updateCatalogLeafVariations(companyId, request);
    }

    //
    // Catalog Import/Export
    //

    @ApiOperation(
            value = "Import company catalog entries from csv file",
            tags = "Catalog:company"
    )
    @PostMapping(path = "{companyId}/import", headers = ("content-type=multipart/*"))
    @PreAuthorize("hasPermission(0, 'COMPANY', 'COMPANY_CATALOG_EDIT')")
    CatalogImportReportDTO importCatalog(@PathVariable Integer companyId, MultipartHttpServletRequest request) {
        MultipartFile multipartFile = FileController.validateAndGetFile(request);
        return catalogUploadService.importCatalog(companyId, multipartFile);
    }

    @GetMapping(path = "{companyId}/export/{scope}")
    @PreAuthorize("hasPermission(0, 'COMPANY', 'COMPANY_CATALOG_EDIT')")
    @ApiOperation(value = "Export the company catalog with scope: snp, all or empty." +
            "If you activate 'show global' exports the complete catalog." +
            "Including the nodes which have to company specification",
            notes = "Examples: \n" +
                    "export/100.200?showGlobal=true (export all child items which snp starts with 100.200)\n" +
                    "export/all?showGlobal=true (export the complete company catalog)\n" +
                    "export/all?showGlobal=true (export the complete catalog, including not overwritten nodes)\n" +
                    "export/empty?showGlobal=true (export an empty table template)",
            tags = "Catalog:company"
    )
    void exportCatalog(@PathVariable("companyId") Integer companyId,
                              @PathVariable("scope") String scope,
                              @RequestParam("showGlobal") boolean showGlobal,
                              HttpServletResponse response) {
        PrintWriter printWriter = CatalogItemLineWriter.prepareFileAndGetWriter(scope, response);
        catalogDownloadService.exportCompanyCatalog(companyId, scope, showGlobal, printWriter);
    }
}
