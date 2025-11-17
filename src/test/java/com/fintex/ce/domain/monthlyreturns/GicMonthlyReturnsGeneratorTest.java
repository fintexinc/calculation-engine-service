package com.fintex.ce.domain.monthlyreturns;

import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.config.enumeration.InterestFreq;
import com.fintex.ce.dto.holding.GicHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.model.redis.RMonthlyReturns;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.fintex.ce.util.DateTimeUtils.rangeWithLastDayOfMonth;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GicMonthlyReturnsGeneratorTest {

    static final BigDecimal RETURN_OF_2_PERCENT_ANNUAL = BigDecimal.valueOf(0.0016515813019202);
    static final BigDecimal RETURN_OF_10_PERCENT_ANNUAL = BigDecimal.valueOf(0.0079741404289038);
    static final BigDecimal RETURN_OF_5_PERCENT_SEMI_ANNUAL = BigDecimal.valueOf(0.0041239154651442);
    static final BigDecimal RETURN_OF_5_PERCENT_QUARTERLY = BigDecimal.valueOf(0.0041494251232543);
    static final BigDecimal RETURN_OF_5_PERCENT_MONTHLY = BigDecimal.valueOf(0.004166666666667);
    static final BigDecimal RETURN_OF_5_PERCENT_BI_MONTHLY = BigDecimal.valueOf(0.0041580220928044);
    static final BigDecimal RETURN_OF_5_PERCENT_WEEKLY = BigDecimal.valueOf(0.0041733490124376);
    static final BigDecimal RETURN_OF_5_PERCENT_BI_WEEKLY = BigDecimal.valueOf(0.0041713413111404);
    static final BigDecimal RETURN_OF_5_PERCENT_DAILY = BigDecimal.valueOf(0.0041750727376082);

    @Test
    void generateGicMonthlyReturns_2PercentAnnualFrequency() {
        //SETUP
        final var sut = new MonthlyReturnsGenerator();
        final var gicHolding = new GicHolding();
        gicHolding.setType(HoldingType.GIC);
        gicHolding.setClientIntRate(BigDecimal.valueOf(2));
        gicHolding.setInvestmentDate(LocalDate.of(2020, 5, 1));

        final HashMap<Holding, RMonthlyReturns> expected = getExpected(gicHolding, RETURN_OF_2_PERCENT_ANNUAL);

        //ACT
        final Map<Holding, RMonthlyReturns> actual = sut.generateGicMonthlyReturns(List.of(gicHolding));

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void generateGicMonthlyReturns_10PercentAnnualFrequency() {
        //SETUP
        final var sut = new MonthlyReturnsGenerator();
        final var gicHolding = new GicHolding();
        gicHolding.setType(HoldingType.GIC);
        gicHolding.setClientIntRate(BigDecimal.valueOf(10));

        final HashMap<Holding, RMonthlyReturns> expected = getExpected(gicHolding, RETURN_OF_10_PERCENT_ANNUAL);

        //ACT
        final Map<Holding, RMonthlyReturns> actual = sut.generateGicMonthlyReturns(List.of(gicHolding));

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void generateGicMonthlyReturns_5PercentSemiAnnualFrequency() {
        //SETUP
        final var sut = new MonthlyReturnsGenerator();
        final var gicHolding = new GicHolding();
        gicHolding.setType(HoldingType.GIC);
        gicHolding.setClientIntRate(BigDecimal.valueOf(5));
        gicHolding.setInterestFreq(InterestFreq.SEMI_ANNUAL);
        gicHolding.setInvestmentDate(LocalDate.of(2005, 2, 1));

        final HashMap<Holding, RMonthlyReturns> expected = getExpected(gicHolding, RETURN_OF_5_PERCENT_SEMI_ANNUAL);

        //ACT
        final Map<Holding, RMonthlyReturns> actual = sut.generateGicMonthlyReturns(List.of(gicHolding));

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void generateGicMonthlyReturns_5PercentQuarterlyFrequency() {
        //SETUP
        final var sut = new MonthlyReturnsGenerator();
        final var gicHolding = new GicHolding();
        gicHolding.setType(HoldingType.GIC);
        gicHolding.setClientIntRate(BigDecimal.valueOf(5));
        gicHolding.setInterestFreq(InterestFreq.QUARTERLY);
        gicHolding.setInvestmentDate(LocalDate.of(2018, 9, 28));

        final HashMap<Holding, RMonthlyReturns> expected = getExpected(gicHolding, RETURN_OF_5_PERCENT_QUARTERLY);

        //ACT
        final Map<Holding, RMonthlyReturns> actual = sut.generateGicMonthlyReturns(List.of(gicHolding));

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void generateGicMonthlyReturns_5PercentMonthlyFrequency() {
        //SETUP
        final var sut = new MonthlyReturnsGenerator();
        final var gicHolding = new GicHolding();
        gicHolding.setType(HoldingType.GIC);
        gicHolding.setClientIntRate(BigDecimal.valueOf(5));
        gicHolding.setInterestFreq(InterestFreq.MONTHLY);
        gicHolding.setInvestmentDate(LocalDate.of(2000, 3, 15));

        final HashMap<Holding, RMonthlyReturns> expected = getExpected(gicHolding, RETURN_OF_5_PERCENT_MONTHLY);

        //ACT
        final Map<Holding, RMonthlyReturns> actual = sut.generateGicMonthlyReturns(List.of(gicHolding));

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void generateGicMonthlyReturns_5PercentBiMonthlyFrequency() {
        //SETUP
        final var sut = new MonthlyReturnsGenerator();
        final var gicHolding = new GicHolding();
        gicHolding.setType(HoldingType.GIC);
        gicHolding.setClientIntRate(BigDecimal.valueOf(5));
        gicHolding.setInterestFreq(InterestFreq.BI_MONTHLY);
        gicHolding.setInvestmentDate(LocalDate.of(2020, 1, 1));

        final HashMap<Holding, RMonthlyReturns> expected = getExpected(gicHolding, RETURN_OF_5_PERCENT_BI_MONTHLY);

        //ACT
        final Map<Holding, RMonthlyReturns> actual = sut.generateGicMonthlyReturns(List.of(gicHolding));

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void generateGicMonthlyReturns_5PercentWeeklyFrequency() {
        //SETUP
        final var sut = new MonthlyReturnsGenerator();
        final var gicHolding = new GicHolding();
        gicHolding.setType(HoldingType.GIC);
        gicHolding.setClientIntRate(BigDecimal.valueOf(5));
        gicHolding.setInterestFreq(InterestFreq.WEEKLY);
        gicHolding.setInvestmentDate(LocalDate.of(2019, 7, 15));

        final HashMap<Holding, RMonthlyReturns> expected = getExpected(gicHolding, RETURN_OF_5_PERCENT_WEEKLY);

        //ACT
        final Map<Holding, RMonthlyReturns> actual = sut.generateGicMonthlyReturns(List.of(gicHolding));

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void generateGicMonthlyReturns_5PercentBiWeeklyFrequency() {
        //SETUP
        final var sut = new MonthlyReturnsGenerator();
        final var gicHolding = new GicHolding();
        gicHolding.setType(HoldingType.GIC);
        gicHolding.setClientIntRate(BigDecimal.valueOf(5));
        gicHolding.setInterestFreq(InterestFreq.BI_WEEKLY);
        gicHolding.setInvestmentDate(LocalDate.of(2020, 5, 31));

        final HashMap<Holding, RMonthlyReturns> expected = getExpected(gicHolding, RETURN_OF_5_PERCENT_BI_WEEKLY);

        //ACT
        final Map<Holding, RMonthlyReturns> actual = sut.generateGicMonthlyReturns(List.of(gicHolding));

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void generateGicMonthlyReturns_5PercentDailyFrequency() {
        //SETUP
        final var sut = new MonthlyReturnsGenerator();
        final var gicHolding = new GicHolding();
        gicHolding.setType(HoldingType.GIC);
        gicHolding.setClientIntRate(BigDecimal.valueOf(5));
        gicHolding.setInterestFreq(InterestFreq.DAILY);
        gicHolding.setInvestmentDate(LocalDate.of(2019, 6, 12));

        final HashMap<Holding, RMonthlyReturns> expected = getExpected(gicHolding, RETURN_OF_5_PERCENT_DAILY);

        //ACT
        final Map<Holding, RMonthlyReturns> actual = sut.generateGicMonthlyReturns(List.of(gicHolding));

        //VERIFY
        assertEquals(expected, actual);
    }

    private HashMap<Holding, RMonthlyReturns> getExpected(final GicHolding gicHolding, final BigDecimal gicReturnValue) {
        final HashMap<Holding, RMonthlyReturns> result = new HashMap<>();

        final TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>();
        rangeWithLastDayOfMonth(gicHolding.getInvestmentDate(), LocalDate.now())
                .forEach(localDate -> returns.put(localDate, gicReturnValue));

        final RMonthlyReturns rMonthlyReturns = new RMonthlyReturns();
        rMonthlyReturns.setHoldingType(HoldingType.GIC);
        rMonthlyReturns.setCurrency(gicHolding.getCurrency().name());
        rMonthlyReturns.setReturns(returns);

        result.put(gicHolding, rMonthlyReturns);
        return result;
    }

}
