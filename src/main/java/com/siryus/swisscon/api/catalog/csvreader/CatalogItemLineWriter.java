package com.siryus.swisscon.api.catalog.csvreader;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.siryus.swisscon.api.catalog.Constants;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class CatalogItemLineWriter {

    public static final String CSV_FILENAME_SUFFIX = "_catalog_export.csv";
    public static final String HEADER_KEY = "Content-Disposition";
    public static final String HEADER_VALUE = "attachment; filename=";
    public static final String CONTENT_TYPE = "application/csv";

    public static void writeItemsToCSV(List<CatalogItemLine> catalogItemLines, PrintWriter writer) throws IOException {
        CSVWriter csvWriter = new CSVWriter(
                writer,
                Constants.CSV_SEPARATOR,
                ICSVWriter.NO_QUOTE_CHARACTER,
                ICSVWriter.DEFAULT_ESCAPE_CHARACTER,
                ICSVWriter.DEFAULT_LINE_END);

        writeHeader(csvWriter);
        writeAllLineRecords(catalogItemLines, csvWriter);
        csvWriter.close();
    }

    public static PrintWriter prepareFileAndGetWriter(String scope, HttpServletResponse response) {
        try {
            String fileName = scope + CSV_FILENAME_SUFFIX;
            String headerValue = HEADER_VALUE + fileName;
            response.setContentType(CONTENT_TYPE);
            response.setHeader(HEADER_KEY, headerValue);
            return response.getWriter();
        } catch (IOException e) {
            throw CatalogCSVException.errorWritingCSV(e);
        }
    }

    private static void writeHeader(CSVWriter csvWriter) {
        String[] headerValues = CatalogItemLine.getHeader();
        csvWriter.writeNext(headerValues);
    }

    private static void writeAllLineRecords(List<CatalogItemLine> catalogItemLines, CSVWriter csvWriter) {
        catalogItemLines.forEach(l -> csvWriter.writeNext(l.toRecord()));
    }

}
