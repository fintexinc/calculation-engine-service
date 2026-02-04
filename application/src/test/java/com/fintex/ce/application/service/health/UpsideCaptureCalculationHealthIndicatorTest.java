package com.fintex.ce.application.service.health;

import com.fintex.ce.application.service.calculation.period.UpsideCaptureCalculationServiceImpl;
import com.fintex.ce.application.service.health.UpsideCaptureCalculationHealthIndicator;
import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.port.input.command.PeriodCommand;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpsideCaptureCalculationHealthIndicatorTest {

  @Test
  void calculateResponse_verifyPerform() {
    // SETUP
    final var upsideCaptureCalculationService = mock(UpsideCaptureCalculationServiceImpl.class);
    final var sut = mock(UpsideCaptureCalculationHealthIndicator.class, withSettings().useConstructor(
        upsideCaptureCalculationService));
    final var requestDto = mock(PeriodCommand.class);

    doCallRealMethod().when(sut).calculateResponse(any(PeriodCommand.class));

    // ACT
    sut.calculateResponse(requestDto);

    // VERIFY
    verify(upsideCaptureCalculationService).perform(requestDto);
  }

  @Test
  void buildInput_checkResult() {
    // SETUP
    final var sut = mock(UpsideCaptureCalculationHealthIndicator.class);

    doCallRealMethod().when(sut).buildInput();

    // ACT
    final PeriodCommand actual = sut.buildInput();

    // VERIFY
    assertEquals(LocalDate.of(2019, 1, 31), actual.getCustomIntervalPsd());
    assertEquals(LocalDate.of(2019, 6, 30), actual.getCustomPed());
    assertEquals(Set.of("12", "36", "60", "120"), actual.getPeriods());
    assertEquals(Currency.CAD, actual.getCurrency());
  }

}