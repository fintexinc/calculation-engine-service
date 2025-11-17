package com.fintex.ce.domain.calculation;

import com.fintex.ce.domain.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.response.MARRatioResDTO;
import com.fintex.ce.dto.response.core.TimeIntervalResDTO;
import com.fintex.ce.dto.response.maxdrawdown.MaxDrawdownDTO;
import com.fintex.ce.util.DecimalUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

import static com.fintex.ce.config.constant.BigDecimalConstants.TWELVE;
import static com.fintex.ce.util.DecimalUtils.abs;

public class MarRatioCalculation extends PeriodCalculationAbstract<MARRatioResDTO, BigDecimal> {

    private final TrailingTotalReturnsCalculation trailingTotalReturnsCalculation;
    private final MaxDrawdownCalculation maxDrawdownCalculation;

    public MarRatioCalculation(final CalculationDTO input,
                               final Set<String> defaultPeriods,
                               final TrailingTotalReturnsCalculation trailingTotalReturnsCalculation,
                               final MaxDrawdownCalculation maxDrawdownCalculation) {
        super(input, defaultPeriods);
        this.trailingTotalReturnsCalculation = trailingTotalReturnsCalculation;
        this.maxDrawdownCalculation = maxDrawdownCalculation;
    }

    @Override
    public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
        if (numberOfMonths < TWELVE.intValue()) {
            return null;
        }
        final BigDecimal trailingTRValue = trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(numberOfMonths);
        final MaxDrawdownDTO maxDrawdownDTO = maxDrawdownCalculation.calculatePeriodForNumberOfMonths(numberOfMonths);
        if (Objects.isNull(maxDrawdownDTO.getValue()) || maxDrawdownDTO.getValue().compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return DecimalUtils.divide(trailingTRValue, abs(maxDrawdownDTO.getValue()));
    }

    @Override
    public MARRatioResDTO defineResponseType(final Set<Pair<String, BigDecimal>> result) {
        final var marRatioResDTO = new MARRatioResDTO();
        final Set<TimeIntervalResDTO> timeIntervals = formTimeIntervalResDTO(result);
        marRatioResDTO.setMarRatio(timeIntervals);
        return marRatioResDTO;
    }

}