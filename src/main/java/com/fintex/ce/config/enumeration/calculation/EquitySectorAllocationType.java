package com.fintex.ce.config.enumeration.calculation;

public enum EquitySectorAllocationType {

    BASIC_MATERIALS("Basic Materials"),
    COMMUNICATION_SERVICES("Communication Services"),
    CONSUMER_CYCLICAL("Consumer Cyclical"),
    CONSUMER_DEFENSIVE("Consumer Defensive"),
    ENERGY("Energy"),
    FINANCIAL_SERVICES("Financial Services"),
    HEALTHCARE("Healthcare"),
    INDUSTRIALS("Industrials"),
    REAL_ESTATE("Real Estate"),
    TECHNOLOGY("Technology"),
    UTILITIES("Utilities");

    private String name;

    EquitySectorAllocationType(String name) {
        this.name = name;
    }

    public static EquitySectorAllocationType of(final String name) {
        for (EquitySectorAllocationType value : values()) {
            if (value.name().equalsIgnoreCase(name) || value.name.equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

}
