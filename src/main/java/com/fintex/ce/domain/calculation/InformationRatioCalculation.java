package com.fintex.ce.domain.calculation;

import com.fintex.ce.config.constant.BigDecimalConstants;
import com.fintex.ce.domain.calculation.core.BenchmarkWeightedAverageCalculation;
import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.response.InformationRatioResDTO;
import com.fintex.ce.dto.response.core.TimeIntervalResDTO;
import com.fintex.ce.util.DecimalUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.Set;

public class InformationRatioCalculation extends BenchmarkWeightedAverageCalculation<InformationRatioResDTO, BigDecimal> {

    private final TrailingTotalReturnsCalculation trailingTotalReturnsCalculation;
    private final TrackingErrorCalculation trackingErrorCalculation;

    public InformationRatioCalculation(final BenchmarkCalculationDTO input,
                                       final Set<String> defaultPeriods,
                                       final TrailingTotalReturnsCalculation trailingTotalReturnsCalculation,
                                       final TrackingErrorCalculation trackingErrorCalculation) {
        super(input, defaultPeriods);
        this.trailingTotalReturnsCalculation = trailingTotalReturnsCalculation;
        this.trackingErrorCalculation = trackingErrorCalculation;
    }

    @Override
    public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
        if (numberOfMonths > getBenchmarkTotalReturns().size()
                || numberOfMonths > getPortfolioTotalReturns().size()
                || numberOfMonths < BigDecimalConstants.TWELVE.intValue()) {
            return null;
        }

        final BigDecimal portfolioReturn = trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(numberOfMonths, getPortfolioTotalReturns());
        final BigDecimal benchmarkReturn = trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(numberOfMonths, getBenchmarkTotalReturns());
        final BigDecimal trackingError = trackingErrorCalculation.calculatePeriodForNumberOfMonths(numberOfMonths);

        return DecimalUtils.divide(portfolioReturn.subtract(benchmarkReturn), trackingError);
    }

    @Override
    public InformationRatioResDTO defineResponseType(final Set<Pair<String, BigDecimal>> periodAndInformationRatio) {
        final var result = new InformationRatioResDTO();
        final Set<TimeIntervalResDTO> timeIntervals = formTimeIntervalResDTO(periodAndInformationRatio);
        result.setTimeIntervalResDTOS(timeIntervals);
        return result;
    }
}
