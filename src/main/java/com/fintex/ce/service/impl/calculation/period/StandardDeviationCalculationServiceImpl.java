package com.fintex.ce.service.impl.calculation.period;


import com.fintex.ce.domain.calculation.StandardDeviationCalculation;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.StandardDeviationResDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.PeriodsReqDtoValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.fintex.ce.util.DecimalUtils.OUTPUT_SCALE;

@Service
public class StandardDeviationCalculationServiceImpl extends PeriodAbstractService<StandardDeviationResDTO, PeriodsReqDTO> {

    public StandardDeviationCalculationServiceImpl(
            final MonthlyReturnsService monthlyReturnsService,
            @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods,
            final PeriodsReqDtoValidator requestValidator) {
        super(monthlyReturnsService, defaultPeriods, requestValidator);
    }

    protected StandardDeviationCalculation defineCalculationMethod(final PeriodsReqDTO reqDTO) {
        final CalculationDTO inputDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
        return new StandardDeviationCalculation(inputDTO, defaultPeriods).setScale(OUTPUT_SCALE);
    }

}
