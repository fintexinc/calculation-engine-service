package com.fintex.ce.domain.calculation.formula;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static com.fintex.ce.util.TestConstants.LOCAL_DATE_NOW;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SumProductTest {

    @Test
    void sumProduct_oneItem() {
        //SETUP
        final SumProduct<Integer, Map<LocalDate, BigDecimal>> sumProduct = new SumProduct<>(Map.of(1, Map.of(LOCAL_DATE_NOW, ONE)), Map.of(1, Map.of(LOCAL_DATE_NOW.minusMonths(1), ONE)))
                .setMap2KeyFinder(date -> date.minusMonths(1));

        //ACT
        final Map<LocalDate, BigDecimal> actual = sumProduct.calculate();

        //VERIFY
        assertEquals(Map.of(LOCAL_DATE_NOW, ONE), actual);
    }

    @Test
    void sumProduct_twoItems() {
        //SETUP
        final SumProduct<Integer, Map<LocalDate, BigDecimal>> sumProduct = new SumProduct<>(
                Map.of(1,
                        Map.of(toLastDayOfMonth(LOCAL_DATE_NOW), ONE, toLastDayOfMonth(LOCAL_DATE_NOW.plusMonths(1)), ONE),
                        2,
                        Map.of(toLastDayOfMonth(LOCAL_DATE_NOW), ONE, toLastDayOfMonth(LOCAL_DATE_NOW.plusMonths(1)), TEN)
                ),
                Map.of(1,
                        Map.of(toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), TEN, toLastDayOfMonth(LOCAL_DATE_NOW), TEN),
                        2,
                        Map.of(toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), TEN, toLastDayOfMonth(LOCAL_DATE_NOW), TEN)
                )
        )
                .setMap2KeyFinder(date -> toLastDayOfMonth(date.minusMonths(1)));

        //ACT
        final Map<LocalDate, BigDecimal> actual = sumProduct.calculate();

        //VERIFY
        assertEquals(Map.of(toLastDayOfMonth(LOCAL_DATE_NOW), BigDecimal.valueOf(20), toLastDayOfMonth(LOCAL_DATE_NOW.plusMonths(1)), BigDecimal.valueOf(110)), actual);
    }

}