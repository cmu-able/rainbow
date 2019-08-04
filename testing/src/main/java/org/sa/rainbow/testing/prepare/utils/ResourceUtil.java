package org.sa.rainbow.testing.prepare.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ResourceUtil {
    private ResourceUtil() {

    }

    private static final String TEMP_FILE_PREFIX = "rainbow-testing-";
    private static final String TEMP_FILE_SUFFIX = ".tmp";

    /**
     * Extracts a resource, save to a temp file, and return the temp file.
     *
     * @param name resource name
     * @return the file that contains extracted resource
     * @throws IOException if the resource cannot be extracted
     */
    public static File extractResource(String name) throws IOException {
        Path tempPath = Files.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
        File tempFile = tempPath.toFile();
        tempFile.deleteOnExit();
        try (InputStream inputStream = ResourceUtil.class.getResourceAsStream(name)) {
            if (inputStream == null) {
                throw new FileNotFoundException("cannot find " + name);
            }
            Files.copy(inputStream, tempPath, REPLACE_EXISTING);
        }
        return tempFile;
    }
}
