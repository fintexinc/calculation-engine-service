package com.fintex.ce.domain.calculation;

import com.fintex.ce.domain.calculation.core.RollingAbstractCalculation;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.response.RollingTotalReturnsResDTO;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

public class RollingTotalReturnsCalculation extends RollingAbstractCalculation<RollingTotalReturnsResDTO> {

    private final TrailingTotalReturnsCalculation trailingTotalReturnsCalculation;

    public RollingTotalReturnsCalculation(final CalculationDTO input,
                                          final Set<String> defaultPeriods,
                                          final TrailingTotalReturnsCalculation trailingTotalReturnsCalculation) {
        super(input, defaultPeriods);
        this.trailingTotalReturnsCalculation = trailingTotalReturnsCalculation;
    }

    @Override
    public BigDecimal calculateRollingValue(final int numberOfMonths, final NavigableMap<LocalDate, BigDecimal> returns) {
        return trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(numberOfMonths, returns);
    }

    @Override
    public RollingTotalReturnsResDTO defineResponseType(final Set<Pair<String, NavigableMap<LocalDate, BigDecimal>>> result) {
        final var rollingTotalReturnsResDTO = new RollingTotalReturnsResDTO();
        final var rollingIntervalResDTOS = getRollingIntervalResDTOS(result);
        rollingTotalReturnsResDTO.setRollingTotalReturns(rollingIntervalResDTOS);
        return rollingTotalReturnsResDTO;
    }

}
