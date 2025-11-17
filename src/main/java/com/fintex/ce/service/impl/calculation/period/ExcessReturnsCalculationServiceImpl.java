package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.ExcessReturnsCalculation;
import com.fintex.ce.domain.calculation.PeriodBasedCalculation;
import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.ExcessReturnsResDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.PeriodReqDtoForBenchmarkCalculationsValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ExcessReturnsCalculationServiceImpl extends PeriodBenchmarkAbstractService<ExcessReturnsResDTO, PeriodsReqDTO> {

    public ExcessReturnsCalculationServiceImpl(
            @Autowired final MonthlyReturnsService monthlyReturnsService,
            @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods,
            @Autowired final PeriodReqDtoForBenchmarkCalculationsValidator requestValidator) {
        super(monthlyReturnsService, defaultPeriods, requestValidator);
    }

    @Override
    protected PeriodBasedCalculation<ExcessReturnsResDTO> defineCalculationMethod(final PeriodsReqDTO reqDTO) {
        final BenchmarkCalculationDTO inDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
        return new ExcessReturnsCalculation(inDTO, defaultPeriods);
    }

}
