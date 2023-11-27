package com.siryus.swisscon.api.base.helpers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestUtil {

    private static final String URL_REGEX = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    public static ArrayList<String> extractUrlsFromString(String content) {
        ArrayList<String> result = new ArrayList<>();

        Matcher m = Pattern.compile(URL_REGEX).matcher(content);
        while (m.find()) {
            result.add(m.group());
        }

        return result;
    }
}
