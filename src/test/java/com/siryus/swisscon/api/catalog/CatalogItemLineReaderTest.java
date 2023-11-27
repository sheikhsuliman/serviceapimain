package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.csvreader.CatalogItemLine;
import com.siryus.swisscon.api.catalog.csvreader.CatalogItemLineReader;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class CatalogItemLineReaderTest {

    @Test
    public void Given_CorrectCsv_When_Import_Then_Success() throws IOException {
        List<CatalogItemLine> catalogItemLines = importFile("correct.csv");
        assertEquals(11, catalogItemLines.size());
        CatalogItemLine variant = catalogItemLines.stream().filter(l -> l.getSnpAndVariationNumber().equals("102.0.80.31.100.1")).findFirst().orElseThrow();
        assertThat(variant.getSubTaskName(), containsString("äöü"));
    }

    @Test
    public void Given_CsvWith5TitleLines_When_Import_Then_Success() throws IOException {
        List<CatalogItemLine> catalogItemLines = importFile("correct_5_title_lines.csv");
        assertEquals(1, catalogItemLines.size());
    }

    @Test
    public void Given_CsvWithWindows1255Encoding_When_Import_Then_Success() throws IOException {
        List<CatalogItemLine> catalogItemLines = importFile("correct_windows_1255_encoding.csv");
        assertEquals(1, catalogItemLines.size());
        assertThat(catalogItemLines.get(0).getSubTaskName(), containsString("äöü"));
    }

    @Test
    public void Given_CsvWithDuplicateSnp_When_Import_Then_Throw() throws IOException {
        importFileWithException("duplicate_snp.csv",
                "Duplicate snp number",
                "102.0.30.31.100");
    }

    @Test
    public void Given_CsvWithInvalidSnp_When_Import_Then_Throw() throws IOException {
        importFileWithException("invalid_snp.csv",
                "There are no null values allowed within the snp number",
                "102, 0, , 31, 100");
    }

    @Test
    public void Given_CsvWithMultipleRoots_When_Import_Then_Throw() throws IOException {
        importFileWithException("multiple_roots.csv",
                "We can only have one trade per catalog import");
    }

    @Test
    public void Given_CsvWithMissingNameField_When_Import_Then_Throw() throws IOException {
        importFileWithException("no_name.csv",
                "The name of has to be set",
                "102, 0, ,");
    }

    @Test
    public void Given_CsvWithWrongRootNumber_When_Import_Then_Throw() throws IOException {
        importFileWithException("wrong_root_nr.csv",
                "This part of the snp number is not numeric or contains special tokens",
                "30.1");
    }

    @Test
    public void Given_CsvWithoutHeadline_When_Import_Then_Throw() throws IOException {
        importFileWithException("missing_headline.csv",
                "First line needs to be the title line",
                "102");
    }

    @Test
    public void Given_CsvWithoutUnitInPosition_When_Import_Then_Throw() throws IOException {
        importFileWithException("missing_unit.csv",
                "The unit of a position has to be set",
                "102, 0, 30, 31, 100, 1");
    }

    @Test
    public void Given_CsvWhichColumnHasDifferentTradeNamesForSameSnp_When_Import_Then_Throw() throws IOException {
        importFileWithException("different_trade_names.csv",
                "All the names of a column with the same snp number have to have the same",
                "Trade 102",
                "Trade 102 typo");
    }

    @Test
    public void Given_CsvWhichColumnHasDifferentGroupNamesForSameSnp_When_Import_Then_Throw() throws IOException {
        importFileWithException("different_group_names.csv",
                "All the names of a column with the same snp number have to have the same",
                "102.0",
                "Chapter 0",
                "Chapter 0 typo");
    }

    @Test
    public void Given_CsvWhichColumnHasDifferentVariantNamesForSameSnp_When_Import_Then_Throw() throws IOException {
        importFileWithException("different_variant_names.csv",
                "All the names of a column with the same snp number have to have the same",
                "102.0.30",
                "Variant 30",
                "Variant 30 typo");
    }

    @Test
    public void Given_CsvWhichColumnHasDifferentSectionNamesForSameSnp_When_Import_Then_Throw() throws IOException {
        importFileWithException("different_section_names.csv",
                "All the names of a column with the same snp number have to have the same",
                "102.0.30.31",
                "Section 31",
                "Section 31");
    }

    @Test
    public void Given_CsvWhichColumnHasDifferentTaskNamesForSameSnp_When_Import_Then_Throw() throws IOException {
        importFileWithException("different_task_names.csv",
                "All the names of a column with the same snp number have to have the same",
                "102.0.30.31.100",
                "Position 100",
                "Position 100 typo");
    }

    @Test
    public void Given_CsvWithEmptyLines_WhenImport_Then_IgnoreAndImportSuccess() throws IOException {
        List<CatalogItemLine> catalogItemLines = importFile("correct_with_empty_lines.csv");
        assertEquals(1, catalogItemLines.size());
    }

    @Test
    public void Given_EmptyCsv_When_Import_Then_Throw() throws IOException {
        importFileWithException("empty.csv",
                "Imported file is empty");
    }

    private List<CatalogItemLine> importFile (String file) throws IOException {
        return CatalogItemLineReader
                .toCatalogItemLineFromInput(readBytes(file));
    }

    private void importFileWithException(String file, String... msgParts) throws IOException {
        try {
            CatalogItemLineReader
                    .toCatalogItemLineFromInput(readBytes(file));
            fail("Exception expected");
        } catch (LocalizedResponseStatusException e) {
            Arrays.asList(msgParts)
                    .forEach(p -> assertThat(e.getMessage(), containsString(p)));
        }
    }

    private InputStream readBytes(String filename) throws IOException {
        String testFilePath = "/" + GlobalCatalogImportIT.CATALOGS_FOLDER + "/" + filename;

        byte[] bytes = IOUtils.toByteArray(getClass().getResourceAsStream(testFilePath));
        return new ByteArrayInputStream(bytes);
    }


}
