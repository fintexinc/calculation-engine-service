package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.DownsideDeviationCalculation;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.DownsideDeviationResDTO;
import com.fintex.ce.service.impl.cache.TBillsCacheStorage;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.PeriodsReqDtoValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class DownsideDeviationCalculationServiceImpl extends PeriodAbstractService<DownsideDeviationResDTO, PeriodsReqDTO> {

    private final TBillsCacheStorage tBillsCacheStorage;

    public DownsideDeviationCalculationServiceImpl(
            @Autowired final MonthlyReturnsService monthlyReturnsService,
            @Autowired final TBillsCacheStorage tBillsCacheStorage,
            @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods,
            @Autowired final PeriodsReqDtoValidator requestValidator) {
        super(monthlyReturnsService, defaultPeriods, requestValidator);
        this.tBillsCacheStorage = tBillsCacheStorage;
    }

    protected DownsideDeviationCalculation<DownsideDeviationResDTO> defineCalculationMethod(final PeriodsReqDTO reqDTO) {
        final CalculationDTO inputDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_ONE);
        final var tBills = tBillsCacheStorage.loadTBillsFor(reqDTO.getCurrency());
        return new DownsideDeviationCalculation<>(inputDTO, defaultPeriods, tBills);
    }
}
