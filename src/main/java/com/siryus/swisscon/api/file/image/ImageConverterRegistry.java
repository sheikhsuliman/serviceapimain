package com.siryus.swisscon.api.file.image;

import java.util.HashMap;
import java.util.Map;

public abstract class ImageConverterRegistry {
    public static final Map<String, ToImageConverter> converters = new HashMap<>();


    private static void registerConverter(ToImageConverter converter) {
        converter.mimeTypes().forEach(type->converters.put(type,converter));

    }

    static {
        registerConverter(new PDFToImageConverter());
    }
}

