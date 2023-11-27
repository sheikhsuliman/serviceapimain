package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.csvreader.CatalogItemLine;
import com.siryus.swisscon.api.general.unit.UnitRepository;
import com.siryus.swisscon.api.catalog.dto.CatalogNodeRequest;
import com.siryus.swisscon.api.catalog.dto.CatalogImportReportDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogVariationDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogVariationsRequest;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationRepository;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeEntity;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeRepository;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.soa.EventsEmitter;
import com.siryus.swisscon.soa.notification.contract.NotificationEvent;
import com.siryus.swisscon.soa.notification.contract.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Service
public class GlobalCatalogUploadService extends AbstractCatalogUploadService {

    private final GlobalCatalogUpdater basicGlobalCatalogUpdater;
    private final CatalogNodeRepository nodeRepository;
    private final CatalogVariationRepository variationRepository;
    private final EventsEmitter eventsEmitter;
    private final SecurityHelper securityHelper;

    @Autowired
    public GlobalCatalogUploadService(GlobalCatalogUpdater basicGlobalCatalogUpdater, CatalogNodeRepository nodeRepository, CatalogVariationRepository variationRepository, UnitRepository unitRepository, EventsEmitter eventsEmitter, SecurityHelper securityHelper) {
        super(unitRepository);
        this.basicGlobalCatalogUpdater = basicGlobalCatalogUpdater;
        this.nodeRepository = nodeRepository;
        this.variationRepository = variationRepository;
        this.eventsEmitter = eventsEmitter;
        this.securityHelper = securityHelper;
    }

    @Transactional
    public CatalogImportReportDTO importCatalog(MultipartFile multipartFile) {

        List<CatalogItemLine> lines = readCatalogItems(multipartFile);

        List<CatalogNodeEntity> nodesFromLines = convertToNodes(lines);
        List<CatalogNodeEntity> allNecessaryNodes = addMissingParents(lines, nodesFromLines);
        List<CatalogVariationEntity> variations = convertToGlobalVariations(lines);

        List<String> existingNodeSnps = findExistingNodeSnps(allNecessaryNodes);
        List<String> existingVariationSnps = findExistingVariationSnps(variations);

        int editedNodes = editExistingNodes(existingNodeSnps, allNecessaryNodes);
        int editedLeafNodes = editExistingVariations(existingVariationSnps, variations);

        int addedNodes = addNewNodes(existingNodeSnps, allNecessaryNodes);
        int addedLeafNodes = addNewVariations(existingVariationSnps, variations);

        final CatalogImportReportDTO importCatalogDTO = createDTO(lines.size(),
                editedNodes,
                editedLeafNodes,
                addedNodes,
                addedLeafNodes);

        eventsEmitter.emitNotification(NotificationEvent.fromCompany(NotificationType.GLOBAL_CATALOG_IMPORTED,
                Constants.GLOBAL_CATALOG_COMPANY_ID,
                securityHelper.currentUserId(), Constants.GLOBAL_CATALOG_COMPANY_ID, Constants.GLOBAL_CATALOG_COMPANY_ID));

        return importCatalogDTO;
    }

    private List<CatalogVariationEntity> convertToGlobalVariations(List<CatalogItemLine> lines) {
        return super.convertToVariations(Constants.GLOBAL_CATALOG_COMPANY_ID, lines, CatalogItemLine::isVariation);
    }

    private List<CatalogNodeEntity> convertToNodes(List<CatalogItemLine> lines) {
        return lines.stream()
                .filter(CatalogItemLine::isNodeAndNotLeaf)
                .map(CatalogItemLine::toNode)
                .collect(Collectors.toList());
    }

    private List<CatalogNodeEntity> addMissingParents(List<CatalogItemLine> lines, List<CatalogNodeEntity> nodes) {
        Map<String, CatalogNodeEntity> nodeMap = nodes.stream().collect(Collectors.toMap(CatalogNodeEntity::getSnp, n -> n));

        lines.forEach(l -> {
            Consumer<String> createParentNodeIfAbsent = parentSnp -> nodeMap
                    .computeIfAbsent(parentSnp, p -> l.toNodeWithSnp(parentSnp));
            SnpHelper.parentSnps(l.getSnpNumber()).forEach(createParentNodeIfAbsent);
        });
        return new LinkedList<>(nodeMap.values());
    }

    private List<String> findExistingNodeSnps(List<CatalogNodeEntity> nodes) {
        if (!nodes.isEmpty()) {
            List<String> snps = nodes.stream().map(CatalogNodeEntity::getSnp).collect(Collectors.toList());
            List<Integer> existingIds = nodeRepository.findLatestInSnps(snps, Constants.GLOBAL_CATALOG_COMPANY_ID);
            return nodeRepository.findAllById(existingIds)
                    .stream()
                    .filter(n -> n.getDisabled() == null)
                    .map(CatalogNodeEntity::getSnp)
                    .collect(Collectors.toList());
        }
        return new LinkedList<>();
    }

    private List<String> findExistingVariationSnps(List<CatalogVariationEntity> variations) {
        if (!variations.isEmpty()) {
            List<String> snps = variations.stream().map(CatalogVariationEntity::getSnp).collect(Collectors.toList());
            List<Integer> existingIds = variationRepository.findLatestInSnps(snps, Constants.GLOBAL_CATALOG_COMPANY_ID);

            return variationRepository.findAllById(existingIds)
                    .stream()
                    .map(CatalogVariationEntity::getSnp)
                    .collect(Collectors.toList());
        }
        return new LinkedList<>();
    }

    private int editExistingNodes(List<String> existingSnps, List<CatalogNodeEntity> nodes) {
        return Math.toIntExact(nodes.stream()
                .filter(n -> existingSnps.contains(n.getSnp()))
                .peek(n -> basicGlobalCatalogUpdater.editCatalogNode(CatalogNodeRequest.from(n)))
                .count());
    }

    private int addNewNodes(List<String> existingSnps, List<CatalogNodeEntity> nodes) {
        return Math.toIntExact(nodes.stream().sorted((l, r) -> SnpHelper.compare(l.getSnp(), r.getSnp()))
                .filter(n -> !existingSnps.contains(n.getSnp()))
                .peek(n -> basicGlobalCatalogUpdater.addCatalogNode(CatalogNodeRequest.from(n)))
                .count());
    }

    private int addNewVariations(List<String> existingSnps, List<CatalogVariationEntity> variations) {
        Predicate<CatalogVariationEntity> filter = v -> !existingSnps.contains(v.getSnp());
        Consumer<CatalogVariationsRequest> persistFunction = basicGlobalCatalogUpdater::addCatalogLeafWithVariations;
        return modifyVariationsWithFilter(variations, filter, persistFunction);
    }

    private int editExistingVariations(List<String> existingSnps, List<CatalogVariationEntity> variations) {
        Predicate<CatalogVariationEntity> filter = v -> existingSnps.contains(v.getSnp());
        Consumer<CatalogVariationsRequest> persistFunction = basicGlobalCatalogUpdater::editCatalogVariations;
        return modifyVariationsWithFilter(variations, filter, persistFunction);
    }

    private int modifyVariationsWithFilter(List<CatalogVariationEntity> variations, Predicate<CatalogVariationEntity> filter, Consumer<CatalogVariationsRequest> persistFunction) {
        Map<String, List<CatalogVariationEntity>> snpVariationsMap = variations.stream()
                .filter(filter)
                .collect(Collectors.groupingBy(CatalogVariationEntity::getSnp));


        AtomicInteger count = new AtomicInteger();
        snpVariationsMap.forEach((snp, vars) -> {
            List<CatalogVariationDTO> variationDTOs = vars
                    .stream()
                    .sorted(Comparator.comparingInt(CatalogVariationEntity::getVariationNumber))
                    .map(CatalogVariationDTO::from).collect(Collectors.toList());

            CatalogVariationsRequest variationsRequest = CatalogVariationsRequest.builder()
                    .snp(snp)
                    .variations(variationDTOs)
                    .build();
            persistFunction.accept(variationsRequest);
            count.incrementAndGet();
        });
        return count.get();
    }

}
