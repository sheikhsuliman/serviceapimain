package com.siryus.swisscon.api.catalog;

import java.util.Arrays;
import java.util.Objects;
import java.util.LinkedList;
import java.util.List;

public class SnpHelper {
    public static final String SNP_SEPARATOR = ".";
    public static final String SNP_SEPARATOR_RE = "\\" + SNP_SEPARATOR;

    public static final String VALID_SNP_RE =  "^[0-9]+(?:\\.[0-9]+){0,4}$";

    public static boolean isLeafSnp(String snp) {
        return snp.split(SNP_SEPARATOR_RE).length == Constants.CATALOG_LEAF_LEVEL;
    }

    public static boolean isValidSnp(String snp) {
        return snp != null && snp.matches(VALID_SNP_RE);
    }

    public static boolean isAncestor(String maybeAncestorSnp, String snp) {
        return snp.indexOf(maybeAncestorSnp + SNP_SEPARATOR) == 0;
    }

    public static String normalize(String snp) {
        if (snp.length() == 0) {
            return snp;
        }

        return Arrays.stream(snp.split(SNP_SEPARATOR_RE))
                .map( s -> String.valueOf(Integer.parseInt(s, 10)))
                .reduce(
                        (r, s) -> r.isBlank() ? s : r + SNP_SEPARATOR + s
                ).orElse("");
    }

    public static String parentSnp(String snp) {
        String[] snpParts = snp.split(SNP_SEPARATOR_RE);
        return snpParts.length <= 1 ? null : String.join(SNP_SEPARATOR, Arrays.copyOfRange(snpParts, 0, snpParts.length - 1));
    }

    public static int compare(String snp1, String snp2) {
        String normalizedSnp1 = normalize(snp1);
        String normalizedSnp2 = normalize(snp2);
        if (Objects.equals(normalizedSnp1, normalizedSnp2)) {
            return 0;
        }

        String[] snp1Parts = normalizedSnp1.length() == 0 ? new String[0] : normalize(normalizedSnp1).split(SNP_SEPARATOR_RE);
        String[] snp2Parts = normalizedSnp2.length() == 0 ? new String[0] : normalize(normalizedSnp2).split(SNP_SEPARATOR_RE);

        for( int i = 0; i < Math.min(snp1Parts.length, snp2Parts.length); i++ ) {
            Integer part1 = Integer.parseInt(snp1Parts[i]);
            Integer part2 = Integer.parseInt(snp2Parts[i]);

            int compareResult = part1.compareTo(part2);

            if (compareResult != 0) {
                return compareResult;
            }
        }

        return Integer.compare(snp1Parts.length, snp2Parts.length);

    }
    public static List<String> parentSnps(String snp) {
        List<String> parents = new LinkedList<>();
        String parent = parentSnp(snp);
        while(parent != null) {
            parents.add(parent);
            parent = parentSnp(parent);
        }
        return parents;
    }

    /**
     * We split the first 4 dots (so that we have 5 numbers)
     * The Levels are between 0 and 4
     * <p>
     * Example:
     * 123 = Level 0 (trade level)
     * 123.1 = Level 1 (chapter level)
     * 123.12.512 = Level 2 (variant level)
     * 123.12.512.522 = Level 3 (section level)
     * 123.12.512.522.12 = Level 4 (task level)
     */
    public static int getSnpNumberLevel(String snpNumber) {
        return snpNumber.split(SNP_SEPARATOR_RE, Constants.CATALOG_LEAF_LEVEL).length - 1;
    }

    public static List<String> getSnpParts(String snp) {
        return Arrays.asList(snp.split(SNP_SEPARATOR_RE));
    }

    public static String topSnp(String snp) {
        return getSnpParts(snp).get(0);
    }

}
