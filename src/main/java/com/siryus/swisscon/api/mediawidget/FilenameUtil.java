package com.siryus.swisscon.api.mediawidget;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilenameUtil {

    private static final String DOT = ".";
    private static final String NUMBER_IN_BRACKETS = "\\(([0-9]*)\\)";

    /**
     * If a filename already exists we change the filename like this:
     *
     * original.pdf
     * original(1).pdf
     * original(2).pdf
     * ....
     */
    public static String evaluateFilename(String name, List<String> filenames) {
        if (nameIsNotUnique(name, filenames)) {
            return evaluateIncrementalFilename(name, filenames);
        }
        return name;
    }

    private static boolean nameIsNotUnique(String name, List<String> filenames) {
        return filenames.contains(name);
    }

    private static String evaluateIncrementalFilename(String name, List<String> filenames) {
        String extension = getExtensionWithDot(name);
        String prefix = FilenameUtils.getBaseName(name);

        Pattern pattern = Pattern.compile(prefix + NUMBER_IN_BRACKETS + extension);

        Integer index = filenames.stream()
                .map(pattern::matcher)
                .filter(Matcher::find)
                .map(m -> Integer.valueOf(m.group(1)))
                .reduce(Integer::max)
                .orElse(0);

        return prefix + "(" + ++index + ")" + extension;
    }

    private static String getExtensionWithDot(String name) {
        String extension = FilenameUtils.getExtension(name);
        return StringUtils.isBlank(extension) ? StringUtils.EMPTY : DOT + extension;
    }

}
