package com.siryus.swisscon.api.catalog.csvreader;

import com.siryus.swisscon.api.catalog.repos.CatalogNodeEntity;
import com.siryus.swisscon.api.general.unit.Unit;
import com.siryus.swisscon.api.catalog.Constants;
import com.siryus.swisscon.api.catalog.SnpHelper;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;
import com.siryus.swisscon.api.util.CustomStringUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.siryus.swisscon.api.util.CustomStringUtils.removeLeadingZeros;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CatalogItemLine {

    /**
     * global columns
     **/
    public static final int TRADE_COLUMN = 0;
    public static final int CHAPTER_COLUMN = 1;
    public static final int VARIANT_COLUMN = 2;
    public static final int SECTION_COLUMN = 3;
    public static final int TASK_COLUMN = 4;
    public static final int SUB_TASK_COLUMN = 5;
    public static final int TRADE_NAME_COLUMN = 6;
    public static final int CHAPTER_NAME_COLUMN = 7;
    public static final int VARIANT_NAME_COLUMN = 8;
    public static final int SECTION_NAME_COLUMN = 9;
    public static final int TASK_NAME_COLUMN = 10;
    public static final int SUB_TASK_NAME_COLUMN = 11;
    public static final int UNIT_COLUMN = 12;

    /**
     * catalog columns
     **/
    public static final int COMPANY_ACTIVE_COLUMN = 13;
    public static final int COMPANY_CHECKLIST_COLUMN = 14;
    public static final int COMPANY_PRICE_COLUMN = 15;
    public static final int COMPANY_TASK_NAME_COLUMN = 16;
    public static final int COMPANY_SUB_TASK_NAME_COLUMN = 17;
    public static final int COMPANY_UNIT_COLUMN = 18;

    private static final Map<Integer, CatalogItemLineColumn> GLOBAL_COLUMNS = new TreeMap<>();
    private static final Map<Integer, CatalogItemLineColumn> COMPANY_COLUMNS = new TreeMap<>();
    private static final Map<Integer, CatalogItemLineColumn> ALL_COLUMNS = new TreeMap<>();

    static {
        GLOBAL_COLUMNS.put(TRADE_COLUMN, new CatalogItemLineColumn(TRADE_COLUMN, "Trade", CatalogItemLine::setTrade, CatalogItemLine::getTrade, (v, m) -> getSnpPart(v.getSnp(), TRADE_COLUMN)));
        GLOBAL_COLUMNS.put(CHAPTER_COLUMN, new CatalogItemLineColumn(CHAPTER_COLUMN, "Chapter", CatalogItemLine::setChapter, CatalogItemLine::getChapter, (v, m) -> getSnpPart(v.getSnp(), CHAPTER_COLUMN)));
        GLOBAL_COLUMNS.put(VARIANT_COLUMN, new CatalogItemLineColumn(VARIANT_COLUMN, "Variant", CatalogItemLine::setVariant, CatalogItemLine::getVariant, (v, m) -> getSnpPart(v.getSnp(), VARIANT_COLUMN)));
        GLOBAL_COLUMNS.put(SECTION_COLUMN, new CatalogItemLineColumn(SECTION_COLUMN, "Section", CatalogItemLine::setSection, CatalogItemLine::getSection, (v, m) -> getSnpPart(v.getSnp(), SECTION_COLUMN)));
        GLOBAL_COLUMNS.put(TASK_COLUMN, new CatalogItemLineColumn(TASK_COLUMN, "Task", CatalogItemLine::setTask, CatalogItemLine::getTask, (v, m) -> getSnpPart(v.getSnp(), TASK_COLUMN)));
        GLOBAL_COLUMNS.put(SUB_TASK_COLUMN, new CatalogItemLineColumn(SUB_TASK_COLUMN, "Sub Task", CatalogItemLine::setSubTask, CatalogItemLine::getSubTask, (v, m) -> v.getVariationNumber().toString()));
        GLOBAL_COLUMNS.put(TRADE_NAME_COLUMN, new CatalogItemLineColumn(TRADE_NAME_COLUMN, "Trade Name", CatalogItemLine::setTradeName, CatalogItemLine::getTradeName, (v, m) -> m.get(TRADE_COLUMN)));
        GLOBAL_COLUMNS.put(CHAPTER_NAME_COLUMN, new CatalogItemLineColumn(CHAPTER_NAME_COLUMN, "Chapter Name", CatalogItemLine::setChapterName, CatalogItemLine::getChapterName, (v, m) -> m.get(CHAPTER_COLUMN)));
        GLOBAL_COLUMNS.put(VARIANT_NAME_COLUMN, new CatalogItemLineColumn(VARIANT_NAME_COLUMN, "Variant Name", CatalogItemLine::setVariantName, CatalogItemLine::getVariantName, (v, m) -> m.get(VARIANT_COLUMN)));
        GLOBAL_COLUMNS.put(SECTION_NAME_COLUMN, new CatalogItemLineColumn(SECTION_NAME_COLUMN, "Section Name", CatalogItemLine::setSectionName, CatalogItemLine::getSectionName, (v, m) -> m.get(SECTION_COLUMN)));
        GLOBAL_COLUMNS.put(TASK_NAME_COLUMN, new CatalogItemLineColumn(TASK_NAME_COLUMN, "Task Name", CatalogItemLine::setTaskName, CatalogItemLine::getTaskName, (v, m) -> v.getTaskName()));
        GLOBAL_COLUMNS.put(SUB_TASK_NAME_COLUMN, new CatalogItemLineColumn(SUB_TASK_NAME_COLUMN, "Sub Task Name", CatalogItemLine::setSubTaskName, CatalogItemLine::getSubTaskName, (v, m) -> v.getTaskVariation()));
        GLOBAL_COLUMNS.put(UNIT_COLUMN, new CatalogItemLineColumn(UNIT_COLUMN, "Unit", CatalogItemLine::setUnit, CatalogItemLine::getUnit, (v, m) -> v.getUnit() != null ? v.getUnit().getSymbol() : null));
    }

    static {
        COMPANY_COLUMNS.put(COMPANY_ACTIVE_COLUMN, new CatalogItemLineColumn(COMPANY_ACTIVE_COLUMN, "Company Active", CatalogItemLine::setCompanyActive, CatalogItemLine::getCompanyActive, (v, m) -> getLineBoolean(v.isActive())));
        COMPANY_COLUMNS.put(COMPANY_CHECKLIST_COLUMN, new CatalogItemLineColumn(COMPANY_CHECKLIST_COLUMN, "Company Checklist", CatalogItemLine::setCompanyChecklist, l -> CustomStringUtils.separateNewLineWithComma(l.getCompanyChecklist()), (v, m) -> v.getCheckList()));
        COMPANY_COLUMNS.put(COMPANY_PRICE_COLUMN, new CatalogItemLineColumn(COMPANY_PRICE_COLUMN, "Company Price", CatalogItemLine::setCompanyPrice, CatalogItemLine::getCompanyPrice, (v, m) -> CustomStringUtils.twoDecimalString(v.getPrice())));
        COMPANY_COLUMNS.put(COMPANY_TASK_NAME_COLUMN, new CatalogItemLineColumn(COMPANY_TASK_NAME_COLUMN, "Company Task", CatalogItemLine::setCompanyTaskName, CatalogItemLine::getCompanyTaskName, (v, m) -> v.getTaskName()));
        COMPANY_COLUMNS.put(COMPANY_SUB_TASK_NAME_COLUMN, new CatalogItemLineColumn(COMPANY_SUB_TASK_NAME_COLUMN, "Company Sub Task", CatalogItemLine::setCompanySubTaskName, CatalogItemLine::getCompanySubTaskName, (v, m) -> v.getTaskVariation()));
        COMPANY_COLUMNS.put(COMPANY_UNIT_COLUMN, new CatalogItemLineColumn(COMPANY_UNIT_COLUMN, "Company Unit", CatalogItemLine::setCompanyUnit, CatalogItemLine::getCompanyUnit, (v, m) -> v.getUnit() != null ? v.getUnit().getSymbol() : null));
    }

    static {
        ALL_COLUMNS.putAll(GLOBAL_COLUMNS);
        ALL_COLUMNS.putAll(COMPANY_COLUMNS);
    }


    public static final String SNP_NUMBER_SEPARATOR = ".";
    public static final String SNP_NUMBER_SEPARATOR_ESCAPED = "\\.";

    /**
     * global properties
     **/
    private String trade;
    private String chapter;
    private String variant;
    private String section;
    private String task;
    private String subTask;
    private String tradeName;
    private String chapterName;
    private String variantName;
    private String sectionName;
    private String taskName;
    private String subTaskName;
    private String unit;

    /**
     * company properties
     **/
    private String companyActive;
    private String companyChecklist;
    private String companyPrice;
    private String companyTaskName;
    private String companySubTaskName;
    private String companyUnit;

    public CatalogNodeEntity toNode() {
        return toNodeWithSnp(getSnpNumber());
    }

    public CatalogNodeEntity toNodeWithSnp(String snp) {
        return CatalogNodeEntity.builder()
                .snp(snp)
                .companyId(0)
                .name(getRightName(snp))
                .parentSnp(SnpHelper.parentSnp(snp))
                .build();
    }

    public CatalogVariationEntity toVariation(Integer companyId, Unit unit) {
        boolean isCompanyLine = isCompanyItemLine();
        String effectiveTaskName = !isCompanyLine ? getTaskName() : Optional.ofNullable(getCompanyTaskName()).orElse(getTaskName());
        String effectiveVariationName = !isCompanyLine ? getSubTaskName() : Optional.ofNullable(getCompanySubTaskName()).orElse(getSubTaskName());
        BigDecimal price = StringUtils.isNotBlank(getCompanyPrice()) ? new BigDecimal(getCompanyPrice()) : null;
        String checkList = CustomStringUtils.separateCommaWithNewLine(getCompanyChecklist());

        return CatalogVariationEntity.builder()
                .companyId(companyId)
                .catalogNodeId(null)
                .unit(unit)
                .snp(getSnpNumber())
                .taskName(effectiveTaskName)
                .taskVariation(effectiveVariationName)
                .variationNumber(getVariationNumber())
                .active(!isCompanyLine || getCompanyActive().equals(Constants.IS_ACTIVE_TRUE))
                .checkList(isCompanyLine ? checkList : null)
                .price(isCompanyLine ? price : null)
                .build();
    }

    public static CatalogItemLine toLine(Map<String, String> parentNames, CatalogVariationEntity globalVariation, CatalogVariationEntity companyVariation) {
        Map<Integer, String> levelNames = parentNames
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> SnpHelper.getSnpNumberLevel(e.getKey()), Map.Entry::getValue));
        CatalogItemLine line = CatalogItemLine.builder().build();
        GLOBAL_COLUMNS.values().forEach(column -> column.setValueFromVariation(line, globalVariation, levelNames));
        COMPANY_COLUMNS.values().forEach(column -> column.setValueFromVariation(line, companyVariation, levelNames));
        return line;
    }

    public static CatalogItemLine toLine(List<String> fields) {
        CatalogItemLine line = CatalogItemLine.builder().build();
        ALL_COLUMNS.values().forEach(c -> c.setValueFromFields(line, fields));
        return line;
    }

    public String[] toRecord() {
        return ALL_COLUMNS.values()
                .stream()
                .map(f -> f.getValueFromLine(this))
                .collect(Collectors.toList()).toArray(new String[]{});
    }

    private Integer getVariationNumber() {
        try {
            return Integer.valueOf(getSubTask());
        } catch (NumberFormatException e) {
            throw CatalogCSVException.snpPartNotNumericOrWithSpecialTokens(getSubTask());
        }
    }

    public boolean isNodeAndNotLeaf() {
        return StringUtils.isBlank(getSubTask()) && StringUtils.isBlank(getTask());
    }

    public boolean isVariation() {
        return StringUtils.isNotBlank(getSubTask());
    }

    public String getSnpNumber() {
        return Stream.of(
                removeLeadingZeros(getTrade()),
                removeLeadingZeros(getChapter()),
                removeLeadingZeros(getVariant()),
                removeLeadingZeros(getSection()),
                removeLeadingZeros(getTask()))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(SNP_NUMBER_SEPARATOR));
    }

    public boolean isCompanyItemLine() {
        return COMPANY_COLUMNS.values().stream()
                .anyMatch(column -> column.getValueFromLine(this) != null);
    }

    public static boolean isCompanyItemLine(List<String> fields) {
        return COMPANY_COLUMNS.keySet().stream()
                .anyMatch(index -> Optional
                        .ofNullable(CatalogItemLineColumn.getOrNull(fields, index))
                        .isPresent());
    }

    private static String getSnpPart(String snp, int index) {
        List<String> snpParts = SnpHelper.getSnpParts(snp);
        return snpParts.size() > index ? snpParts.get(index) : null;
    }

    public String getSnpAtLevel(int level) {
        if (GLOBAL_COLUMNS.get(level).getValueFromLine(this) == null) {
            return null;
        }
        List<String> snpParts = new LinkedList<>();
        for (int i = 0; i <= level; i++) {
            snpParts.add(GLOBAL_COLUMNS.get(i).getValueFromLine(this));
        }
        return String.join(SNP_NUMBER_SEPARATOR, snpParts);
    }

    public String getNameAtLevel(int level) {
        int nameLevel = level + Constants.CATALOG_LEAF_LEVEL + 1;
        return GLOBAL_COLUMNS.get(nameLevel).getValueFromLine(this);
    }

    public String getSnpAndVariationNumber() {
        return getSnpNumber() + SNP_NUMBER_SEPARATOR + getSubTask();
    }

    private static String getLineBoolean(boolean bool) {
        return bool ? Constants.IS_ACTIVE_TRUE : Constants.IS_ACTIVE_FALSE;
    }

    public static String[] getHeader() {
        return ALL_COLUMNS.values()
                .stream()
                .map(CatalogItemLineColumn::getHeader)
                .collect(Collectors.toList())
                .toArray(new String[]{});
    }

    private String getRightName(String snpNumber) {
        int nameColumnIndex = SnpHelper.getSnpNumberLevel(snpNumber) + Constants.CATALOG_LEAF_LEVEL + 1;
        return GLOBAL_COLUMNS.get(nameColumnIndex).getValueFromLine(this);
    }

    public boolean isRootLine() {
        return SnpHelper.getSnpNumberLevel(getSnpNumber()) == 0;
    }

}
