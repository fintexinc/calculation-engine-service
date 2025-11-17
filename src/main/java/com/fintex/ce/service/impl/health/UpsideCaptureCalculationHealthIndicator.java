package com.fintex.ce.service.impl.health;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.UpsideCaptureResDTO;
import com.fintex.ce.service.impl.calculation.period.UpsideCaptureCalculationServiceImpl;
import com.fintex.ce.service.interfaces.health.CalculationHeathIndicator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
public class UpsideCaptureCalculationHealthIndicator extends CalculationHeathIndicator<PeriodsReqDTO> {

    private final UpsideCaptureCalculationServiceImpl upsideCaptureCalculationService;

    public UpsideCaptureCalculationHealthIndicator(final UpsideCaptureCalculationServiceImpl upsideCaptureCalculationService) {
        this.upsideCaptureCalculationService = upsideCaptureCalculationService;
    }

    @Override
    protected UpsideCaptureResDTO calculateResponse(final PeriodsReqDTO periodsReqDTO) {
        return upsideCaptureCalculationService.perform(periodsReqDTO);
    }

    @Override
    protected PeriodsReqDTO buildInput() {
        return (PeriodsReqDTO) new PeriodsReqDTO()
                .setCustomIntervalPsd(LocalDate.of(2019, 1, 31))
                .setCustomPed(LocalDate.of(2019, 6, 30))
                .setPeriods(Set.of("12", "36", "60", "120"))
                .setCurrency(Currency.CAD)
                .setBenchmarkHoldings(getHoldings())
                .setHoldings(getHoldings());
    }

}
