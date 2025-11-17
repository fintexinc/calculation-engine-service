package com.fintex.ce.service.impl.calculation.period.core;

import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.core.PeriodResDTO;
import com.fintex.ce.exception.notification.pattern.Notification;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.RequestValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

public abstract class PeriodBenchmarkAbstractService<E extends PeriodResDTO, R extends PeriodsReqDTO> extends PeriodAbstractService<E, R> {

    protected PeriodBenchmarkAbstractService(final MonthlyReturnsService monthlyReturnsService,
                                             final Set<String> defaultPeriods,
                                             final RequestValidator<R> requestValidator) {
        super(monthlyReturnsService, defaultPeriods, requestValidator);
    }

    @Override
    public BenchmarkCalculationDTO buildCalculationDto(R reqDTO, ReturnFactorScale returnFactorScale) {
        var notification = new Notification();

        Returns portfolioMonthlyReturns = notification.tryCatch(() -> monthlyReturnsService
                .getPortfolioMonthlyReturns(reqDTO.getHoldings(), reqDTO.getCurrency(), returnFactorScale));
        Returns benchmarkMonthlyReturns = notification.tryCatch(() -> monthlyReturnsService
                .getBenchmarkMonthlyReturns(reqDTO.getBenchmarkHoldings(), reqDTO.getCurrency(), returnFactorScale));
        notification.ifAnyErrorThrowException();

        portfolioMonthlyReturns.cutArgumentToTheSameEndDate(benchmarkMonthlyReturns);
        benchmarkMonthlyReturns.cutArgumentToTheSameEndDate(portfolioMonthlyReturns);

        NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = notification.tryCatch(() -> monthlyReturnsService
                .getWeightedAverageWithCpedValidation(portfolioMonthlyReturns, reqDTO.getCustomPed()));
        NavigableMap<LocalDate, BigDecimal> benchmarkTotalReturns = notification.tryCatch(() -> monthlyReturnsService
                .getWeightedAverageWithCpedValidation(benchmarkMonthlyReturns, reqDTO.getCustomPed()));
        notification.ifAnyErrorThrowException();

        var result = new BenchmarkCalculationDTO();
        result.setWeightedAverageBenchmarkReturns(benchmarkTotalReturns);
        result.setWeightedAveragePortfolioReturns(portfolioTotalReturns);
        result.setCipsd(reqDTO.getCustomIntervalPsd());
        return result;
    }

}
