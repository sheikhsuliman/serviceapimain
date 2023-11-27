package com.siryus.swisscon.api.catalog.csvreader;

import com.siryus.swisscon.api.exceptions.LocalizedReason;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;

import java.util.List;

import static com.siryus.swisscon.api.exceptions.LocalizedReason.ParameterValue.pv;

@SuppressWarnings("WeakerAccess")
public class CatalogCSVException {

    private static final String FIELDS = "fields";

    private static int e(int n) {
        return LocalizedResponseStatusException.CATALOG_CSV_ERROR_CODE + n;
    }

    public static final LocalizedReason ERROR_READING_CSV_FILE = LocalizedReason.like(e(1), "Error reading csv: {{message}}");
    public static final LocalizedReason ERROR_WRITING_CSV_FILE = LocalizedReason.like(e(2), "Error writing csv: {{message}}");
    public static final LocalizedReason FILE_IS_EMPTY = LocalizedReason.like(e(3), "Imported file is empty");
    public static final LocalizedReason FIRST_LINE_IS_NOT_TITLE = LocalizedReason.like(e(4), "First line needs to be the title line: {{fields}}");
    public static final LocalizedReason NAME_NOT_SET  = LocalizedReason.like(e(5), "The name of has to be set: {{fields}}");
    public static final LocalizedReason NAMES_ARE_NOT_THE_SAME  = LocalizedReason.like(e(6), "All the names of a column with the same snp number have to have the same: `{{snp}}`, `{{existingName}}`, `{{name}}`");
    public static final LocalizedReason UNIT_NOT_SET  = LocalizedReason.like(e(7), "The unit of a position has to be set: {{fields}}");
    public static final LocalizedReason NO_NUMBERS_AT_ALL_SET  = LocalizedReason.like(e(8), "No Number values set");
    public static final LocalizedReason NULL_VALUES_WITHIN_SNP_NUMBER  = LocalizedReason.like(e(9), "There are no null values allowed within the snp number: {{fields}}");
    public static final LocalizedReason SNP_PART_NOT_NUMERIC_OR_SPECIAL_TOKENS  = LocalizedReason.like(e(10), "This part of the snp number is not numeric or contains special tokens: {{snpPart}}");
    public static final LocalizedReason UNIT_WITH_SYMBOL_NOT_FOUND  = LocalizedReason.like(e(11), "Unit with symbol not found in DB: : {{symbol}}");
    public static final LocalizedReason POSITION_HAS_NO_UNIT  = LocalizedReason.like(e(12), "Position has no unit: {{snp}}");
    public static final LocalizedReason DUPLICATE_SNP_NUMBER  = LocalizedReason.like(e(13), "Duplicate snp number: {{snp}}");
    public static final LocalizedReason MULTIPLE_ROOT_ITEMS  = LocalizedReason.like(e(14), "We can only have one trade per catalog import");
    public static final LocalizedReason IS_ACTIVE_INVALID  = LocalizedReason.like(e(15), "isActive has to bet set to 1 or 0: {{fields}}");
    public static final LocalizedReason PRICE_INVALID  = LocalizedReason.like(e(16), "price should be a valid number: {{price}}");

    private CatalogCSVException() {
    }

    public static LocalizedResponseStatusException errorReadingCSVFile(Throwable e) {
        return LocalizedResponseStatusException.internalError(ERROR_READING_CSV_FILE.with(pv("message", e.getMessage())));
    }

    public static LocalizedResponseStatusException errorWritingCSV(Throwable e) {
        return LocalizedResponseStatusException.internalError(ERROR_WRITING_CSV_FILE.with(pv(FIELDS, e.getMessage())));
    }

    public static LocalizedResponseStatusException fileIsEmpty() {
        return LocalizedResponseStatusException.badRequest(FILE_IS_EMPTY.with());
    }

    public static LocalizedResponseStatusException firstLineIsNotTitleLine(List<String> fields) {
        return LocalizedResponseStatusException.badRequest(FIRST_LINE_IS_NOT_TITLE.with(pv(FIELDS, fields.toString())));
    }

    public static LocalizedResponseStatusException nameNotSet(List<String> fields) {
        return LocalizedResponseStatusException.badRequest(NAME_NOT_SET.with(pv(FIELDS, fields.toString())));
    }

    public static LocalizedResponseStatusException namesNotTheSame(String snp, String existingName, String name) {
        return LocalizedResponseStatusException.badRequest(NAMES_ARE_NOT_THE_SAME.with(pv("snp", snp), pv("existingName", existingName), pv("name", name)));
    }

    public static LocalizedResponseStatusException unitNotSet(List<String> fields) {
        return LocalizedResponseStatusException.badRequest(UNIT_NOT_SET.with(pv(FIELDS, fields.toString())));
    }

    public static LocalizedResponseStatusException noNumberAtAllSet() {
        return LocalizedResponseStatusException.badRequest(NO_NUMBERS_AT_ALL_SET.with());
    }

    public static LocalizedResponseStatusException nullValuesWithinSnpNumber(List<String> fields) {
        return LocalizedResponseStatusException.badRequest(NULL_VALUES_WITHIN_SNP_NUMBER.with(pv(FIELDS, fields.toString())));
    }

    public static LocalizedResponseStatusException snpPartNotNumericOrWithSpecialTokens(String snpPart) {
        return LocalizedResponseStatusException.badRequest(SNP_PART_NOT_NUMERIC_OR_SPECIAL_TOKENS.with(pv("snpPart", snpPart)));
    }

    public static LocalizedResponseStatusException unitWithSymbolNotFound(String symbol) {
        return LocalizedResponseStatusException.badRequest(UNIT_WITH_SYMBOL_NOT_FOUND.with(pv("symbol", symbol)));
    }

    public static LocalizedResponseStatusException positionHasNoUnit(String snp) {
        return LocalizedResponseStatusException.badRequest(POSITION_HAS_NO_UNIT.with(pv("snp", snp)));
    }

    public static LocalizedResponseStatusException duplicateSnpNumber(String snp) {
        return LocalizedResponseStatusException.badRequest(DUPLICATE_SNP_NUMBER.with(pv("snp", snp)));
    }

    public static LocalizedResponseStatusException multipleRootItems() {
        return LocalizedResponseStatusException.badRequest(MULTIPLE_ROOT_ITEMS.with());
    }

    public static LocalizedResponseStatusException isActiveIsNotSetOrNotOneOrZero(List<String> fields) {
        return LocalizedResponseStatusException.badRequest(IS_ACTIVE_INVALID.with(pv(FIELDS, fields.toString())));
    }

    public static LocalizedResponseStatusException priceIsNotANumber(String price) {
        return LocalizedResponseStatusException.badRequest(PRICE_INVALID.with(pv("price", price)));
    }

}
