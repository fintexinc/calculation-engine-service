package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.CorrelationCalculation;
import com.fintex.ce.domain.calculation.PeriodBasedCalculation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.CorrelationResDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.CorrelationReqValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

@Service
public class CorrelationServiceImpl extends PeriodAbstractService<CorrelationResDTO, PeriodsReqDTO> {

    public CorrelationServiceImpl(
            @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods,
            final CorrelationReqValidator requestValidator,
            final MonthlyReturnsService monthlyReturnsService) {
        super(monthlyReturnsService, defaultPeriods, requestValidator);
    }

    @Override
    public CorrelationResDTO perform(final PeriodsReqDTO reqDTO) {
        requestValidator.validate(reqDTO);
        final PeriodBasedCalculation<CorrelationResDTO> calculationMethod = defineCalculationMethod(reqDTO);
        return calculationMethod.calculate(reqDTO.getPeriods());
    }

    protected CorrelationCalculation defineCalculationMethod(final PeriodsReqDTO reqDTO) {
        reqDTO.setReqCurrencyToCashHolding();
        final Returns monthlyReturns = monthlyReturnsService.getPortfolioMonthlyReturns(
                reqDTO.getHoldings(), reqDTO.getCurrency(), ReturnFactorScale.SCALE_OF_TWO);

        final Map<Holding, Map<LocalDate, BigDecimal>> baseTotalReturns = monthlyReturns
                .validateCped(reqDTO.getCustomPed())
                .cutByCpedIfCpedEmptyCutByPed(reqDTO.getCustomPed())
                .fxRatesApplied()
                .getReturnsMap();

        final NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns = monthlyReturns
                .cutByPsd()
                .getWeightedAverage();

        final var calculationDTO = new CalculationDTO(reqDTO.getCustomIntervalPsd(), weightedAveragePortfolioReturns);
        return new CorrelationCalculation(calculationDTO, baseTotalReturns, defaultPeriods);
    }

}
