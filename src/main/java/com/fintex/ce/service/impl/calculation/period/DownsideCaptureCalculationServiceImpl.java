package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.DownsideCaptureCalculation;
import com.fintex.ce.domain.calculation.PeriodBasedCalculation;
import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.DownsideCaptureResDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.PeriodReqDtoForBenchmarkCalculationsValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class DownsideCaptureCalculationServiceImpl extends PeriodBenchmarkAbstractService<DownsideCaptureResDTO, PeriodsReqDTO> {

    public DownsideCaptureCalculationServiceImpl(
            @Autowired final MonthlyReturnsService monthlyReturnsService,
            @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods,
            @Autowired final PeriodReqDtoForBenchmarkCalculationsValidator requestValidator) {
        super(monthlyReturnsService, defaultPeriods, requestValidator);
    }

    @Override
    protected PeriodBasedCalculation<DownsideCaptureResDTO> defineCalculationMethod(final PeriodsReqDTO reqDTO) {
        final BenchmarkCalculationDTO inDTO = buildCalculationDto(reqDTO, ReturnFactorScale.AS_IS);
        return new DownsideCaptureCalculation(inDTO, defaultPeriods);
    }

}
