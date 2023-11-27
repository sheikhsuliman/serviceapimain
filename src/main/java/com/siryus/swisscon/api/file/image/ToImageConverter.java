package com.siryus.swisscon.api.file.image;

import com.siryus.swisscon.api.file.file.FileData;

import java.awt.image.BufferedImage;
import java.util.List;


public abstract class ToImageConverter {

    public abstract List<String> mimeTypes();

    public abstract BufferedImage toImageFile(FileData file);
}
