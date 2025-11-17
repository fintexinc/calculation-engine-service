package com.fintex.ce.service.impl.health;

import com.fintex.ce.config.enumeration.ParameterType;
import com.fintex.ce.dto.request.AverageMerRequestDTO;
import com.fintex.ce.service.impl.calculation.MERCalculationServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MERCalculationHealthIndicatorTest {

    @Test
    void calculateResponse_verifyPerform() {
        //SETUP
        final var merCalculationService = mock(MERCalculationServiceImpl.class);
        final var sut = mock(MERCalculationHealthIndicator.class, withSettings().useConstructor(merCalculationService));
        final var averageMerRequestDTO = mock(AverageMerRequestDTO.class);

        doCallRealMethod().when(sut).calculateResponse(any(AverageMerRequestDTO.class));

        //ACT
        sut.calculateResponse(averageMerRequestDTO);

        //VERIFY
        verify(merCalculationService).perform(averageMerRequestDTO);
    }

    @Test
    void buildInput() {
        //SETUP
        final var sut = mock(MERCalculationHealthIndicator.class);

        doCallRealMethod().when(sut).buildInput();

        //ACT
        final AverageMerRequestDTO actual = sut.buildInput();

        //VERIFY
        assertEquals(List.of(ParameterType.ABSOLUTE), actual.getParameterTypes());
    }
}