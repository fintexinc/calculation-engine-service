package com.fintex.ce.domain.calculation.core;

import com.fintex.ce.config.constant.BigDecimalConstants;
import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.response.core.PeriodResDTO;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;

import static com.fintex.ce.util.CalculationUtils.average;

@Getter
abstract class PortfolioBenchmarkCalculationAbstract<T extends PeriodResDTO> extends BenchmarkWeightedAverageCalculation<T, BigDecimal>  {
    protected NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn;
    protected NavigableMap<LocalDate, BigDecimal> benchmarkExcessReturn;

    protected PortfolioBenchmarkCalculationAbstract(final BenchmarkCalculationDTO input,
                                                    final Set<String> periods,
                                                    final NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn,
                                                    final NavigableMap<LocalDate, BigDecimal> benchmarkExcessReturn) {
        super(input, periods);
        this.portfolioExcessReturn = portfolioExcessReturn;
        this.benchmarkExcessReturn = benchmarkExcessReturn;
    }

    @Override
    public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
        if (isNumberOfMonthsInvalid(numberOfMonths)) {
            return null;
        }
        final LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, getPortfolioTotalReturns());
        final SortedMap<LocalDate, BigDecimal> portfolioExcessReturnByPeriod = getSubMapByPeriodStartDate(periodStartDate, portfolioExcessReturn);
        final SortedMap<LocalDate, BigDecimal> benchmarkExcessReturnByPeriod = getSubMapByPeriodStartDate(periodStartDate, benchmarkExcessReturn);
        final BigDecimal portfolioExcessAverage = average(portfolioExcessReturnByPeriod);
        return calculatePeriod(portfolioExcessReturnByPeriod, benchmarkExcessReturnByPeriod, portfolioExcessAverage);
    }

    protected abstract BigDecimal calculatePeriod(
            final SortedMap<LocalDate, BigDecimal> portfolioExcessReturnByPeriod,
            final SortedMap<LocalDate, BigDecimal> benchmarkExcessReturnByPeriod,
            final BigDecimal portfolioExcessAverage);

    private boolean isNumberOfMonthsInvalid(final int numberOfMonths) {
        return numberOfMonths > getBenchmarkTotalReturns().size()
                || numberOfMonths > getPortfolioTotalReturns().size()
                || numberOfMonths > portfolioExcessReturn.size()
                || numberOfMonths > benchmarkExcessReturn.size()
                || numberOfMonths < BigDecimalConstants.TWELVE.intValue();
    }
}
