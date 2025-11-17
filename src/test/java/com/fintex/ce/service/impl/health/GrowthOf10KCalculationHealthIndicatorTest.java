package com.fintex.ce.service.impl.health;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.dto.request.GrowthOf10KReqDTO;
import com.fintex.ce.dto.request.ReturnReqDTO;
import com.fintex.ce.service.impl.calculation.GrowthOf10KCalculationServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GrowthOf10KCalculationHealthIndicatorTest {

    @Test
    void calculateResponse_verifyPerform() {
        //SETUP
        final var growthOf10KCalculationService = mock(GrowthOf10KCalculationServiceImpl.class);
        final var sut = mock(GrowthOf10kCalculationHealthIndicator.class, withSettings().useConstructor(growthOf10KCalculationService));
        final var returnReqDTO = mock(GrowthOf10KReqDTO.class);

        doCallRealMethod().when(sut).calculateResponse(any(GrowthOf10KReqDTO.class));

        //ACT
        sut.calculateResponse(returnReqDTO);

        //VERIFY
        verify(growthOf10KCalculationService).perform(returnReqDTO);
    }

    @Test
    void buildInput_checkResult() {
        //SETUP
        final var sut = mock(GrowthOf10kCalculationHealthIndicator.class);

        doCallRealMethod().when(sut).buildInput();

        //ACT
        final ReturnReqDTO actual = sut.buildInput();

        //VERIFY
        assertEquals(Currency.CAD, actual.getCurrency());
        assertEquals(LocalDate.of(2015, 6, 30), actual.getCustomPerformanceStartDate());
        assertEquals(LocalDate.of(2016, 6, 30), actual.getCustomPerformanceEndDate());
    }

}
