package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.BetaCalculation;
import com.fintex.ce.domain.calculation.PeriodBasedCalculation;
import com.fintex.ce.domain.calculation.TreynorRatioCalculation;
import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.TreynorRatioResDTO;
import com.fintex.ce.service.impl.cache.TBillsCacheStorage;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.PeriodReqDtoForBenchmarkCalculationsValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

import static com.fintex.ce.domain.calculation.core.PeriodCalculationAbstract.calculateExcessReturn;

@Service
public class TreynorRatioServiceImpl extends PeriodBenchmarkAbstractService<TreynorRatioResDTO, PeriodsReqDTO> {

    private final TBillsCacheStorage tBillsCacheStorage;

    public TreynorRatioServiceImpl(
            MonthlyReturnsService monthlyReturnsService,
            TBillsCacheStorage tBillsCacheStorage,
            @Value("#{'${default.periods.risk-calculations}'.split(',')}") Set<String> defaultPeriods,
            PeriodReqDtoForBenchmarkCalculationsValidator requestValidator) {
        super(monthlyReturnsService, defaultPeriods, requestValidator);
        this.tBillsCacheStorage = tBillsCacheStorage;
    }

    @Override
    protected PeriodBasedCalculation<TreynorRatioResDTO> defineCalculationMethod(PeriodsReqDTO reqDTO) {
        BenchmarkCalculationDTO betaInput = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
        BenchmarkCalculationDTO treynorRationInput = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
        var tBills = tBillsCacheStorage.loadTBillsFor(reqDTO.getCurrency());
        NavigableMap<LocalDate, BigDecimal> portfolioExccessReturn = calculateExcessReturn(betaInput.getWeightedAveragePortfolioReturns(), tBills);
        NavigableMap<LocalDate, BigDecimal> benchmarkExccessReturn = calculateExcessReturn(betaInput.getWeightedAverageBenchmarkReturns(), tBills);
        var betaCalculation = new BetaCalculation(betaInput, defaultPeriods, portfolioExccessReturn, benchmarkExccessReturn);
        return new TreynorRatioCalculation(treynorRationInput, defaultPeriods, tBills, betaCalculation);
    }

}
