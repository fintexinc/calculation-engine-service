package com.fintex.ce.service.impl.health;

import com.fintex.ce.config.enumeration.ParameterType;
import com.fintex.ce.dto.request.AverageMerRequestDTO;
import com.fintex.ce.dto.response.AverageMerResponse;
import com.fintex.ce.service.impl.calculation.MERCalculationServiceImpl;
import com.fintex.ce.service.interfaces.health.CalculationHeathIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("merCalculation")
public class MERCalculationHealthIndicator extends CalculationHeathIndicator<AverageMerRequestDTO> {

    private final MERCalculationServiceImpl merCalculationService;

    public MERCalculationHealthIndicator(final MERCalculationServiceImpl merCalculationService) {
        this.merCalculationService = merCalculationService;
    }

    @Override
    protected AverageMerResponse calculateResponse(final AverageMerRequestDTO request) {
        return merCalculationService.perform(request);
    }

    @Override
    protected AverageMerRequestDTO buildInput() {
        final AverageMerRequestDTO averageMerRequest = new AverageMerRequestDTO();
        averageMerRequest.setHoldings(getHoldings());
        averageMerRequest.setParameterTypes(List.of(ParameterType.ABSOLUTE));
        return averageMerRequest;
    }

}
