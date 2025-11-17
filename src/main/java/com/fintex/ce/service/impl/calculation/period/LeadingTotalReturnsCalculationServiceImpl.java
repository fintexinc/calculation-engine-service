package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.LeadingTotalReturnsCalculation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.LeadingTotalReturnPeriodsReqDTO;
import com.fintex.ce.dto.response.LeadingTotalReturnsResDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.LeadingTotalReturnsReqValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

@Service
public class LeadingTotalReturnsCalculationServiceImpl extends PeriodAbstractService<LeadingTotalReturnsResDTO, LeadingTotalReturnPeriodsReqDTO> {

    public LeadingTotalReturnsCalculationServiceImpl(
            @Autowired final MonthlyReturnsService monthlyReturnsService,
            @Value("#{'${default.periods.leading-total-returns}'.split(',')}") final Set<String> defaultPeriods,
            @Autowired final LeadingTotalReturnsReqValidator requestValidator) {
        super(monthlyReturnsService, defaultPeriods, requestValidator);
    }

    @Override
    public LeadingTotalReturnsResDTO perform(final LeadingTotalReturnPeriodsReqDTO reqDTO) {
        requestValidator.validate(reqDTO);
        final LeadingTotalReturnsCalculation leadingTotalReturnsCalculation = defineCalculationMethod(reqDTO);
        return leadingTotalReturnsCalculation.calculate(reqDTO.getPeriods());
    }

    @Override
    protected LeadingTotalReturnsCalculation defineCalculationMethod(final LeadingTotalReturnPeriodsReqDTO reqDTO) {
        final CalculationDTO input = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
        return new LeadingTotalReturnsCalculation(input, defaultPeriods);
    }

    @Override
    public CalculationDTO buildCalculationDto(final LeadingTotalReturnPeriodsReqDTO reqDTO, final ReturnFactorScale returnFactorScale) {
        final Returns portfolioMonthlyReturns = monthlyReturnsService.getPortfolioMonthlyReturns(
                reqDTO.getHoldings(), reqDTO.getCurrency(), returnFactorScale);

        final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = portfolioMonthlyReturns
                .validateCpsd(reqDTO.getCustomPsd())
                .cutByPed()
                .cutByCpsdIfCpsdEmptyCutByPsd(reqDTO.getCustomPsd())
                .fxRatesApplied()
                .getWeightedAverage();

        return new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns);

    }

}
