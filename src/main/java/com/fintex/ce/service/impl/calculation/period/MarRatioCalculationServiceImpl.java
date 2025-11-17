package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.Growth10KCalculation;
import com.fintex.ce.domain.calculation.MarRatioCalculation;
import com.fintex.ce.domain.calculation.MaxDrawdownCalculation;
import com.fintex.ce.domain.calculation.TrailingTotalReturnsCalculation;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.MARRatioResDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.MarRatioReqValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

@Service
public class MarRatioCalculationServiceImpl extends PeriodAbstractService<MARRatioResDTO, PeriodsReqDTO> {

    public static final Function<BigDecimal, BigDecimal> SCALE_FUNCTION = e -> e;

    public MarRatioCalculationServiceImpl(
            @Autowired final MonthlyReturnsService monthlyReturnsService,
            @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods,
            @Autowired final MarRatioReqValidator requestValidator) {
        super(monthlyReturnsService, defaultPeriods, requestValidator);
    }

    @Override
    public MARRatioResDTO perform(final PeriodsReqDTO reqDTO) {
        requestValidator.validate(reqDTO);
        final MarRatioCalculation calculationMethod = defineCalculationMethod(reqDTO);
        return calculationMethod.calculate(reqDTO.getPeriods());
    }

    protected MarRatioCalculation defineCalculationMethod(final PeriodsReqDTO reqDTO) {
        final CalculationDTO inputDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
        final var trailingTotalReturnsCalculation = new TrailingTotalReturnsCalculation(inputDTO, defaultPeriods);

        final var growth10KCalculation = new Growth10KCalculation(inputDTO.getWeightedAveragePortfolioReturns(), null, false);
        final TreeMap<LocalDate, BigDecimal> growth10K = new TreeMap<>();
        growth10KCalculation.setFirstGrowth10KValue(inputDTO.getWeightedAveragePortfolioReturns(), growth10K);
        growth10KCalculation.calculateGrowth10K(inputDTO.getWeightedAveragePortfolioReturns(), growth10K);

        final var maxDrawdownCalculation = new MaxDrawdownCalculation(inputDTO, defaultPeriods, growth10K, SCALE_FUNCTION);
        return new MarRatioCalculation(inputDTO, defaultPeriods, trailingTotalReturnsCalculation, maxDrawdownCalculation);
    }

}
