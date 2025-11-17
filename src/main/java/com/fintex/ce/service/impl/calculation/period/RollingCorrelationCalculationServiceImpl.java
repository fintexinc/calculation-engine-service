package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.CorrelationCalculation;
import com.fintex.ce.domain.calculation.RollingCorrelationCalculation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.RollingCalculationReqDTO;
import com.fintex.ce.dto.response.RollingCorrelationResDTO;
import com.fintex.ce.exception.notification.pattern.Notification;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.RollingCorrelationReqValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

@Service
public class RollingCorrelationCalculationServiceImpl extends PeriodBenchmarkAbstractService<RollingCorrelationResDTO, RollingCalculationReqDTO> {

    public RollingCorrelationCalculationServiceImpl(
            MonthlyReturnsService monthlyReturnsService,
            @Value("#{'${default.periods.rolling-calculations}'.split(',')}") Set<String> defaultPeriods,
            RollingCorrelationReqValidator requestValidator) {
        super(monthlyReturnsService, defaultPeriods, requestValidator);
    }

    @Override
    public RollingCorrelationResDTO perform(RollingCalculationReqDTO reqDTO) {
        requestValidator.validate(reqDTO);
        RollingCorrelationCalculation rollingCorrelationCalculation = defineCalculationMethod(reqDTO);
        return rollingCorrelationCalculation.calculate(reqDTO.getRollingPeriods());
    }

    @Override
    protected RollingCorrelationCalculation defineCalculationMethod(RollingCalculationReqDTO reqDTO) {
        BenchmarkCalculationDTO inputDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
        Map<Holding, Map<LocalDate, BigDecimal>> baseTotalReturn = getBaseTotalReturns(reqDTO);
        var correlationCalculation = new CorrelationCalculation(inputDTO, baseTotalReturn, defaultPeriods);
        return new RollingCorrelationCalculation(inputDTO, defaultPeriods, correlationCalculation, inputDTO.getWeightedAverageBenchmarkReturns());
    }

    Map<Holding, Map<LocalDate, BigDecimal>> getBaseTotalReturns(RollingCalculationReqDTO reqDTO) {
        Returns monthlyReturns = monthlyReturnsService.getPortfolioMonthlyReturns(reqDTO.getHoldings(), reqDTO.getCurrency(), ReturnFactorScale.SCALE_OF_TWO);

        return monthlyReturns
                .validateCped(reqDTO.getCustomPed())
                .cutByCpedIfCpedEmptyCutByPed(reqDTO.getCustomPed())
                .fxRatesApplied()
                .getReturnsMap();
    }

    @Override
    public BenchmarkCalculationDTO buildCalculationDto(RollingCalculationReqDTO reqDTO,
                                                       ReturnFactorScale returnFactorScale) {
        Notification notification = new Notification();

        Returns portfolioMonthlyReturns = notification.tryCatch(() -> monthlyReturnsService
                .getPortfolioMonthlyReturns(reqDTO.getHoldings(), reqDTO.getCurrency(), returnFactorScale));
        Returns benchmarkMonthlyReturns = notification.tryCatch(() -> monthlyReturnsService
                .getBenchmarkMonthlyReturns(reqDTO.getBenchmarkHoldings(), reqDTO.getCurrency(), returnFactorScale));
        notification.ifAnyErrorThrowException();

        portfolioMonthlyReturns.cutArgumentToTheSameEndDate(benchmarkMonthlyReturns);
        benchmarkMonthlyReturns.cutArgumentToTheSameEndDate(portfolioMonthlyReturns);

        NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = notification.tryCatch(() -> monthlyReturnsService
                .getWeightedAverageWithCpsdAndCpedValidation(portfolioMonthlyReturns, reqDTO.getCustomPsd(), reqDTO.getCustomPed()));
        NavigableMap<LocalDate, BigDecimal> benchmarkTotalReturns = notification.tryCatch(() -> monthlyReturnsService
                .getWeightedAverageWithCpsdAndCpedValidation(benchmarkMonthlyReturns, reqDTO.getCustomPsd(), reqDTO.getCustomPed()));
        notification.ifAnyErrorThrowException();

        var result = new BenchmarkCalculationDTO();
        result.setWeightedAverageBenchmarkReturns(benchmarkTotalReturns);
        result.setWeightedAveragePortfolioReturns(portfolioTotalReturns);
        result.setCipsd(reqDTO.getCustomIntervalPsd());
        return result;
    }

}
