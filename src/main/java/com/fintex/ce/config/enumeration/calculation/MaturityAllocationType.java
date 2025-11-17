package com.fintex.ce.config.enumeration.calculation;


import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Getter
public enum MaturityAllocationType {

    MORE_THAN_TWENTY_YEARS("More than twenty years"),
    UNDER_ONE_YEAR("Under one year"),
    ONE_TO_SEVEN_DAYS("One to seven days", 0, 7, UNDER_ONE_YEAR),
    EIGHT_TO_THIRTY_DAYS("Eight to thirty days", 0, 30, UNDER_ONE_YEAR),
    THIRTYONE_TO_NINTY_DAYS("Thirty one to ninty days", 0, 90, UNDER_ONE_YEAR),
    NINTYONE_TO_182_DAYS("Ninty one to 182 days", 0, 182, UNDER_ONE_YEAR),
    ONEHUNDREDANDEIGHTYTHREE_TO_364_DAYS("183 to 364 days", 0, 364, UNDER_ONE_YEAR),
    ONE_TO_THREE_YEARS("One to three years", 2),
    THREE_TO_FIVE_YEARS("Three to five years", 4),
    FIVE_TO_SEVEN_YEARS("Five to seven years", 6),
    SEVEN_TO_TEN_YEARS("Seven to ten years", 9),
    TEN_TO_FIFTEEN_YEARS("Ten to fifteen years", 14),
    FIFTEEN_TO_TWENTY_YEARS("Fifteen to twenty years", 19),
    TWENTY_TO_THIRTY_YEARS("Twenty to thirty years", 29, MORE_THAN_TWENTY_YEARS),
    MORE_THAN_THIRTY_YEARS("More than thirty years", MORE_THAN_TWENTY_YEARS);


    private final String name;
    private final Optional<MaturityAllocationType> displayType;
    private final int maxYears;
    private final int maxDays;

    MaturityAllocationType(String name) {
        this(name, 0, 0, null);
    }

    MaturityAllocationType(String name, MaturityAllocationType displayType) {
        this(name, 0, 0, displayType);
    }

    MaturityAllocationType(String name, int maxYears) {
        this(name, maxYears, 0, null);
    }

    MaturityAllocationType(String name, int maxYears, MaturityAllocationType displayType) {
        this(name, maxYears, 0, displayType);
    }

    MaturityAllocationType(String name, int maxYears, int maxDays, MaturityAllocationType displayType) {
        this.name = name;
        this.maxYears = maxYears;
        this.maxDays = maxDays;
        this.displayType = Optional.ofNullable(displayType);
    }

    public static MaturityAllocationType of(final String typeStr) {
        return Arrays.stream(values())
                .filter(value -> value.name().equalsIgnoreCase(typeStr))
                .findFirst()
                .orElse(null);
    }

    public static MaturityAllocationType of(final int years, final int days) {
        return Arrays.stream(values())
                .filter(type -> years <= type.getMaxYears())
                .filter(type -> years > 0 || days <= type.getMaxDays())
                .findFirst()
                .orElse(years > 29 ? MORE_THAN_THIRTY_YEARS : null);
    }

    public MaturityAllocationType getDisplayType() {
        return displayType.orElse(this);
    }

}

