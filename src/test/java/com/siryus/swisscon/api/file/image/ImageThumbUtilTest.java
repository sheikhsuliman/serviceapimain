package com.siryus.swisscon.api.file.image;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImageThumbUtilTest {

    private static final String THUMB_KEY =  "{THUMB_KEY}";

    @Test
    public void testFullPathWith2Dots() {
        String url = "https://sc-dev-private-storage.s3.amazonaws.com/Files/156/blabla.example.jpg";
        String urlExpected = "https://sc-dev-private-storage.s3.amazonaws.com/Files/156/blabla.example_" + THUMB_KEY + ".png";
        executeThumbPathTest(urlExpected, url);
    }

    @Test
    public void testFullPathWith1Dot() {
        String url = "https://www.example.com/test.pdf";
        String urlExpected = "https://www.example.com/test_" + THUMB_KEY + ".png";
        executeThumbPathTest(urlExpected, url);
    }

    @Test
    public void testFullPathWithoutExtension() {
        String url = "https://www.example.com/url";
        String urlExpected = "https://www.example.com/url_" + THUMB_KEY;
        executeThumbPathTest(urlExpected, url);
    }

    @Test
    public void testFullPathWithMulipleDots() {
        String url = "https://www.example.com/testfile......xxx";
        String urlExpected = "https://www.example.com/testfile....._" + THUMB_KEY + ".png";
        executeThumbPathTest(urlExpected, url);
    }


    private void executeThumbPathTest(String urlExpected, String url) {
        String urlSmall = ImageThumbUtil.getSmallPath(url);
        String urlMedium = ImageThumbUtil.getMediumPath(url);
        assertEquals(urlExpected.replace(THUMB_KEY, ImageThumbUtil.SMALL_KEY), urlSmall);
        assertEquals(urlExpected.replace(THUMB_KEY, ImageThumbUtil.MEDIUM_KEY), urlMedium);
    }

}
