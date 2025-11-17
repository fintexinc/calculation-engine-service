package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.PeriodBasedCalculation;
import com.fintex.ce.domain.calculation.TrackingErrorCalculation;
import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.TrackingErrorResDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.PeriodReqDtoForBenchmarkCalculationsValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class TrackingErrorCalculationServiceImpl extends PeriodBenchmarkAbstractService<TrackingErrorResDTO, PeriodsReqDTO> {

    public TrackingErrorCalculationServiceImpl(
            final MonthlyReturnsService monthlyReturnsService,
            @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods,
            final PeriodReqDtoForBenchmarkCalculationsValidator requestValidator) {
        super(monthlyReturnsService, defaultPeriods, requestValidator);
    }

    @Override
    protected PeriodBasedCalculation<TrackingErrorResDTO> defineCalculationMethod(final PeriodsReqDTO reqDTO) {
        final BenchmarkCalculationDTO input = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
        return new TrackingErrorCalculation(input, defaultPeriods);
    }
}
