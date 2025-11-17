package com.fintex.ce.framework.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Strings;
import com.fintex.ce.framework.exceptions.InternalTestGeneralException;
import org.junit.platform.launcher.Launcher;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class YmlParser {

    private YmlParser() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> T parseYmlToObject(String relativePath, Class<T> aClass) {
        InputStream resourceAsStream = getYmlInputStream(relativePath);

        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(resourceAsStream, aClass);
        } catch (Exception e) {
            throw new InternalTestGeneralException("While parsing YML file", e);
        }
    }

    private static InputStream getYmlInputStream(String relativePath) {
        InputStream resourceAsStream = Launcher.class.getResourceAsStream(relativePath);
        if (resourceAsStream == null) {
            throw new InternalTestGeneralException("Can't find file <*.yml>");
        }
        return resourceAsStream;
    }

    /**
     * YML file parser.
     * Return only one document
     *
     * @param relativePath relative path to YML file
     * @return map of properties and their corresponding values
     */
    @SuppressWarnings("all")
    public static Map<String, Object> parseYamlToMap(String relativePath) {
        Logger.info("Getting properties from the file {}", relativePath);
        InputStream resourceAsStream = getYmlInputStream(relativePath);

        Yaml yaml = new Yaml();

        try (InputStream in = resourceAsStream) {
            // will be loaded only first document from YML file
            final Map<String, Object> loadedProperties = (Map<String, Object>) yaml.load(in);

            return format("", loadedProperties, new HashMap<>());
        } catch (Exception e) {
            throw new InternalTestGeneralException("Error occurred while parsing <.yml> document", e);
        }
    }

    /**
     * Format the incoming map to a new map with a new key
     *
     * @param name   key name
     * @param map    loaded map by YAML parser
     * @param newMap empty mutible map
     * @return new map
     * @newMapkey = full path to the poperty
     * Escape of using nested maps
     */
    @SuppressWarnings("all")
    private static Map<String, Object> format(String name, Map<String, Object> map, Map<String, Object> newMap) {
        map.forEach((key, value) -> {
            final String keyPath = Strings.isNullOrEmpty(name) ? key : name + "." + key;
            if (value instanceof LinkedHashMap) {
                format(keyPath, (Map<String, Object>) value, newMap);
            } else {
                newMap.putIfAbsent(keyPath, value);
            }
        });
        return newMap;
    }

}
