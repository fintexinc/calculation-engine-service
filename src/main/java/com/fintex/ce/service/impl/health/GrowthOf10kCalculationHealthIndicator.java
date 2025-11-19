package com.fintex.ce.service.impl.health;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.dto.request.ReturnReqDTO;
import com.fintex.ce.dto.response.Growth10KResDTO;
import com.fintex.ce.service.interfaces.calculation.GrowthOf10KCalculationService;
import com.fintex.ce.service.interfaces.health.CalculationHeathIndicator;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class GrowthOf10kCalculationHealthIndicator extends CalculationHeathIndicator<ReturnReqDTO> {

    private final GrowthOf10KCalculationService growthOf10KCalculationService;

    public GrowthOf10kCalculationHealthIndicator(final GrowthOf10KCalculationService growthOf10KCalculationService) {
        this.growthOf10KCalculationService = growthOf10KCalculationService;
    }

    @Override
    protected Growth10KResDTO calculateResponse(final ReturnReqDTO request) {
        return growthOf10KCalculationService.perform(request);
    }

    @Override
    protected ReturnReqDTO buildInput() {
        final ReturnReqDTO request = new ReturnReqDTO();
        request.setHoldings(getHoldings());
        request.setCurrency(Currency.CAD);
        request.setCustomPerformanceStartDate(LocalDate.of(2015, 6, 30));
        request.setCustomPerformanceEndDate(LocalDate.of(2016, 6, 30));
        return request;
    }

}
