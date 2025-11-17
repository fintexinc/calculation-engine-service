package com.fintex.ce.domain.calculation;

import com.fintex.ce.domain.calculation.core.RollingAbstractCalculation;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.response.RollingSharpeRatioResDTO;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

public class RollingSharpeRatioCalculation extends RollingAbstractCalculation<RollingSharpeRatioResDTO> {

    private final SharpeRatioCalculation sharpeRatioCalculation;

    public RollingSharpeRatioCalculation(final CalculationDTO input,
                                         final Set<String> defaultPeriods,
                                         final SharpeRatioCalculation sharpeRatioCalculation) {
        super(input, defaultPeriods);
        this.sharpeRatioCalculation = sharpeRatioCalculation;
    }

    @Override
    public BigDecimal calculateRollingValue(final int numberOfMonths, final NavigableMap<LocalDate, BigDecimal> returns) {
        return sharpeRatioCalculation.calculatePeriodForNumberOfMonths(numberOfMonths, returns);
    }

    @Override
    public RollingSharpeRatioResDTO defineResponseType(final Set<Pair<String, NavigableMap<LocalDate, BigDecimal>>> result) {
        final var rollingSharpeRatioResDTO = new RollingSharpeRatioResDTO();
        final var rollingIntervalResDTOS = getRollingIntervalResDTOS(result);
        rollingSharpeRatioResDTO.setRollingSharpeRatio(rollingIntervalResDTOS);
        return rollingSharpeRatioResDTO;
    }

}
