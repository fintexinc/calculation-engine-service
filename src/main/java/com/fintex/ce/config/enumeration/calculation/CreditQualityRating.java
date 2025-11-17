package com.fintex.ce.config.enumeration.calculation;

import lombok.Getter;

@Getter
public enum CreditQualityRating {

    AAA("AAA"),
    AA("AA"),
    A("A"),
    BBB("BBB"),
    BB("BB"),
    B("B"),
    BELOW_B("BelowB"),
    NOT_RATED("NotRated");

    private final String rating;

    CreditQualityRating(String rating) {
        this.rating = rating;
    }

    public static CreditQualityRating of(final String value) {
        for (CreditQualityRating qualityRating : values()) {
            if (qualityRating.name().equalsIgnoreCase(value) || qualityRating.rating.equalsIgnoreCase(value)) {
                return qualityRating;
            }
        }
        return null;
    }

}
