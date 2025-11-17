package com.fintex.ce.framework.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.fintex.ce.framework.exceptions.InternalTestGeneralException;
import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintex.ce.framework.model.JSONObjectHolder;
import org.junit.platform.launcher.Launcher;


public class  FileUtils {

    /**
     * Get paths to json files even when files placed in JAR
     *
     * @param folderPath path to JAR file
     * @return list of paths
     * @throws IOException
     * @throws URISyntaxException
     */
    public static List<String> retrieveJsonFilenamesFromJarOrFolder(String folderPath) throws IOException, URISyntaxException {
        List<String> list = new ArrayList<>();
        final File jarFile = new File(folderPath);

        if (jarFile.isFile()) {
            // Run with JAR file
            try (final JarFile jar = new JarFile(jarFile)) {
                // gives ALL entries in jar
                final Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    final String name = entries.nextElement().getName();
                    // filter according to the path
                    if (name.startsWith(folderPath) && name.endsWith(".json")) {
                        list.add(name);
                    }
                }
            }
        } else {
            // Run with IDE
            final URL url = Launcher.class.getResource("/" + folderPath);
            if (url != null) {
                final File apps = new File(url.toURI());
                for (File file : apps.listFiles()) {
                    if (file.getName().endsWith(".json")) {
                        list.add(file.getName());
                    }
                }
            }
        }
        return list;
    }

    public static Object[][] getListOfFilenamesByPath(String folderPath) throws IOException, URISyntaxException {
        final List<String> jsonFilenames = FileUtils.retrieveJsonFilenamesFromJarOrFolder(folderPath);
        List<Object[]> list = new ArrayList<>();
        for (String jsonFilename : jsonFilenames) {
            final JSONObjectHolder jsonDTO = new ObjectMapper().readValue(FileUtils.class.getResourceAsStream("/" + folderPath + jsonFilename),
                    JSONObjectHolder.class);
            list.add(new Object[] { jsonDTO.getTitle(), jsonDTO.getData() });
        }
        return list.toArray(new Object[list.size()][]);
    }

    public static String getTextFromResourceFile(String filePath) {
        InputStream resourceAsStream = FileUtils.class.getResourceAsStream(filePath);
        try {
            String text = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
            if (text == null) {
                throw new InternalTestGeneralException("Could not find file by path: " + filePath);
            }
            return text;
        } catch (IOException e) {
            throw new InternalTestGeneralException("Could not find file by path: " + filePath, e);
        } finally {
            IOUtils.closeQuietly(resourceAsStream);
        }
    }

}
