package com.fintex.ce.service.impl.calculation.period;

import com.fintex.ce.domain.calculation.InformationRatioCalculation;
import com.fintex.ce.domain.calculation.PeriodBasedCalculation;
import com.fintex.ce.domain.calculation.TrackingErrorCalculation;
import com.fintex.ce.domain.calculation.TrailingTotalReturnsCalculation;
import com.fintex.ce.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.InformationRatioResDTO;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.PeriodReqDtoForBenchmarkCalculationsValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class InformationRatioCalculationServiceImpl extends PeriodBenchmarkAbstractService<InformationRatioResDTO, PeriodsReqDTO> {

    public InformationRatioCalculationServiceImpl(
            @Autowired final MonthlyReturnsService monthlyReturnsService,
            @Value("#{'${default.periods.information-ratio-returns}'.split(',')}") final Set<String> defaultPeriods,
            @Autowired final PeriodReqDtoForBenchmarkCalculationsValidator requestValidator) {
        super(monthlyReturnsService, defaultPeriods, requestValidator);
    }

    @Override
    protected PeriodBasedCalculation<InformationRatioResDTO> defineCalculationMethod(PeriodsReqDTO reqDTO) {
        final BenchmarkCalculationDTO input = buildCalculationDto(reqDTO, ReturnFactorScale.SCALE_OF_TWO);
        final var trailingTotalReturnsCalculation = new TrailingTotalReturnsCalculation(input, Set.of());
        final var trackingErrorCalculation = new TrackingErrorCalculation(input, Set.of());
        return new InformationRatioCalculation(input, defaultPeriods, trailingTotalReturnsCalculation, trackingErrorCalculation);
    }
}
