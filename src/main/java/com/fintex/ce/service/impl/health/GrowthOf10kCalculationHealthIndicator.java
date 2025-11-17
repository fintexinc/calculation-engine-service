package com.fintex.ce.service.impl.health;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.dto.request.GrowthOf10KReqDTO;
import com.fintex.ce.dto.response.Growth10KResDTO;
import com.fintex.ce.service.interfaces.calculation.GrowthOf10KCalculationService;
import com.fintex.ce.service.interfaces.health.CalculationHeathIndicator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class GrowthOf10kCalculationHealthIndicator extends CalculationHeathIndicator<GrowthOf10KReqDTO> {

    private final GrowthOf10KCalculationService growthOf10KCalculationService;

    public GrowthOf10kCalculationHealthIndicator(final GrowthOf10KCalculationService growthOf10KCalculationService) {
        this.growthOf10KCalculationService = growthOf10KCalculationService;
    }

    @Override
    protected Growth10KResDTO calculateResponse(final GrowthOf10KReqDTO request) {
        return growthOf10KCalculationService.perform(request);
    }

    @Override
    protected GrowthOf10KReqDTO buildInput() {
        final GrowthOf10KReqDTO request = new GrowthOf10KReqDTO();
        request.setHoldings(getHoldings());
        request.setCurrency(Currency.CAD);
        request.setCustomPerformanceStartDate(LocalDate.of(2015, 6, 30));
        request.setCustomPerformanceEndDate(LocalDate.of(2016, 6, 30));
        return request;
    }

}
