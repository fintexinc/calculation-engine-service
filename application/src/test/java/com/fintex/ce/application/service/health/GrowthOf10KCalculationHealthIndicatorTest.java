package com.fintex.ce.application.service.health;

import com.fintex.ce.application.service.calculation.GrowthOf10KCalculationServiceImpl;
import com.fintex.ce.application.service.health.GrowthOf10kCalculationHealthIndicator;
import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.port.input.command.ReturnCommand;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

class GrowthOf10KCalculationHealthIndicatorTest {

  @Test
  void calculateResponse_verifyPerform() {
    // SETUP
    final var growthOf10KCalculationService = mock(GrowthOf10KCalculationServiceImpl.class);
    final var sut = mock(GrowthOf10kCalculationHealthIndicator.class, withSettings().useConstructor(
        growthOf10KCalculationService));
    final var returnReqDTO = mock(ReturnCommand.class);

    doCallRealMethod().when(sut).calculateResponse(any(ReturnCommand.class));

    // ACT
    sut.calculateResponse(returnReqDTO);

    // VERIFY
    verify(growthOf10KCalculationService).perform(returnReqDTO);
  }

  @Test
  void buildInput_checkResult() {
    // SETUP
    final var sut = mock(GrowthOf10kCalculationHealthIndicator.class);

    doCallRealMethod().when(sut).buildInput();

    // ACT
    final ReturnCommand actual = sut.buildInput();

    // VERIFY
    assertEquals(Currency.CAD, actual.getCurrency());
    assertEquals(LocalDate.of(2015, 6, 30), actual.getCustomPerformanceStartDate());
    assertEquals(LocalDate.of(2016, 6, 30), actual.getCustomPerformanceEndDate());
  }

}
