package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.RollingStandardDeviationCalculation;
import com.fintex.ce.domain.calculation.StandardDeviationCalculation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.RollingCalculationReqDTO;
import com.fintex.ce.dto.response.RollingStandardDeviationResDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.RollingCalculationReqDtoValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

import static com.fintex.ce.util.DecimalUtils.OUTPUT_SCALE;

@Service
public class RollingStandardDeviationCalculationServiceImpl extends PeriodAbstractService<RollingStandardDeviationResDTO, RollingCalculationReqDTO> {

    public RollingStandardDeviationCalculationServiceImpl(
            final MonthlyReturnsService monthlyReturnsService,
            @Value("#{'${default.periods.rolling-calculations}'.split(',')}") final Set<String> defaultPeriods,
            final RollingCalculationReqDtoValidator requestValidator) {
        super(monthlyReturnsService, defaultPeriods, requestValidator);
    }

    @Override
    public RollingStandardDeviationResDTO perform(final RollingCalculationReqDTO reqDTO) {
        requestValidator.validate(reqDTO);
        final var rollingStandardDeviationCalculation = defineCalculationMethod(reqDTO);
        return rollingStandardDeviationCalculation.calculate(reqDTO.getRollingPeriods());
    }

    @Override
    protected RollingStandardDeviationCalculation defineCalculationMethod(final RollingCalculationReqDTO reqDTO) {
        final CalculationDTO input = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
        final var standardDeviationCalculation = new StandardDeviationCalculation<>(input, defaultPeriods).setScale(OUTPUT_SCALE);
        return new RollingStandardDeviationCalculation(input, defaultPeriods, standardDeviationCalculation);
    }

    @Override
    public CalculationDTO buildCalculationDto(final RollingCalculationReqDTO reqDTO, final ReturnFactorScale returnFactorScale) {
        final Returns monthlyReturns = monthlyReturnsService.getPortfolioMonthlyReturns(
                reqDTO.getHoldings(), reqDTO.getCurrency(), returnFactorScale);

        final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns =  monthlyReturnsService
                .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, reqDTO.getCustomPsd(), reqDTO.getCustomPed());

        return new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns);
    }

}
