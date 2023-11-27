package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.repos.CatalogVariationRepository;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GlobalCatalogDownloadService extends AbstractCatalogDownloadService {

    @Autowired
    public GlobalCatalogDownloadService(CatalogNodeRepository nodeRepository, CatalogVariationRepository variationRepository, CatalogValidator catalogValidator) {
        super(nodeRepository, variationRepository, catalogValidator);
    }

    public void exportGlobalCatalog(String scope, PrintWriter writer) {
        exportCatalog(Constants.GLOBAL_CATALOG_COMPANY_ID, scope, true, writer);
    }

    @Override
    protected List<LineVariation> loadAllVariations(Integer companyId, boolean showGlobal, List<String> leafSnps) {
        List<Integer> variationIds = variationRepository.findLatestInSnps(leafSnps, companyId);

        return variationRepository.findAllById(variationIds).stream()
                .map(v-> LineVariation.builder().globalVariation(v).build())
                .collect(Collectors.toList());
    }
}
