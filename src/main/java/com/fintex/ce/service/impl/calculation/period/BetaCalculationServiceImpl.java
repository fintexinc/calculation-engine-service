package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.BetaCalculation;
import com.fintex.ce.domain.calculation.PeriodBasedCalculation;
import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.BetaResDTO;
import com.fintex.ce.service.impl.cache.TBillsCacheStorage;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.PeriodReqDtoForBenchmarkCalculationsValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

import static com.fintex.ce.domain.calculation.core.PeriodCalculationAbstract.calculateExcessReturn;

@Service
public class BetaCalculationServiceImpl extends PeriodBenchmarkAbstractService<BetaResDTO, PeriodsReqDTO> {

    private final TBillsCacheStorage tBillsCacheStorage;

    public BetaCalculationServiceImpl(
            @Autowired final MonthlyReturnsService monthlyReturnsService,
            @Autowired final TBillsCacheStorage tBillsCacheStorage,
            @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods,
            @Autowired final PeriodReqDtoForBenchmarkCalculationsValidator requestValidator) {
        super(monthlyReturnsService, defaultPeriods, requestValidator);
        this.tBillsCacheStorage = tBillsCacheStorage;
    }

    @Override
    protected PeriodBasedCalculation<BetaResDTO> defineCalculationMethod(final PeriodsReqDTO reqDTO) {
        final BenchmarkCalculationDTO inDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
        final var tBills = tBillsCacheStorage.loadTBillsFor(reqDTO.getCurrency());
        final NavigableMap<LocalDate, BigDecimal> portfolioExccessReturn = calculateExcessReturn(inDTO.getWeightedAveragePortfolioReturns(), tBills);
        final NavigableMap<LocalDate, BigDecimal> benchmarkExccessReturn = calculateExcessReturn(inDTO.getWeightedAverageBenchmarkReturns(), tBills);
        return new BetaCalculation(inDTO, defaultPeriods, portfolioExccessReturn, benchmarkExccessReturn);
    }

}
