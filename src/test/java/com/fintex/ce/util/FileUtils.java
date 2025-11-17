package com.fintex.ce.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

public class FileUtils {

    private FileUtils() {
    }

    public static String toText(final String path) {
        final InputStream resource = Objects.requireNonNull(FileUtils.class.getClassLoader().getResourceAsStream(path));
        return toText(resource);
    }

    private static String toText(final InputStream resource) {
        String text;
        try (Scanner scanner = new Scanner(resource, StandardCharsets.UTF_8.name())) {
            text = scanner.useDelimiter("\\A").next();
        }
        return text;
    }

}
