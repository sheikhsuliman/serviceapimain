package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.exceptions.LocalizedReason;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;

import static com.siryus.swisscon.api.exceptions.LocalizedReason.ParameterValue.pv;

final class CatalogExceptions {
    private static int e(int n) {
        return LocalizedResponseStatusException.CATALOG_ERROR_CODE + n;
    }

    static final LocalizedReason CATALOG_NODE_ALREADY_EXISTS = LocalizedReason.like(e(1), "Catalog Node with SNP `{{snp}}` already exits");
    static final LocalizedReason CATALOG_NODE_DOES_NOT_EXIST = LocalizedReason.like(e(2), "Catalog Node with SNP `{{snp}}` does not exit");
    static final LocalizedReason ITEM_OR_ITS_ANCESTOR_DOES_NOT_EXIST = LocalizedReason.like(e(3), "Item or it's ancestor does not exist for SNP `{{snp}}`");
    static final LocalizedReason INVALID_SNP = LocalizedReason.like(e(4), "Invalid SNP `{{snp}}`");
    static final LocalizedReason CATALOG_NODE_ALREADY_DISABLED = LocalizedReason.like(e(5), "Catalog node `{{snp}}` already disabled for company {{companyId}}");
    static final LocalizedReason CATALOG_NODE_ALREADY_ENABLED = LocalizedReason.like(e(6), "Catalog node `{{snp}}` already enabled for company {{companyId}}");

    static final LocalizedReason COMPANY_VARIATION_HAS_TO_HAVE_UNIT = LocalizedReason.like(e(7), "Company variation `{{taskName}} - {{variation}}` has to have unit defined");
    static final LocalizedReason COMPANY_VARIATION_HAS_TO_HAVE_PRICE = LocalizedReason.like(e(8), "Company variation `{{taskName}} - {{variation}}` has to have price defined");
    static final LocalizedReason COMPANY_VARIATION_DOES_NOT_EXIST = LocalizedReason.like(e(9), "Company variation with snp `{{snp}} and variation number {{variationNumber}}` does not exist");

    private CatalogExceptions() {
    }

    static LocalizedResponseStatusException catalogNodeAlreadyExists(String snp) {
        return LocalizedResponseStatusException.badRequest(CATALOG_NODE_ALREADY_EXISTS.with(pv("snp", snp)));
    }

    static LocalizedResponseStatusException catalogNodeDoesNotExist(String snp) {
        return LocalizedResponseStatusException.badRequest(CATALOG_NODE_DOES_NOT_EXIST.with(pv("snp", snp)));
    }
    static LocalizedResponseStatusException itemOrItsAncestorDoesNotExist(String snp) {
        return LocalizedResponseStatusException.badRequest(ITEM_OR_ITS_ANCESTOR_DOES_NOT_EXIST.with(pv("snp", snp)));
    }

    static LocalizedResponseStatusException invalidSnp(String snp) {
        return LocalizedResponseStatusException.badRequest(INVALID_SNP.with(pv("snp", snp)));
    }

    static LocalizedResponseStatusException catalogNodeAlreadyDisabled(Integer companyId, String snp) {
        return LocalizedResponseStatusException.badRequest(CATALOG_NODE_ALREADY_DISABLED.with(pv("companyId", companyId), pv("snp", snp)));
    }

    static LocalizedResponseStatusException catalogNodeAlreadyEnabled(Integer companyId, String snp) {
        return LocalizedResponseStatusException.badRequest(CATALOG_NODE_ALREADY_ENABLED.with(pv("companyId", companyId), pv("snp", snp)));
    }

    public static LocalizedResponseStatusException companyVariationHasToHaveUnit(String taskName, String variation) {
        return LocalizedResponseStatusException.badRequest(COMPANY_VARIATION_HAS_TO_HAVE_UNIT.with(pv("taskName", taskName), pv("variation", variation)));
    }

    public static LocalizedResponseStatusException companyVariationHasToHavePrice(String taskName, String variation) {
        return LocalizedResponseStatusException.badRequest(COMPANY_VARIATION_HAS_TO_HAVE_PRICE.with(pv("taskName", taskName), pv("variation", variation)));
    }

    public static LocalizedResponseStatusException companyVariationDoesNotExist(String snp, Integer variationNumber) {
        return LocalizedResponseStatusException.badRequest(COMPANY_VARIATION_DOES_NOT_EXIST.with(pv("snp", snp), pv("variationNumber", variationNumber)));
    }

}
