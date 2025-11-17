package com.fintex.ce.config.enumeration;

import com.fintex.ce.exception.SystemException;
import com.fintex.ce.exception.code.ErrorCode;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum InterestFreq {

    ANNUAL(BigDecimal.valueOf(1), BigDecimal.valueOf(12)),
    SEMI_ANNUAL(BigDecimal.valueOf(2), BigDecimal.valueOf(6)),
    QUARTERLY(BigDecimal.valueOf(4), BigDecimal.valueOf(3)),
    BI_MONTHLY(BigDecimal.valueOf(6), BigDecimal.valueOf(2)),
    MONTHLY(BigDecimal.valueOf(12), BigDecimal.ONE),
    BI_WEEKLY(BigDecimal.valueOf(26), BigDecimal.ONE),
    WEEKLY(BigDecimal.valueOf(52), BigDecimal.ONE),
    DAILY(BigDecimal.valueOf(365), BigDecimal.ONE);

    private final BigDecimal frequency;
    private final BigDecimal monthsInPeriod;

    InterestFreq(final BigDecimal frequency,
                 final BigDecimal monthsInPeriod) {
        this.frequency = frequency;
        this.monthsInPeriod = monthsInPeriod;
    }

    public static InterestFreq of(final BigDecimal frequency) {
        for (InterestFreq value : values()) {
            if (value.getFrequency().compareTo(frequency) == 0) {
                return value;
            }
        }
        final String message = String.format("Could not find such Gic Interest Freq %s", frequency);
        throw new SystemException(message, ErrorCode.INTERNAL_SERVER_ERROR);
    }

}
