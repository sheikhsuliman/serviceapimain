package com.siryus.swisscon.api.util;

import org.apache.commons.codec.Charsets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ResourceUtils {

    private ResourceUtils() {
        throw new IllegalStateException("Util class not instantiable");
    }

    /**
     * @param resourceFolder The folder name within the resources folder
     * @return the name + extension of all files within the folder
     * @throws IOException If an error loading the folder occurs
     */
    public static List<String> getFolderPaths(String resourceFolder) throws IOException {
        List<String> filePaths = new LinkedList<>();

        ClassLoader loader = ResourceUtils.class.getClassLoader();
        InputStream in = loader.getResourceAsStream(resourceFolder);

        if(in != null) { // in case folder is empty
            BufferedReader rdr = new BufferedReader(new InputStreamReader(Objects.requireNonNull(in), Charsets.UTF_8));
            String line;
            while ((line = rdr.readLine()) != null) {
                filePaths.add(line);
            }
            rdr.close();

            return filePaths;
        }
        return filePaths;
    }
}
