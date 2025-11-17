package com.fintex.ce.framework.common;

public enum EnvironmentProfiles {
    UAT("uat","Acceptance envrionemt , Application deployed in UAT"),
    DEV("dev", "Application deployed in DEV"),
    TEST("test", "Application deployed in TEST"),
    PROD("prod", "Application deployed in production"),

    private String name;
    private String description;

    EnvironmentProfiles(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
