package com.fintex.ce.domain.calculation.core;

import com.fintex.ce.config.constant.BigDecimalConstants;
import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.response.core.PeriodResDTO;
import com.fintex.ce.util.DecimalUtils;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;

import static com.fintex.ce.util.CalculationUtils.average;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.ZERO;

/**
 * Abstraction for Alpha and Beta calculations. Since the Alpha totally uses the result of Beta
 * and some functionality (Excess Portfolio Return, Avg Excess Portfolio Return, Excess Benchmark Return, Avg Excess Benchmark Return)
 * this class is an abstract implementation of Beta calc but in the same time is reusable for Alpha.
 *
 * @param <T> response type.
 */
public abstract class AlphaBetaCalculationAbstract<T extends PeriodResDTO> extends PortfolioBenchmarkCalculationAbstract<T> {

    protected AlphaBetaCalculationAbstract(final BenchmarkCalculationDTO input,
                                           final Set<String> periods,
                                           final NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn,
                                           final NavigableMap<LocalDate, BigDecimal> benchmarkExcessReturn) {
        super(input, periods, portfolioExcessReturn, benchmarkExcessReturn);
    }

    @Override
    public BigDecimal calculatePeriod(
            final SortedMap<LocalDate, BigDecimal> portfolioExcessReturnByPeriod,
            final SortedMap<LocalDate, BigDecimal> benchmarkExcessReturnByPeriod,
            final BigDecimal portfolioExcessAverage
    ) {
        final BigDecimal benchmarkExcessAverage = average(benchmarkExcessReturnByPeriod);
        return calculateBeta(portfolioExcessReturnByPeriod, benchmarkExcessReturnByPeriod, portfolioExcessAverage, benchmarkExcessAverage);
    }

    /**
     * calculates beta,
     *
     * @param portfolioExcessReturnByPeriod portfolio excess return values from start of period to the end
     * @param benchmarkExcessReturnByPeriod benchmark excess return values from start of period to the end
     * @param portfolioExcessAverage        average value of portfolioExcessReturnByPeriod
     * @param benchmarkExcessAverage        average value of benchmarkExcessReturnByPeriod
     * @return beta value
     */
    public BigDecimal calculateBeta(final SortedMap<LocalDate, BigDecimal> portfolioExcessReturnByPeriod,
                                    final SortedMap<LocalDate, BigDecimal> benchmarkExcessReturnByPeriod,
                                    final BigDecimal portfolioExcessAverage,
                                    final BigDecimal benchmarkExcessAverage) {
        final BigDecimal numerator = calculateNumerator(portfolioExcessReturnByPeriod, benchmarkExcessReturnByPeriod, portfolioExcessAverage, benchmarkExcessAverage);
        final BigDecimal denominator = calculateDenominator(benchmarkExcessReturnByPeriod, benchmarkExcessAverage);
        return toUserScale(DecimalUtils.divide(numerator, denominator));
    }

    /**
     * calculates numerator for beta formula.
     *
     * @param portfolioExcessReturnByPeriod portfolio excess return values from start of period to the end
     * @param benchmarkExcessReturnByPeriod benchmark excess return values from start of period to the end
     * @param portfolioExcessAverage        average value of portfolioExcessReturnByPeriod
     * @param benchmarkExcessAverage        average value of benchmarkExcessReturnByPeriod
     * @return numerator value
     */
    BigDecimal calculateNumerator(final SortedMap<LocalDate, BigDecimal> portfolioExcessReturnByPeriod,
                                  final SortedMap<LocalDate, BigDecimal> benchmarkExcessReturnByPeriod,
                                  final BigDecimal portfolioExcessAverage,
                                  final BigDecimal benchmarkExcessAverage) {
        return portfolioExcessReturnByPeriod.entrySet().stream().map(e -> e.getValue().subtract(portfolioExcessAverage)
                .multiply(benchmarkExcessReturnByPeriod.get(e.getKey()).subtract(benchmarkExcessAverage)))
                .reduce(ZERO, BigDecimal::add);
    }

    /**
     * calculates denominator for beta formula.
     *
     * @param benchmarkExcessReturnByPeriod benchmark excess return values from start of period to the end
     * @param benchmarkExcessAverage        average value of benchmarkExcessReturnByPeriod
     * @return denominator value
     */
    BigDecimal calculateDenominator(final SortedMap<LocalDate, BigDecimal> benchmarkExcessReturnByPeriod,
                                    final BigDecimal benchmarkExcessAverage) {
        return benchmarkExcessReturnByPeriod.values().stream()
                .map(bigDecimal -> DecimalUtils.pow(bigDecimal.subtract(benchmarkExcessAverage), BigDecimalConstants.TWO))
                .reduce(ZERO, BigDecimal::add);
    }

}
