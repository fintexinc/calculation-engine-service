package com.fintex.ce.config.enumeration;

import com.fintex.ce.exception.SystemException;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.config.enumeration.Currency.CAD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrencyTest {

    @Test
    void of_checkResult() {
        //SETUP
        final var currency = CAD;

        //ACT
        final Currency actual = Currency.of(currency.name());

        //VERIFY
        assertEquals(CAD.name(), actual.name());
    }

    @Test
    void of_checkResult2() {
        //SETUP

        //ACT
        final SystemException actual = assertThrows(SystemException.class, () -> Currency.of("Zl"));
        //VERIFY
        assertEquals("Could not find such Currency Zl", actual.getMessage());
    }
}