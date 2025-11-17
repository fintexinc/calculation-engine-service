package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.SharpeRatioCalculation;
import com.fintex.ce.domain.calculation.StandardDeviationCalculation;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.SharpeRatioResDTO;
import com.fintex.ce.service.impl.cache.TBillsCacheStorage;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.PeriodsReqDtoValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SharpeRatioCalculationServiceImpl extends PeriodAbstractService<SharpeRatioResDTO, PeriodsReqDTO> {

    private final TBillsCacheStorage tBillsCacheStorage;

    public SharpeRatioCalculationServiceImpl(
            final MonthlyReturnsService monthlyReturnsService,
            final TBillsCacheStorage tBillsCacheStorage,
            @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods,
            final PeriodsReqDtoValidator requestValidator) {
        super(monthlyReturnsService, defaultPeriods, requestValidator);
        this.tBillsCacheStorage = tBillsCacheStorage;
    }

    protected SharpeRatioCalculation defineCalculationMethod(final PeriodsReqDTO reqDTO) {
        final CalculationDTO input = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
        final var tBills = tBillsCacheStorage.loadTBillsFor(reqDTO.getCurrency());
        final var standardDeviationCalculation = new StandardDeviationCalculation<SharpeRatioResDTO>(input, defaultPeriods);
        return new SharpeRatioCalculation(input, defaultPeriods, tBills, standardDeviationCalculation);
    }

}
