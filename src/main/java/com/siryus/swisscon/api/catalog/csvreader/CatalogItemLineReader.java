package com.siryus.swisscon.api.catalog.csvreader;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.siryus.swisscon.api.catalog.Constants;
import org.apache.any23.encoding.TikaEncodingDetector;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CatalogItemLineReader {

    private CatalogItemLineReader() {
        throw new IllegalStateException("Util class not instantiable");
    }

    public static List<CatalogItemLine> toCatalogItemLineFromInput(InputStream inputStream) {
        InputStreamReader inputStreamReader = loadReader(inputStream);
        List<String[]> lines = loadLines(inputStreamReader);
        List<CatalogItemLine> catalogItemLines = retrieveCatalogLines(lines);
        CatalogItemLineValidator.validateItemLines(catalogItemLines);
        return catalogItemLines;
    }

    private static InputStreamReader loadReader(InputStream inputStream) {
        try {
            InputStream bufferedIS = new BufferedInputStream(inputStream);
            Charset charset = Charset.forName(new TikaEncodingDetector().guessEncoding(bufferedIS));
            bufferedIS.reset();
            return new InputStreamReader(Objects.requireNonNull(bufferedIS), charset);
        } catch (IOException e) {
            throw CatalogCSVException.errorReadingCSVFile(e);
        }
    }

    private static List<CatalogItemLine> retrieveCatalogLines(List<String[]> lines) {
        try {
            CatalogItemLineValidator.validateFirstLine(getFields(lines.get(0)));
            removeTitleLines(lines);
            removeEmptyLines(lines);
            return lines.stream().map(CatalogItemLineReader::retrieveLine)
                    .collect(Collectors.toList());
        } catch (IndexOutOfBoundsException e) {
            throw CatalogCSVException.fileIsEmpty();
        }
    }

    private static void removeTitleLines(List<String[]> lines) {
        for (Iterator<String[]> i = lines.iterator(); i.hasNext(); ) {
            if (CatalogItemLineValidator.firstFieldIsNotNumeric(getFields(i.next()))) {
                i.remove();
            } else {
                return;
            }
        }
    }

    private static void removeEmptyLines(List<String[]> lines) {
        lines.removeIf(CatalogItemLineValidator::allFieldsAreEmpty);
    }

    private static CatalogItemLine retrieveLine(String[] line) {

        List<String> fields = getFields(line);

        CatalogItemLineValidator.validateItemLine(fields);

        return CatalogItemLine.toLine(fields);
    }

    private static List<String> getFields(String[] line) {
        return Arrays.stream(line)
                .collect(Collectors.toList());
    }

    private static List<String[]> loadLines(Reader reader) {
        CSVParser parser = new CSVParserBuilder().withSeparator(Constants.CSV_SEPARATOR).build();

        CSVReader csvReader = new CSVReaderBuilder(reader)
                .withCSVParser(parser)
                .build();

        try {
            return csvReader.readAll();
        } catch (CsvException | IOException e) {
            throw CatalogCSVException.errorReadingCSVFile(e);
        }

    }

}
