package com.fintex.ce.config.enumeration;

import com.fintex.ce.exception.SystemException;
import com.fintex.ce.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public enum Frequency {
    ANNUAL(12),
    SEMI_ANNUAL(6),
    MONTHLY(1);

    private final int frequency;

    Frequency(int frequency) {
        this.frequency = frequency;
    }

    public static Frequency of(int frequency) {
        for (Frequency value : values()) {
            if (value.getFrequency() == frequency) {
                return value;
            }
        }
        String message = String.format("Could not find such Frequency %s", frequency);
        throw new SystemException(message, ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
