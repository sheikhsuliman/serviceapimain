package com.siryus.swisscon.api.file.image;

import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

@Getter
public class ImageThumbUtil {

    // Image restrictions for S3 full size picture
    public static final Integer MAX_HEIGHT = 4096;
    public static final Integer MAX_WIDTH = 4096;

    // Image restrictions for S3 medium size picture
    public static final Integer MEDIUM_MAX_HEIGHT = 800;
    public static final Integer MEDIUM_MAX_WIDTH = 800;

    // Image restrictions for S3 small size picture
    public static final Integer SMALL_MAX_HEIGHT = 128;
    public static final Integer SMALL_MAX_WIDTH = 128;

    // Key which is added to the original filename before the extension for the thumbnails
    public static final String MEDIUM_KEY = MEDIUM_MAX_HEIGHT + "x" + MEDIUM_MAX_WIDTH;
    public static final String SMALL_KEY = SMALL_MAX_HEIGHT + "x" + SMALL_MAX_WIDTH;


    public static String getSmallPath(String path) {
        return getThumbPath(path, SMALL_KEY);
    }

    public static String getMediumPath(String path) {
        return getThumbPath(path, MEDIUM_KEY);
    }

    private static String getThumbPath(String path, String thumbKey) {
        String extension = FilenameUtils.getExtension(path);

        if(StringUtils.isNotBlank(extension)) {
            String pathWithoutExtension = FilenameUtils.removeExtension(path);
            return pathWithoutExtension + "_" + thumbKey + ".png";
        }
        return path + "_" + thumbKey;
    }
}
