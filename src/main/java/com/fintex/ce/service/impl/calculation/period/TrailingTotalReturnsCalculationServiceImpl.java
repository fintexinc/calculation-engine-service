package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.TrailingTotalReturnsCalculation;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.TrailingTotalReturnsResDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.TrailingTotalReturnsReqValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class TrailingTotalReturnsCalculationServiceImpl extends PeriodAbstractService<TrailingTotalReturnsResDTO, PeriodsReqDTO> {

    public TrailingTotalReturnsCalculationServiceImpl(
            final MonthlyReturnsService monthlyReturnsService,
            @Value("#{'${default.periods.trailing-total-returns}'.split(',')}") final Set<String> defaultPeriods,
            final TrailingTotalReturnsReqValidator trailingTotalReturnsReqValidator) {
        super(monthlyReturnsService, defaultPeriods, trailingTotalReturnsReqValidator);
    }

    protected TrailingTotalReturnsCalculation defineCalculationMethod(final PeriodsReqDTO reqDTO) {
        final CalculationDTO inputDTO = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
        return new TrailingTotalReturnsCalculation(inputDTO, defaultPeriods);
    }

    @Override
    public void addSpecificChecks(final PeriodsReqDTO reqDTO) {
        // Empty as there are no specific checks for the current calculation
    }

}
