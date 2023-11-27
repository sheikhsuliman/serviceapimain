package com.siryus.swisscon.api.file.image;

import com.siryus.swisscon.api.file.FileExceptions;
import com.siryus.swisscon.api.file.file.FileData;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ImageUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtil.class);


    private static final String IMAGE_JPEG = "image/jpeg";
    private static final String IMAGE_PNG = "image/png";
    private static final String IMAGE_GIF = "image/gif";
    private static final String IMAGE_X_MS_BMP = "image/x-ms-bmp";
    private static final String[] MIMES_IMAGE = {IMAGE_JPEG, IMAGE_PNG, IMAGE_GIF, IMAGE_X_MS_BMP};

    private static final Map<String, String> MIME_FORMATS = new HashMap<>();

    private ImageUtil() {
    }

    public static String getImageIoFormat(String contentType) {
        if (MIME_FORMATS.size() == 0) {
            MIME_FORMATS.put(IMAGE_JPEG, "jpeg");
            MIME_FORMATS.put(IMAGE_PNG, "png");
            MIME_FORMATS.put(IMAGE_GIF, "gif");
        }
        String format = MIME_FORMATS.get(contentType);
        // If other type, then use PNG as preview type
        if (format == null) {
            format = "png";
        }
        return format;
    }

    public static boolean isImage(String contentType) {
        return ArrayUtils.contains(MIMES_IMAGE, contentType.toLowerCase());
    }

    public static FileData convertToPngIfGif(FileData fileData) {
        // convert GIF to PNG
        if (IMAGE_GIF.equals(fileData.getContentType())) {
            FileData gifDto = fileData;

            File tempFile = createTempFile();
            try(
                InputStream in = new FileInputStream(fileData.getIn());
                FileOutputStream os = new FileOutputStream(tempFile)
            ) {
                // convert
                BufferedImage tmpImg = ImageIO.read(in);
                ImageIO.write(tmpImg, "PNG", os);
                // update FileDTO
                fileData = FileData.builder()
                        .contentLength(tempFile.length())
                        .contentType(IMAGE_PNG)
                        .in(tempFile)
                        .path(fileData.getPath()).build();

            } catch (Exception e) {
                throw FileExceptions.failedToConvertFile(fileData.getFilename());
            }
            LOGGER.debug("Converted GIF: {} to PNG: {}", gifDto, fileData);
        }
        return fileData;
    }

    private static File createTempFile() {
        try {
            return File.createTempFile("upload", "tmp");
        } catch (IOException e) {
            throw FileExceptions.internalError(e.getMessage());
        }
    }
}
