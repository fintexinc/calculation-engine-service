package com.fintex.ce.domain.calculation;

import com.fintex.ce.domain.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.response.TrailingTotalReturnsResDTO;
import com.fintex.ce.dto.response.core.TimeIntervalResDTO;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

import static com.fintex.ce.config.constant.BigDecimalConstants.TWELVE;
import static com.fintex.ce.util.DecimalUtils.divide;
import static com.fintex.ce.util.DecimalUtils.pow;
import static java.math.BigDecimal.ONE;

public class TrailingTotalReturnsCalculation extends PeriodCalculationAbstract<TrailingTotalReturnsResDTO, BigDecimal> {

    public TrailingTotalReturnsCalculation(final CalculationDTO input,
                                           final Set<String> defaultPeriods) {
        super(input, defaultPeriods);
    }


    @Override
    public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
        return calculatePeriodForNumberOfMonths(numberOfMonths, getPortfolioTotalReturns());
    }

    public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths,
                                                       final NavigableMap<LocalDate, BigDecimal> totalReturns) {
        if (numberOfMonths > totalReturns.size()) {
            return null;
        }
        final BigDecimal product = calculateProductForPeriod(numberOfMonths, totalReturns);
        if (numberOfMonths < 12) {
            return product.subtract(ONE);
        }
        final BigDecimal annualizedP = divide(TWELVE, BigDecimal.valueOf(numberOfMonths));
        return pow(product, annualizedP).subtract(ONE);
    }

    @Override
    public TrailingTotalReturnsResDTO defineResponseType(final Set<Pair<String, BigDecimal>> result) {
        final TrailingTotalReturnsResDTO trailingTotalReturnsResDTO = new TrailingTotalReturnsResDTO();
        final Set<TimeIntervalResDTO> timeIntervals = formTimeIntervalResDTO(result);
        trailingTotalReturnsResDTO.setTrailingTotalReturn(timeIntervals);
        return trailingTotalReturnsResDTO;
    }

}
