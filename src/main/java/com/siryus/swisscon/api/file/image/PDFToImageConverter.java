package com.siryus.swisscon.api.file.image;

import com.siryus.swisscon.api.file.FileExceptions;
import com.siryus.swisscon.api.file.file.FileData;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

public class PDFToImageConverter extends ToImageConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(PDFToImageConverter.class);
    private static final long MAX_MEMORY_USE_100_MB = 100L * 1024 * 1024;

    public List<String> mimeTypes() {
        List<String> types = new LinkedList<>();
        types.add("application/pdf");
        return types;
    }

    public BufferedImage toImageFile(FileData file) {
        LOGGER.info("Before - Memory usage - {} - total: {} free:{} max:{}",
                    file.getPath(),
                    formatFileSize(Runtime.getRuntime().totalMemory()),
                    formatFileSize(Runtime.getRuntime().freeMemory()),
                    formatFileSize(Runtime.getRuntime().maxMemory())
        );
        try (PDDocument document = PDDocument.load(file.getIn(), MemoryUsageSetting.setupMixed(MAX_MEMORY_USE_100_MB))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            return pdfRenderer.renderImageWithDPI(0, 72, ImageType.RGB);
        }
        catch (Exception e) {
            throw FileExceptions.failedToConvertFile(file.getFilename());
        }
        finally {
            LOGGER.info("After - Memory usage - {} - total: {} free:{} max:{}",
                        file.getPath(),
                        formatFileSize(Runtime.getRuntime().totalMemory()),
                        formatFileSize(Runtime.getRuntime().freeMemory()),
                        formatFileSize(Runtime.getRuntime().maxMemory())
            );
        }
    }

    private static String formatFileSize(long size) {
        String hrSize = null;

        double b = size;
        double k = size/1024.0;
        double m = ((size/1024.0)/1024.0);
        double g = (((size/1024.0)/1024.0)/1024.0);
        double t = ((((size/1024.0)/1024.0)/1024.0)/1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if ( t>1 ) {
            hrSize = dec.format(t).concat(" TB");
        } else if ( g>1 ) {
            hrSize = dec.format(g).concat(" GB");
        } else if ( m>1 ) {
            hrSize = dec.format(m).concat(" MB");
        } else if ( k>1 ) {
            hrSize = dec.format(k).concat(" KB");
        } else {
            hrSize = dec.format(b).concat(" Bytes");
        }

        return hrSize;
    }
}