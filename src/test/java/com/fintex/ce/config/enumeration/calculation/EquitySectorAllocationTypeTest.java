package com.fintex.ce.config.enumeration.calculation;

import org.junit.jupiter.api.Test;

import static com.fintex.ce.config.enumeration.calculation.EquitySectorAllocationType.HEALTHCARE;
import static com.fintex.ce.config.enumeration.calculation.EquitySectorAllocationType.INDUSTRIALS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EquitySectorAllocationTypeTest {

    @Test
    void of_checkResult() {
        //SETUP
        final EquitySectorAllocationType expected = EquitySectorAllocationType.REAL_ESTATE;

        //ACT
        final EquitySectorAllocationType actual = EquitySectorAllocationType.of(expected.name());

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void of_checkResult2() {
        //SETUP
        final String type = INDUSTRIALS.getName();

        //ACT
        final EquitySectorAllocationType actual = EquitySectorAllocationType.of(type);

        //VERIFY
        assertEquals(INDUSTRIALS, actual);
    }

    @Test
    void of_checkResult3() {
        //SETUP
        final String region = "Energye";

        //ACT
        final EquitySectorAllocationType actual = EquitySectorAllocationType.of(region);

        //VERIFY
        assertNull(actual);
    }

    @Test
    void of_checkResult4() {
        //SETUP
        final String region = "HeaLthCaRE";

        //ACT
        final EquitySectorAllocationType actual = EquitySectorAllocationType.of(region);

        //VERIFY
        assertEquals(HEALTHCARE, actual);
    }
}