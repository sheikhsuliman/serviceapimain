package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.csvreader.CatalogItemLine;
import com.siryus.swisscon.api.catalog.csvreader.CatalogItemLineWriter;
import com.siryus.swisscon.api.util.CustomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CatalogItemLineWriterTest {

    @Test
    public void Given_ValidGlobalCatalogLine_When_WriteToCsv_Then_LineIsWrittenSuccessfullyIntoWriter() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();

        List<String> fields = Arrays.asList("100", "200", "300", "400", "500", "1", // snp numbers
                "trade 100", "group 200", "variant 300", "section 400", "task 500", "sub task 1", //names
                "m2");// unit
        CatalogItemLine line = CatalogItemLine.toLine(fields);

        CatalogItemLineWriter.writeItemsToCSV(Collections.singletonList(line), response.getWriter());


        String[] lines = getLines(response);
        assertHeader(lines);
        assertFields(lines, fields);
    }

    @Test
    public void Given_ValidCompanyCatalogLine_When_WriteToCsv_Then_LineIsWrittenSuccessfullyIntoWriter() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();

        List<String> fields = Arrays.asList("100", "200", "300", "400", "500", "1", // snp numbers
                "trade 100", "group 200", "variant 300", "section 400", "task 500", "sub task 1", //names
                "m2", // unit
                "1", "item_a\nitem_b", "20.00", "company task 500", "company sub task 1", "m3" // company values
                );
        CatalogItemLine line = CatalogItemLine.toLine(fields);

        CatalogItemLineWriter.writeItemsToCSV(Collections.singletonList(line), response.getWriter());


        String[] lines = getLines(response);
        assertHeader(lines);
        assertFields(lines, fields);
    }

    @Test
    public void Given_NoLines_When_WriteToCsv_Then_CSVHasOnlyOneHeader() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();

        CatalogItemLineWriter.writeItemsToCSV(new LinkedList<>(), response.getWriter());

        String[] lines = getLines(response);

        assertHeader(lines);
        assertEquals(1, lines.length);
    }

    private void assertFields(String[] lines, List<String> fieldList) {
        String[] fields = lines[1].split(String.valueOf(Constants.CSV_SEPARATOR));
        String[] expectedFields = fieldList.stream().map(field->{
            if (field.contains("\n")) {
                return CustomStringUtils.separateNewLineWithComma(field);
            }
            return field;
        }).collect(Collectors.toList())
                .toArray(new String[]{});

        assertArrayEquals(expectedFields, fields);
    }

    private void assertHeader(String[] lines) {
        String[] headers = lines[0].split(String.valueOf(Constants.CSV_SEPARATOR));
        String[] expectedHeaders = CatalogItemLine.getHeader();

        assertArrayEquals(expectedHeaders, headers);
    }

    private String[] getLines(MockHttpServletResponse response) throws UnsupportedEncodingException {
        return response.getContentAsString().split("\n");
    }


}
