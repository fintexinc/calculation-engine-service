package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.RollingTotalReturnsCalculation;
import com.fintex.ce.domain.calculation.TrailingTotalReturnsCalculation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.RollingCalculationReqDTO;
import com.fintex.ce.dto.response.RollingTotalReturnsResDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.RollingTotalReturnsCalculationReqValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

@Service
public class RollingTotalReturnsCalculationServiceImpl extends PeriodAbstractService<RollingTotalReturnsResDTO, RollingCalculationReqDTO> {
    public RollingTotalReturnsCalculationServiceImpl(
            final MonthlyReturnsService monthlyReturnsService,
            @Value("#{'${default.periods.rolling-calculations}'.split(',')}") final Set<String> defaultPeriods,
            final RollingTotalReturnsCalculationReqValidator requestValidator) {
        super(monthlyReturnsService, defaultPeriods, requestValidator);
    }

    @Override
    public RollingTotalReturnsResDTO perform(final RollingCalculationReqDTO reqDTO) {
        requestValidator.validate(reqDTO);
        final RollingTotalReturnsCalculation rollingTotalReturnsCalculation = defineCalculationMethod(reqDTO);
        return rollingTotalReturnsCalculation.calculate(reqDTO.getRollingPeriods());
    }

    @Override
    protected RollingTotalReturnsCalculation defineCalculationMethod(final RollingCalculationReqDTO reqDTO) {
        final CalculationDTO input = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
        final var trailingTotalReturnsCalculation = new TrailingTotalReturnsCalculation(input, defaultPeriods);
        return new RollingTotalReturnsCalculation(input, defaultPeriods, trailingTotalReturnsCalculation);
    }

    @Override
    public CalculationDTO buildCalculationDto(final RollingCalculationReqDTO reqDTO, final ReturnFactorScale returnFactorScale) {
        final Returns monthlyReturns = monthlyReturnsService.getPortfolioMonthlyReturns(
                reqDTO.getHoldings(), reqDTO.getCurrency(), returnFactorScale);

        final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = monthlyReturnsService
                .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, reqDTO.getCustomPsd(), reqDTO.getCustomPed());

        return new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns);
    }

}
