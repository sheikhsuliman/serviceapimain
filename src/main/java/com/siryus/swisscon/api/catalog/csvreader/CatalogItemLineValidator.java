package com.siryus.swisscon.api.catalog.csvreader;

import com.siryus.swisscon.api.catalog.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.siryus.swisscon.api.catalog.csvreader.CatalogItemLineColumn.getOrNull;

public class CatalogItemLineValidator {

    /**
     * 5 snp parts + 1 variant number
     */
    private static final int SNP_PART_SIZE = 6;

    private CatalogItemLineValidator() {
        throw new IllegalStateException("Util class not instantiable");
    }

    public static void validateFirstLine(List<String> fields) {
        if (!firstFieldIsNotNumeric(fields)) {
            throw CatalogCSVException.firstLineIsNotTitleLine(fields);
        }
    }

    public static boolean firstFieldIsNotNumeric(List<String> fields) {
        String firstField = getOrNull(fields, 0);
        return !StringUtils.isNumeric(firstField);
    }

    public static boolean allFieldsAreEmpty(String[] fields) {
        return Arrays.stream(fields).allMatch(StringUtils::isBlank);
    }

    /**
     * Checks if there are no null values within the snp number.
     * For example 210. null . 50.50 would not be allowed and throw a {@link CatalogCSVException}
     * Also checks if the name is set
     */
    public static void validateItemLine(List<String> fields) {
        List<String> snpParts = Arrays.asList(
                getOrNull(fields, CatalogItemLine.TRADE_COLUMN),
                getOrNull(fields, CatalogItemLine.CHAPTER_COLUMN),
                getOrNull(fields, CatalogItemLine.VARIANT_COLUMN),
                getOrNull(fields, CatalogItemLine.SECTION_COLUMN),
                getOrNull(fields, CatalogItemLine.TASK_COLUMN),
                getOrNull(fields, CatalogItemLine.SUB_TASK_COLUMN)
        );

        validateSnpNumberPartsAreNumeric(snpParts);
        validateNotNullValues(fields, snpParts);
        validateName(fields, snpParts);
        validateUnit(fields);
        validateIsActive(fields);
        validatePrice(fields);
    }

    public static void validateItemLines(List<CatalogItemLine> lines) {
        checkDuplicateSnpNumbers(lines);
        checkMultipleRootLines(lines);
        checkAllNamesPerSnpAreTheSame(lines);
    }

    private static void checkDuplicateSnpNumbers(List<CatalogItemLine> lines) {
        for (CatalogItemLine line : lines) {
            long numberOfItemWithSameSnp = lines
                    .stream()
                    .filter(i -> line.getSnpAndVariationNumber().equals(i.getSnpAndVariationNumber())).count();

            if (numberOfItemWithSameSnp > 1) {
                throw CatalogCSVException.duplicateSnpNumber(line.getSnpNumber());
            }
        }
    }

    private static void checkMultipleRootLines(List<CatalogItemLine> lines) {
        long numberOfRootLines = lines.stream().filter(CatalogItemLine::isRootLine).count();
        if(numberOfRootLines > 1) {
            throw CatalogCSVException.multipleRootItems();
        }
    }

    private static void checkAllNamesPerSnpAreTheSame(List<CatalogItemLine> lines) {
        HashMap<String, String> names = new HashMap<>();

       for(CatalogItemLine line : lines) {
           for(int i = 0 ; i <= Constants.CATALOG_LEAF_LEVEL; i++) {
               String snpAtLevel = line.getSnpAtLevel(i);
               String name = line.getNameAtLevel(i);
               String existingName = names.get(snpAtLevel);
               if(existingName == null) {
                   names.put(snpAtLevel, name);
               } else if(!Objects.equals(existingName, name)) {
                   throw CatalogCSVException.namesNotTheSame(snpAtLevel, existingName, name);
               }
           }
       }
    }

    private static void validateName(List<String> fields, List<String> snpParts) {
        if (nameIsMissing(snpParts, fields)) {
            throw CatalogCSVException.nameNotSet(fields);
        }
    }

    /**
     * Reverse through list and check if the name field has a value
     * <p>
     * For example if the line is a section = index 3
     * Then the section name field has to be filled = index = 8 (3+5)
     */
    private static boolean nameIsMissing(List<String> snpStrings, List<String> fields) {
        for (int i = snpStrings.size(); i-- > 0; ) {
            if(isNotVariantOrSubTask(i)) {
                String snpPart = snpStrings.get(i);
                if (StringUtils.isNotBlank(snpPart)) {
                    int nameColumnIndex = i + SNP_PART_SIZE;
                    return StringUtils.isBlank(fields.get(nameColumnIndex));
                }
            }
        }
        return true;
    }

    private static boolean isNotVariantOrSubTask(int index) {
        return !(index == CatalogItemLine.VARIANT_COLUMN || index ==CatalogItemLine.SUB_TASK_COLUMN);
    }

    private static void validateUnit(List<String> fields) {
        boolean isCompanyVariation = CatalogItemLine.isCompanyItemLine(fields);
        boolean isVariation =  isCompanyVariation || StringUtils
                .isNotBlank(getOrNull(fields, CatalogItemLine.SUB_TASK_COLUMN));
        boolean hasNoCompanyUnit = StringUtils.isBlank(getOrNull(fields, CatalogItemLine.COMPANY_UNIT_COLUMN));
        boolean hasNoUnit = hasNoCompanyUnit && StringUtils.isBlank(getOrNull(fields, CatalogItemLine.UNIT_COLUMN));
        if (isVariation && hasNoUnit) {
            throw CatalogCSVException.unitNotSet(fields);
        }
    }

    private static void validateIsActive(List<String> fields) {
        boolean isCompanyLine = CatalogItemLine.isCompanyItemLine(fields);
        if(isCompanyLine) {
            String isActive = getOrNull(fields, CatalogItemLine.COMPANY_ACTIVE_COLUMN);
            boolean isBlank = StringUtils.isBlank(isActive);
            boolean incorrectTrueFormat = !isBlank && !isActive.equals(Constants.IS_ACTIVE_TRUE);
            boolean incorrectFalseFormat = !isBlank && !isActive.equals(Constants.IS_ACTIVE_FALSE);

            if(isBlank || (incorrectTrueFormat && incorrectFalseFormat)) {
                throw CatalogCSVException.isActiveIsNotSetOrNotOneOrZero(fields);
            }
        }
    }

    private static void validatePrice(List<String> fields) {
        String price = getOrNull(fields, CatalogItemLine.COMPANY_PRICE_COLUMN);
        boolean isActive = !Constants.IS_ACTIVE_FALSE.equals(getOrNull(fields, CatalogItemLine.COMPANY_ACTIVE_COLUMN));

        if(CatalogItemLine.isCompanyItemLine(fields) && isActive && !NumberUtils.isParsable(price)) {
            throw CatalogCSVException.priceIsNotANumber(Optional.ofNullable(price).orElse(""));
        }
    }

    private static void validateNotNullValues(List<String> fields, List<String> snpParts) {
        int firstNullIndex = IntStream
                .range(0, snpParts.size())
                .filter(i -> StringUtils.isBlank(snpParts.get(i)))
                .findFirst()
                .orElse(snpParts.size());

        int lastNotNullIndex = IntStream
                .range(0, snpParts.size())
                .filter(i -> StringUtils.isNotBlank(snpParts.get(i)))
                .reduce((first, second) -> second) // this will get you the last item
                .orElseThrow(CatalogCSVException::noNumberAtAllSet);

        if (lastNotNullIndex > firstNullIndex) {
            throw CatalogCSVException.nullValuesWithinSnpNumber(fields);
        }
    }

    private static void validateSnpNumberPartsAreNumeric(List<String> snpParts) {
        for (String snpPart : snpParts) {
            if (StringUtils.isNotBlank(snpPart) && !StringUtils.isNumeric(snpPart)) {
                throw CatalogCSVException.snpPartNotNumericOrWithSpecialTokens(snpPart);
            }
        }
    }

}
