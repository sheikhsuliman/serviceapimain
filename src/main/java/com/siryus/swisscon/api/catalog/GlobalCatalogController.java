package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.csvreader.CatalogItemLineWriter;
import com.siryus.swisscon.api.catalog.dto.CatalogImportReportDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogNodeDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogNodeRequest;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

@RestController
@RequestMapping("/api/rest/new-catalog")
@Api( tags = {"Catalog:global"})
class GlobalCatalogController {

    private final GlobalCatalogReader reader;
    private final GlobalCatalogUpdater updater;
    private final GlobalCatalogUploadService uploadService;
    private final GlobalCatalogDownloadService catalogDownloadService;

    @Autowired
    GlobalCatalogController(
            GlobalCatalogReader reader,
            GlobalCatalogUpdater updater,
            GlobalCatalogUploadService uploadService,
            GlobalCatalogDownloadService catalogDownloadService
    ) {
        this.reader = reader;
        this.updater = updater;
        this.uploadService = uploadService;
        this.catalogDownloadService = catalogDownloadService;
    }

    //
    // Catalog Reader
    //

    @ApiOperation(
            value = "List all root nodes in global catalog",
            tags = "Catalog:global"
    )
    @GetMapping(path = "list/roots")
    List<CatalogNodeDTO> listRoots() {
        return reader.listRoots();
    }

    @ApiOperation(
            value = "List all children nodes for given parent node in global catalog",
            tags = "Catalog:global"
    )
    @GetMapping(path = "list/children/{parentSnp}")
    List<CatalogNodeDTO> listChildren(@PathVariable("parentSnp") String parentSnp) {
       return reader.listChildren(parentSnp);
    }

    @ApiOperation(
            value = "Return expanded tree of node with given snp in global catalog",
            tags = "Catalog:global"
    )
    @GetMapping(path = "expand/{snp}")
    List<CatalogNodeDTO> expand(@PathVariable("snp") String snp) {
        return reader.expand(snp);
    }

    //
    // Catalog Updater
    //

    @ApiOperation(
            value = "Add new node in global catalog",
            tags = "Catalog:global"
    )
    @PostMapping(path="node/add")
    @PreAuthorize("hasPermission(0, 'COMPANY', 'COMPANY_CATALOG_EDIT')")
    CatalogNodeDTO addCatalogNode(@RequestBody CatalogNodeRequest request) {
        return updater.addCatalogNode(request);
    }

    @ApiOperation(
            value = "Edit existing node in global catalog",
            tags = "Catalog:global"
    )
    @PostMapping(path="node/edit")
    @PreAuthorize("hasPermission(0, 'COMPANY', 'COMPANY_CATALOG_EDIT')")
    CatalogNodeDTO editCatalogNode(@RequestBody CatalogNodeRequest request) {
        return updater.editCatalogNode(request);
    }

    @ApiOperation(
            value = "Archive node in global catalog",
            tags = "Catalog:global"
    )
    @PostMapping(path={ "node/{snp}/delete", "node/{snp}/archive" })
    @PreAuthorize("hasPermission(0, 'COMPANY', 'COMPANY_CATALOG_EDIT')")
    CatalogNodeDTO archiveCatalogNode(@PathVariable String snp) {
        return updater.archiveCatalogNode(snp);
    }

    @ApiOperation(
            value = "Create leaf node with variations in global catalog",
            tags = "Catalog:global"
    )
    @PostMapping(path="variations/add")
    @PreAuthorize("hasPermission(0, 'COMPANY', 'COMPANY_CATALOG_EDIT')")
    CatalogNodeDTO addCatalogLeafWithVariations(@RequestBody CatalogVariationsRequest request) {
        return updater.addCatalogLeafWithVariations(request);
    }


    @ApiOperation(
            value = "Edit leaf node variations in global catalog",
            tags = "Catalog:global"
    )
    @PostMapping(path="variations/edit")
    @PreAuthorize("hasPermission(0, 'COMPANY', 'COMPANY_CATALOG_EDIT')")
    CatalogNodeDTO editCatalogVariations(@RequestBody CatalogVariationsRequest request) {
        return updater.editCatalogVariations(request);
    }

    //
    // Catalog Import/Export
    //

    @ApiOperation(
            value = "Import global catalog entries from csv file",
            tags = "Catalog:global"
    )
    @PostMapping(path = "import", headers = ("content-type=multipart/*"))
    @PreAuthorize("hasPermission(0, 'COMPANY', 'COMPANY_CATALOG_EDIT')")
    CatalogImportReportDTO importCatalog(MultipartHttpServletRequest request) {
        MultipartFile multipartFile = FileController.validateAndGetFile(request);
        return uploadService.importCatalog(multipartFile);
    }

    @GetMapping(path = "export/{scope}")
    @PreAuthorize("hasPermission(0, 'COMPANY', 'COMPANY_CATALOG_EDIT')")
    @ApiOperation(value = "Export the global catalog with scope: snp, all or empty",
            notes = "Examples: \n" +
            "export/100.200 (export all child items which snp starts with 100.200)\n" +
            "export/all (export the complete catalog)\n" +
            "export/empty (export an empty table template)",
            tags = "Catalog:global"
    )
    void exportCatalog(@PathVariable("scope") String scope, HttpServletResponse response) {
        PrintWriter printWriter = CatalogItemLineWriter.prepareFileAndGetWriter(scope, response);
        catalogDownloadService.exportGlobalCatalog(scope, printWriter);
    }
}
