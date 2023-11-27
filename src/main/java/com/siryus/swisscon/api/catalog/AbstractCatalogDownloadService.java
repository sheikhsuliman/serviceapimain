package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.csvreader.CatalogCSVException;
import com.siryus.swisscon.api.catalog.csvreader.CatalogItemLine;
import com.siryus.swisscon.api.catalog.csvreader.CatalogItemLineWriter;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeEntity;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeRepository;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class AbstractCatalogDownloadService {

    private final CatalogNodeRepository nodeRepository;
    protected final CatalogVariationRepository variationRepository;
    private final CatalogValidator catalogValidator;

    AbstractCatalogDownloadService(CatalogNodeRepository nodeRepository, CatalogVariationRepository variationRepository, CatalogValidator catalogValidator) {
        this.nodeRepository = nodeRepository;
        this.variationRepository = variationRepository;
        this.catalogValidator = catalogValidator;
    }

    protected void exportCatalog(Integer companyId, String scope, boolean showGlobal, PrintWriter writer) {
        switch (scope.toLowerCase()) {
            case Constants.FULL_CATALOG:
                exportFullCatalog(companyId, showGlobal, writer);
                break;
            case Constants.EMPTY_CATALOG:
                exportEmptyCatalog(writer);
                break;
            default:
                exportCatalogFromSnp(companyId, scope, showGlobal, writer);
        }
    }

    private void exportFullCatalog(Integer companyId, boolean showGlobal, PrintWriter writer) {
        Map<String, CatalogNodeEntity> allNodes = findAllNodes();

        exportCatalogFromNodes(companyId, showGlobal, allNodes, writer);
    }

    private void exportCatalogFromSnp(Integer companyId, String parentSnp, boolean showGlobal, PrintWriter writer) {
        catalogValidator.validateNodeAndAllAncestorsExists(parentSnp);

        Map<String, CatalogNodeEntity> allNodes = findAllNodesIncludingParents(parentSnp);

        exportCatalogFromNodes(companyId, showGlobal, allNodes, writer);
    }

    private void exportCatalogFromNodes(Integer companyId, boolean showGlobal, Map<String, CatalogNodeEntity> nodes, PrintWriter writer) {
        List<String> activeLeafSnps = findAllActiveLeafSnps(nodes);

        List<LineVariation> variations = loadAllVariations(companyId, showGlobal, activeLeafSnps);

        List<CatalogItemLine> catalogItemLines = convertToLine(variations, nodes);

        List<CatalogItemLine> sortedCatalogItemLines = sortLines(catalogItemLines);

        writeItemsToCSV(sortedCatalogItemLines, writer);
    }

    private void exportEmptyCatalog(PrintWriter writer) {
        writeItemsToCSV(Collections.emptyList(), writer);
    }

    private Map<String, CatalogNodeEntity> findAllNodes() {
        List<Integer> nodeIds = nodeRepository
                .listAllLatest(Constants.GLOBAL_CATALOG_COMPANY_ID);

        return nodeRepository
                .findAllById(nodeIds)
                .stream()
                .collect(Collectors.toMap(CatalogNodeEntity::getSnp, Function.identity()));
    }

    private Map<String, CatalogNodeEntity> findAllNodesIncludingParents(String snp) {
        List<Integer> nodeIds = nodeRepository
                .listLatestStartsWith(Constants.GLOBAL_CATALOG_COMPANY_ID, snp);

        nodeIds.addAll(findParentIds(snp));

        return nodeRepository
                .findAllById(nodeIds)
                .stream()
                .collect(Collectors.toMap(CatalogNodeEntity::getSnp, n -> n));
    }

    private List<Integer> findParentIds(String snp) {
        List<String> parentSnps = SnpHelper.parentSnps(snp);
        if (!parentSnps.isEmpty()) {
            return nodeRepository
                    .findLatestInSnps(parentSnps, Constants.GLOBAL_CATALOG_COMPANY_ID);
        }
        return Collections.emptyList();
    }

    private List<String> findAllActiveLeafSnps(Map<String, CatalogNodeEntity> nodes) {
        return nodes.values().stream()
                .filter(n -> SnpHelper.isLeafSnp(n.getSnp()) && parentsAndNodeIsActive(nodes, n.getSnp()))
                .map(CatalogNodeEntity::getSnp)
                .collect(Collectors.toList());
    }

    abstract List<LineVariation> loadAllVariations(Integer companyId, boolean showGlobal, List<String> leafSnps);

    private List<CatalogItemLine> convertToLine(List<LineVariation> variations, Map<String, CatalogNodeEntity> nodes) {
        return variations
                .stream()
                .map(lv -> CatalogItemLine
                        .toLine(mapParentNames(nodes,
                                lv.globalVariation),
                                lv.globalVariation,
                                lv.companyVariation))
                .collect(Collectors.toList());
    }

    private Map<String, String> mapParentNames(Map<String, CatalogNodeEntity> nodes, CatalogVariationEntity v) {
        return SnpHelper
                .parentSnps(v.getSnp())
                .stream()
                .collect(Collectors.toMap(snp -> snp, snp -> Optional.ofNullable(nodes.get(snp).getName()).orElse("")));
    }

    private boolean parentsAndNodeIsActive(Map<String, CatalogNodeEntity> nodes, String leafSnp) {
        List<String> snps = SnpHelper.parentSnps(leafSnp);
        snps.add(leafSnp);
        return snps.stream().allMatch(snp -> nodes.get(snp).getDisabled() == null);
    }

    private List<CatalogItemLine> sortLines(List<CatalogItemLine> lines) {
        lines.sort((l, r) -> SnpHelper
                .compare(l.getSnpAndVariationNumber(), r.getSnpAndVariationNumber()));
        return lines;
    }

    private static void writeItemsToCSV(List<CatalogItemLine> lines, PrintWriter writer) {
        try {
            CatalogItemLineWriter.writeItemsToCSV(lines, writer);
        } catch (IOException e) {
            throw CatalogCSVException.errorWritingCSV(e);
        }
    }

    @Builder
    @Getter
    @Setter
    protected static class LineVariation {

        private CatalogVariationEntity globalVariation;
        private CatalogVariationEntity companyVariation;

        protected boolean isAdditionalVariation() {
            return globalVariation == null && companyVariation != null;
        }
    }
}
