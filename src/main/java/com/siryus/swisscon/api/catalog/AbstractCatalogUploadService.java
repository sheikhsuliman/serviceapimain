package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.csvreader.CatalogCSVException;
import com.siryus.swisscon.api.catalog.csvreader.CatalogItemLine;
import com.siryus.swisscon.api.catalog.csvreader.CatalogItemLineReader;
import com.siryus.swisscon.api.catalog.csvreader.CatalogItemUnitEvaluator;
import com.siryus.swisscon.api.catalog.dto.CatalogImportReportDTO;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;
import com.siryus.swisscon.api.general.unit.Unit;
import com.siryus.swisscon.api.general.unit.UnitRepository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

abstract class AbstractCatalogUploadService {

    private final UnitRepository unitRepository;

    AbstractCatalogUploadService(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    protected List<CatalogItemLine> readCatalogItems(MultipartFile multipartFile) {
        try {
            return CatalogItemLineReader.toCatalogItemLineFromInput(multipartFile.getInputStream());
        } catch (IOException e) {
            throw CatalogCSVException.errorReadingCSVFile(e);
        }
    }

    protected List<CatalogVariationEntity> convertToVariations(Integer companyId, List<CatalogItemLine> lines, Predicate<CatalogItemLine> filter) {
        Map<String, Unit> allUnits = unitRepository.findAll().stream().collect(Collectors.toMap(Unit::getSymbol, u -> u));
        return lines.stream()
                .filter(filter)
                .map(line -> {
                    Unit unit = CatalogItemUnitEvaluator.evaluateUnit(line, allUnits);
                    return line.toVariation(companyId, unit);
                })
                .collect(Collectors.toList());
    }

    protected CatalogImportReportDTO createDTO(int lines, int editedNodes, int editedLeafNodes, int addedNodes, int addedLeafNodes) {
        return new CatalogImportReportDTO(
                Math.toIntExact(lines),
                Math.toIntExact(editedNodes),
                Math.toIntExact(editedLeafNodes),
                Math.toIntExact(addedNodes),
                Math.toIntExact(addedLeafNodes)
        );
    }
}
