package com.fintex.ce.framework.common.properties;



import com.fintex.ce.framework.common.EnvironmentProfiles;
import com.fintex.ce.framework.utils.Logger;
import com.fintex.ce.framework.utils.Utils;
import com.fintex.ce.framework.utils.YmlParser;
import joptsimple.internal.Strings;

import java.util.Collections;
import java.util.Map;

public abstract class PropertiesHolder {

    public static final EnvironmentProfiles ENVIRONMENT;
    public static final String RELATIVE_PATH;
    public static final Map<String, Object> PROPERTY_MAP;

    private static final String CONFIGURATION_FILE_NAME = "application";

    static {
        ENVIRONMENT = getTestEnvironment();
        RELATIVE_PATH =  formatPropertyLocation(ENVIRONMENT.getName());
        PROPERTY_MAP = loadProperties();
    }

    private static Map<String, Object> loadProperties() {
        final Map<String, Object> childProperties = YmlParser.parseYamlToMap(RELATIVE_PATH);
        final String parentPropertyPath = formatPropertyLocation("base");
        if (PropertiesHolder.class.getResource(parentPropertyPath) == null) {
            return Collections.unmodifiableMap(childProperties);
        }
        final Map<String, Object> parentPropertyMap = YmlParser.parseYamlToMap(parentPropertyPath);
        parentPropertyMap.forEach(childProperties::putIfAbsent);
        return Collections.unmodifiableMap(childProperties);
    }

    private static String formatPropertyLocation(String propertyName) {
        final String extension = ".yml";
        if (Strings.isNullOrEmpty(propertyName)) {
            // construct location of default application properties file
            return "/" + CONFIGURATION_FILE_NAME + extension;
        }
        return "/" + CONFIGURATION_FILE_NAME + "-" + propertyName + extension;
    }

    public static <T> T loadProperties(Class<T> aClass) {
        return YmlParser.parseYmlToObject(RELATIVE_PATH, aClass);
    }

    public static String startsWithRestApi(String path) {
        Logger.info("Trying to load property by YML path: {}", path);
        final Object o = PROPERTY_MAP.get("manual.rest.api." + path);
        Logger.info("Loaded property value {}", o);
        return (String) o;
    }

    public static String startsWithManual(String path) {
        Logger.info("Trying to load property by YML path: {}", path);
        final Object o = PROPERTY_MAP.get("manual." + path);
        Logger.info("Loaded property value {}", o);
        return (String) o;
    }

    public static String getPropertyAsString(String pathInFile) {
        Logger.info("Trying to load system property by YML path: {}", pathInFile);
        final Object o = PROPERTY_MAP.get(pathInFile);
        Logger.info("Loaded property value {}", o);
        return o + "";
    }

    /**
     * Get the ENVIRONMENT to execute the test test from the system property `EnvironmentProfiles`
     * If no values are passed, then the Default Test ENVIRONMENT will be used.
     *
     * @return EnvironmentProfiles
     * @throws IllegalArgumentException - if an invalid value of type EnvironmentProfiles is passed
     */
    private static EnvironmentProfiles getTestEnvironment() {
        String profile = "profile";
        String highlighterLine = "----------------------------------------------------------------------------------";
        Logger.info(highlighterLine);
        EnvironmentProfiles testEnvironment;
        String environment = System.getProperty(profile);

        if (environment == null) {
            testEnvironment = EnvironmentProfiles.TEST;
            Logger.warn("`{}` value is not passed in the command line, running the test in the default ENVIRONMENT -`{}`", profile, testEnvironment.getDescription());
            Logger.info(highlighterLine);
            return testEnvironment;
        }

        testEnvironment = Utils.lookUpEnum(EnvironmentProfiles.class, environment);
        if (testEnvironment == null) {
            throw new IllegalArgumentException("The value `" + environment + "` passed for the `" + profile + "` system property is invalid," +
                    "check the enum " + EnvironmentProfiles.class.getTypeName());
        }
        Logger.info("Test Environment set to - `{}` which is `{}`",testEnvironment.getName(), testEnvironment.getDescription());
        Logger.info(highlighterLine);
        return testEnvironment;
    }

}
