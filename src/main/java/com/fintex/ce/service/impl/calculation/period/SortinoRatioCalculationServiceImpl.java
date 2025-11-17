package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.DownsideDeviationCalculation;
import com.fintex.ce.domain.calculation.SortinoRatioCalculation;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.SortinoRatioResDTO;
import com.fintex.ce.service.impl.cache.TBillsCacheStorage;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.PeriodsReqDtoValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SortinoRatioCalculationServiceImpl extends PeriodAbstractService<SortinoRatioResDTO, PeriodsReqDTO> {

    private final TBillsCacheStorage tBillsCacheStorage;

    public SortinoRatioCalculationServiceImpl(
            final MonthlyReturnsService monthlyReturnsService,
            final TBillsCacheStorage tBillsCacheStorage,
            @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods,
            final PeriodsReqDtoValidator requestValidator) {
        super(monthlyReturnsService, defaultPeriods, requestValidator);
        this.tBillsCacheStorage = tBillsCacheStorage;
    }

    @Override
    protected SortinoRatioCalculation defineCalculationMethod(final PeriodsReqDTO reqDTO) {
        final CalculationDTO input = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
        final var tBills = tBillsCacheStorage.loadTBillsFor(reqDTO.getCurrency());
        final DownsideDeviationCalculation<SortinoRatioResDTO> downsideDeviationCalculation = new DownsideDeviationCalculation<>(input, defaultPeriods, tBills);
        return new SortinoRatioCalculation(input, defaultPeriods, tBills, downsideDeviationCalculation);
    }
}
