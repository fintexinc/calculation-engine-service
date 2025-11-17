package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.Growth10KCalculation;
import com.fintex.ce.domain.calculation.MaxDrawdownCalculation;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.MaxDrawdownResDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.MaxDrawdownReqValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

@Service
public class MaxDrawdownServiceImpl extends PeriodAbstractService<MaxDrawdownResDTO, PeriodsReqDTO> {

    public MaxDrawdownServiceImpl(
            final MonthlyReturnsService monthlyReturnsService,
            @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods,
            final MaxDrawdownReqValidator maxDrawdownReqValidator) {
        super(monthlyReturnsService, defaultPeriods, maxDrawdownReqValidator);
    }

    protected MaxDrawdownCalculation defineCalculationMethod(final PeriodsReqDTO reqDTO) {
        final CalculationDTO inputDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
        final var growth10KCalculation = new Growth10KCalculation(inputDTO.getWeightedAveragePortfolioReturns(), null, false);
        final NavigableMap<LocalDate, BigDecimal> growth10K = initializeGrowthOf10KMap(inputDTO, growth10KCalculation);
        return new MaxDrawdownCalculation(inputDTO, defaultPeriods, growth10K, DecimalUtils::toUserScale);
    }

    NavigableMap<LocalDate, BigDecimal> initializeGrowthOf10KMap(final CalculationDTO inputDTO,
                                                                 final Growth10KCalculation growth10KCalculation) {
        final NavigableMap<LocalDate, BigDecimal> growth10K = new TreeMap<>();
        if (!CollectionUtils.isEmpty(inputDTO.getWeightedAveragePortfolioReturns())) {
            growth10KCalculation.setFirstGrowth10KValue(inputDTO.getWeightedAveragePortfolioReturns(), growth10K);
            growth10KCalculation.calculateGrowth10K(inputDTO.getWeightedAveragePortfolioReturns(), growth10K);
        }
        return growth10K;
    }

    @Override
    public void addSpecificChecks(PeriodsReqDTO reqDTO) {
        // There are no specific checks for MaxDrawdownCalculation
    }
}
