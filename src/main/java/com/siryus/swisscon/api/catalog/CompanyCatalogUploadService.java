package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.csvreader.CatalogItemLine;
import com.siryus.swisscon.api.catalog.dto.CatalogImportReportDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogNodeDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogVariationDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogVariationsRequest;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;
import com.siryus.swisscon.api.general.unit.UnitDTO;
import com.siryus.swisscon.api.general.unit.UnitRepository;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.soa.EventsEmitter;
import com.siryus.swisscon.soa.notification.contract.NotificationEvent;
import com.siryus.swisscon.soa.notification.contract.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
class CompanyCatalogUploadService extends AbstractCatalogUploadService {

    private final CompanyCatalogUpdater companyCatalogUpdater;
    private final EventsEmitter eventsEmitter;
    private final SecurityHelper securityHelper;

    @Autowired
    public CompanyCatalogUploadService(CompanyCatalogUpdater companyCatalogUpdater, UnitRepository unitRepository, EventsEmitter eventsEmitter, SecurityHelper securityHelper) {
        super(unitRepository);
        this.companyCatalogUpdater = companyCatalogUpdater;
        this.eventsEmitter = eventsEmitter;
        this.securityHelper = securityHelper;
    }

    @Transactional
    public CatalogImportReportDTO importCatalog(Integer companyId, MultipartFile multipartFile) {
        List<CatalogItemLine> lines = readCatalogItems(multipartFile);

        List<CatalogVariationEntity> variations = convertToCompanyVariations(companyId, lines);

        List<CatalogNodeDTO> catalogNodeDTOs = saveVariations(companyId, variations);

        final CatalogImportReportDTO catalogImportReportDTO = createDTO(lines.size(),
                0, catalogNodeDTOs.size(), 0, 0);

        eventsEmitter.emitNotification(NotificationEvent.fromCompany(NotificationType.COMPANY_CATALOG_IMPORTED,
                companyId, securityHelper.currentUserId(), companyId, companyId));

        return catalogImportReportDTO;
    }

    private List<CatalogVariationEntity> convertToCompanyVariations(Integer companyId, List<CatalogItemLine> lines) {
        return super.convertToVariations(companyId, lines, CatalogItemLine::isCompanyItemLine);
    }

    private List<CatalogNodeDTO> saveVariations(Integer companyId, List<CatalogVariationEntity> variations) {
        return convertToRequests(companyId, variations)
                .stream()
                .map(r -> companyCatalogUpdater.updateCatalogLeafVariations(companyId, r))
                .collect(Collectors.toList());
    }

    private List<CatalogVariationsRequest> convertToRequests(Integer companyId, List<CatalogVariationEntity> variations) {
        return mapCatalogVariations(variations)
                .entrySet()
                .stream()
                .map(entry -> convertToVariationRequest(entry.getKey(),
                        convertToVariationDTOs(companyId, entry.getValue())))
                .collect(Collectors.toList());
    }

    private CatalogVariationsRequest convertToVariationRequest(String snp, List<CatalogVariationDTO> variationDTOs) {
        return CatalogVariationsRequest.builder()
                .snp(snp)
                .variations(variationDTOs)
                .build();
    }

    private List<CatalogVariationDTO> convertToVariationDTOs(Integer companyId, List<CatalogVariationEntity> variations) {
        return variations.stream().map(v -> CatalogVariationDTO.builder()
                .active(v.isActive())
                .variationNumber(v.getVariationNumber())
                .description(v.getTaskName())
                .variant(v.getTaskVariation())
                .checkListItems(v.getCheckListItems())
                .unit(UnitDTO.from(v.getUnit()))
                .companyId(companyId)
                .price(v.getPrice())
                .build()).collect(Collectors.toList());
    }

    private Map<String, List<CatalogVariationEntity>> mapCatalogVariations(List<CatalogVariationEntity> variations) {
        Map<String, List<CatalogVariationEntity>> variationMap = new HashMap<>();
        variations.forEach(v -> {
            List<CatalogVariationEntity> vars = variationMap.getOrDefault(v.getSnp(), new ArrayList<>());
            vars.add(v);
            variationMap.put(v.getSnp(), vars);
        });
        return variationMap;
    }
}
