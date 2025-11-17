package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.RollingSharpeRatioCalculation;
import com.fintex.ce.domain.calculation.SharpeRatioCalculation;
import com.fintex.ce.domain.calculation.StandardDeviationCalculation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.RollingCalculationReqDTO;
import com.fintex.ce.dto.response.RollingSharpeRatioResDTO;
import com.fintex.ce.dto.response.SharpeRatioResDTO;
import com.fintex.ce.service.impl.cache.TBillsCacheStorage;
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

@Service
public class RollingSharpeRatioCalculationServiceImpl extends PeriodAbstractService<RollingSharpeRatioResDTO, RollingCalculationReqDTO> {

    private final TBillsCacheStorage tBillsCacheStorage;

    public RollingSharpeRatioCalculationServiceImpl(
            final MonthlyReturnsService monthlyReturnsService,
            final TBillsCacheStorage tBillsCacheStorage,
            @Value("#{'${default.periods.rolling-calculations}'.split(',')}") final Set<String> defaultPeriods,
            final RollingCalculationReqDtoValidator requestValidator) {
        super(monthlyReturnsService, defaultPeriods, requestValidator);
        this.tBillsCacheStorage = tBillsCacheStorage;
    }

    @Override
    public RollingSharpeRatioResDTO perform(final RollingCalculationReqDTO reqDTO) {
        requestValidator.validate(reqDTO);
        final var rollingStandardDeviationCalculation = defineCalculationMethod(reqDTO);
        return rollingStandardDeviationCalculation.calculate(reqDTO.getRollingPeriods());
    }

    @Override
    protected RollingSharpeRatioCalculation defineCalculationMethod(final RollingCalculationReqDTO reqDTO) {
        final CalculationDTO input = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
        final var tBills = tBillsCacheStorage.loadTBillsFor(reqDTO.getCurrency());

        final var standardDeviationCalculation = new StandardDeviationCalculation<SharpeRatioResDTO>(input, defaultPeriods);
        final var sharpeRatioCalculation = new SharpeRatioCalculation(input, defaultPeriods, tBills, standardDeviationCalculation);

        return new RollingSharpeRatioCalculation(input, defaultPeriods, sharpeRatioCalculation);
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
