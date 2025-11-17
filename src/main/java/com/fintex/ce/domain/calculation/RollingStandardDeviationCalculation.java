package com.fintex.ce.domain.calculation;

import com.fintex.ce.domain.calculation.core.RollingAbstractCalculation;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.response.RollingStandardDeviationResDTO;
import com.fintex.ce.dto.response.core.PeriodResDTO;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

public class RollingStandardDeviationCalculation extends RollingAbstractCalculation<RollingStandardDeviationResDTO> {

    private final StandardDeviationCalculation<PeriodResDTO> standardDeviationCalculation;

    public RollingStandardDeviationCalculation(final CalculationDTO input,
                                               final Set<String> defaultPeriods,
                                               final StandardDeviationCalculation<PeriodResDTO> standardDeviationCalculation) {
        super(input, defaultPeriods);
        this.standardDeviationCalculation = standardDeviationCalculation;
    }

    @Override
    public BigDecimal calculateRollingValue(final int numberOfMonths, final NavigableMap<LocalDate, BigDecimal> returns) {
        return standardDeviationCalculation.calculatePeriodForNumberOfMonths(numberOfMonths, returns);
    }

    @Override
    public RollingStandardDeviationResDTO defineResponseType(final Set<Pair<String, NavigableMap<LocalDate, BigDecimal>>> result) {
        final var rollingStandardDeviationResDTO = new RollingStandardDeviationResDTO();
        final var rollingIntervalResDTOS = getRollingIntervalResDTOS(result);
        rollingStandardDeviationResDTO.setRollingStandardDeviation(rollingIntervalResDTOS);
        return rollingStandardDeviationResDTO;
    }

}
