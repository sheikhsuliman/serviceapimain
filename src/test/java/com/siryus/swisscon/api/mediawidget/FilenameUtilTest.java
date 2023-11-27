package com.siryus.swisscon.api.mediawidget;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FilenameUtilTest {

    private static final String ABC = "abc.pdf";
    private static final String ABC1 = "abc(1).pdf";
    private static final String ABC2 = "abc(2).pdf";
    private static final String ABC3 = "abc(3).pdf";

    @Test
    public void Given_pdfFileNames_When_evaluateFilename_Then_success() {
        assertEquals(ABC, FilenameUtil.evaluateFilename(ABC, Collections.emptyList()));
        assertEquals(ABC1, FilenameUtil.evaluateFilename(ABC, Collections.singletonList(ABC)));
        assertEquals(ABC2, FilenameUtil.evaluateFilename(ABC, Arrays.asList(ABC, ABC1)));
        assertEquals(ABC3, FilenameUtil.evaluateFilename(ABC, Arrays.asList(ABC, ABC2)));
    }

    @Test
    public void Given_fileNamesWithMultipleExtensions_When_evaluateFilename_Then_success() {
        assertEquals("a.txt.abc(1).pdf", FilenameUtil.evaluateFilename("a.txt.abc(1).pdf", Collections.singletonList("a.txt.abc.pdf")));
        assertEquals("a.txt.abc(2).pdf", FilenameUtil.evaluateFilename("a.txt.abc(2).pdf", Arrays.asList("a.txt.abc.pdf", "a.txt.abc(1).pdf")));
    }

    @Test
    public void Given_fileNamesWithoutExtension_When_evaluateFilename_Then_success() {
        assertEquals("abc", FilenameUtil.evaluateFilename("abc", Collections.emptyList()));
        assertEquals("abc(1)", FilenameUtil.evaluateFilename("abc", Collections.singletonList("abc")));
        assertEquals("abc(2)", FilenameUtil.evaluateFilename("abc", Arrays.asList("abc", "abc(1)")));
    }

    @Test
    public void Given_fileNameWithIncrementNumber_When_evaluateFilename_Then_success() {
        assertEquals("image(1)(1).jpg", FilenameUtil.evaluateFilename("image(1).jpg", Collections.singletonList("image(1).jpg")));
    }

    @Test
    public void Given_fileNameWithoutBasename_When_evaluateFilename_Then_success() {
        assertEquals("(1).extension", FilenameUtil.evaluateFilename(".extension", Collections.singletonList(".extension")));
        assertEquals("(2).extension", FilenameUtil.evaluateFilename(".extension", Arrays.asList(".extension", "(1).extension")));
    }

}
