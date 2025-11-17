package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.PeriodBasedCalculation;
import com.fintex.ce.domain.calculation.UpsideCaptureCalculation;
import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.UpsideCaptureResDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.util.validation.request.PeriodReqDtoForBenchmarkCalculationsValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.fintex.ce.util.ReturnFactorScale.AS_IS;

@Service
public class UpsideCaptureCalculationServiceImpl extends PeriodBenchmarkAbstractService<UpsideCaptureResDTO, PeriodsReqDTO> {

    public UpsideCaptureCalculationServiceImpl(
            final MonthlyReturnsService monthlyReturnsService,
            @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods,
            final PeriodReqDtoForBenchmarkCalculationsValidator requestValidator) {
        super(monthlyReturnsService, defaultPeriods, requestValidator);
    }

    @Override
    protected PeriodBasedCalculation<UpsideCaptureResDTO> defineCalculationMethod(final PeriodsReqDTO reqDTO) {
        final BenchmarkCalculationDTO inDTO = buildCalculationDto(reqDTO, AS_IS);
        return new UpsideCaptureCalculation(inDTO, defaultPeriods);
    }

}
