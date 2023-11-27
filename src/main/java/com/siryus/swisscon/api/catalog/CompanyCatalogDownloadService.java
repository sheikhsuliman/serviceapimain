package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationRepository;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
class CompanyCatalogDownloadService extends AbstractCatalogDownloadService {

    @Autowired
    CompanyCatalogDownloadService(CatalogNodeRepository nodeRepository,
                                  CatalogVariationRepository variationRepository,
                                  CatalogValidator catalogValidator) {
        super(nodeRepository, variationRepository, catalogValidator);
    }

    void exportCompanyCatalog(Integer companyId, String scope, boolean showGlobal, PrintWriter writer) {
        super.exportCatalog(companyId, scope, showGlobal, writer);
    }

    @Override
    List<LineVariation> loadAllVariations(Integer companyId, boolean showGlobal, List<String> leafSnps) {
        List<CatalogVariationEntity> companyVariations = findVariationsByCompany(companyId, leafSnps);
        List<CatalogVariationEntity> globalVariations = findGlobalVariations(companyVariations, showGlobal, leafSnps);

        Map<String, LineVariation> lineVariations = new HashMap<>();

        companyVariations.forEach(v -> setLineVariations(lineVariations, v, true));
        globalVariations.forEach(v -> setLineVariations(lineVariations, v, false));

        return handleAdditionalCompanyVariations(lineVariations, globalVariations);
    }

    private List<CatalogVariationEntity> findVariationsByCompany(Integer companyId, List<String> leafSnps) {
        List<Integer> variationIds = variationRepository.findLatestInSnps(leafSnps, companyId);
        return variationRepository.findAllById(variationIds);
    }

    private List<CatalogVariationEntity> findGlobalVariations(List<CatalogVariationEntity> companyVariations,
                                                              boolean showGlobal, List<String> leafSnps) {
        if (showGlobal) {
            return findVariationsByCompany(Constants.GLOBAL_CATALOG_COMPANY_ID, leafSnps);
        }
        List<String> companySnps = companyVariations
                .stream()
                .map(CatalogVariationEntity::getSnp)
                .collect(Collectors.toList());
        return findVariationsByCompany(Constants.GLOBAL_CATALOG_COMPANY_ID, companySnps);
    }

    private void setLineVariations(Map<String, LineVariation> lineVars, CatalogVariationEntity var, boolean isCompany) {
        String fullSnp = var.getSnp() + SnpHelper.SNP_SEPARATOR + var.getVariationNumber();
        LineVariation lineVariation = lineVars.getOrDefault(fullSnp, LineVariation.builder().build());
        if (isCompany) {
            lineVariation.setCompanyVariation(var);
        } else {
            lineVariation.setGlobalVariation(var);
        }
        lineVars.put(fullSnp, lineVariation);
    }

    private List<LineVariation> handleAdditionalCompanyVariations(Map<String, LineVariation> lineVariations,
                                                                  List<CatalogVariationEntity> globalVariations) {
        Map<String, CatalogVariationEntity> globalVariationsMap = globalVariations.stream().collect(Collectors.toMap(CatalogVariationEntity::getSnp, Function.identity(),
                (existing, replacement) -> existing));


        return lineVariations.values()
                .stream()
                .peek(lv->setEmptyGlobalVariation(lv, globalVariationsMap))
                .collect(Collectors.toList());
    }

    private void setEmptyGlobalVariation(LineVariation lineVariation, Map<String, CatalogVariationEntity> globalVariationsMap) {
        if(lineVariation.isAdditionalVariation()) {
            CatalogVariationEntity companyVariation = lineVariation.getCompanyVariation();
            CatalogVariationEntity globalVariation = globalVariationsMap.get(companyVariation.getSnp());

            CatalogVariationEntity defaultGlobalVariation = lineVariation.getCompanyVariation()
                    .toBuilder()
                    .taskName(globalVariation.getTaskName())
                    .taskVariation(null)
                    .unit(null)
                    .price(null)
                    .unit(null)
                    .build();
            lineVariation.setGlobalVariation(defaultGlobalVariation);
        }
    }
}
