package com.fintex.ce.application.service.health;

import com.fintex.ce.domain.enumeration.HoldingIdentifierType;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.port.input.result.WarningResult;
import com.fintex.ce.domain.exception.ReqValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CalculationHeathIndicatorTest {

  @Test
  void health_verifyCalculateResponse() {
    // SETUP
    final var sut = mock(com.fintex.ce.application.service.health.CalculationHeathIndicator.class);
    final var warningDTO = mock(WarningResult.class);

    when(warningDTO.getWarnings()).thenReturn(List.of());
    when(sut.calculateResponse(any())).thenReturn(warningDTO);
    doCallRealMethod().when(sut).health();

    // ACT
    sut.health();

    // VERIFY
    verify(sut).calculateResponse(any());
  }

  @Test
  void health_verifyBuildInput() {
    // SETUP
    final var sut = mock(com.fintex.ce.application.service.health.CalculationHeathIndicator.class);
    final var warningDTO = mock(WarningResult.class);

    when(warningDTO.getWarnings()).thenReturn(List.of());
    when(sut.calculateResponse(any())).thenReturn(warningDTO);
    doCallRealMethod().when(sut).health();

    // ACT
    sut.health();

    // VERIFY
    verify(sut).buildInput();
  }

  @Test
  void health_checkResult_whenResultContainsWarnings() {
    // SETUP
    final var sut = mock(com.fintex.ce.application.service.health.CalculationHeathIndicator.class);
    final var warningDTO = mock(WarningResult.class);
    final var warnings = List.of(mock(Warning.class));

    when(warningDTO.getWarnings()).thenReturn(warnings);
    when(sut.calculateResponse(any())).thenReturn(warningDTO);
    doCallRealMethod().when(sut).health();

    // ACT
    final Health actual = sut.health();

    // VERIFY
    assertEquals(Status.DOWN, actual.getStatus());
    assertEquals(1, actual.getDetails().size());
    assertTrue(actual.getDetails().containsKey("warnings"));
    assertTrue(actual.getDetails().containsValue(warnings));
  }

  @Test
  void health_checkResult_whenResultWarningsIsEmpty() {
    // SETUP
    final var sut = mock(com.fintex.ce.application.service.health.CalculationHeathIndicator.class);
    final var warningDTO = mock(WarningResult.class);

    when(warningDTO.getWarnings()).thenReturn(List.of());
    when(sut.calculateResponse(any())).thenReturn(warningDTO);
    doCallRealMethod().when(sut).health();

    // ACT
    final Health actual = sut.health();

    // VERIFY
    assertEquals(Status.UP, actual.getStatus());
    assertEquals(0, actual.getDetails().size());
  }

  @Test
  void health_checkResult_whenCalculateResponseThrowsValidationException() {
    // SETUP
    final var sut = mock(com.fintex.ce.application.service.health.CalculationHeathIndicator.class);
    final var expectedMessage = "message";

    when(sut.calculateResponse(any())).thenThrow(new ReqValidationException(expectedMessage));
    doCallRealMethod().when(sut).health();

    // ACT
    final Health actual = sut.health();

    // VERIFY
    assertEquals(Status.UP, actual.getStatus());
    assertEquals(1, actual.getDetails().size());
    assertTrue(actual.getDetails().containsKey("data validation error"));
    assertTrue(actual.getDetails().containsValue(expectedMessage));
  }

  @Test
  void health_checkResult_whenCalculateResponseThrowsException() {
    // SETUP
    final var sut = mock(com.fintex.ce.application.service.health.CalculationHeathIndicator.class);
    final var expectedMessage = "message";

    when(sut.calculateResponse(any())).thenThrow(new RuntimeException(expectedMessage));
    doCallRealMethod().when(sut).health();

    // ACT
    final Health actual = sut.health();

    // VERIFY
    assertEquals(Status.DOWN, actual.getStatus());
    assertEquals(1, actual.getDetails().size());
    assertTrue(actual.getDetails().containsKey("exception"));
    assertTrue(actual.getDetails().containsValue(expectedMessage));
  }

  @Test
  void getHoldings_checkResult() {
    // SETUP
    final var sut = mock(com.fintex.ce.application.service.health.CalculationHeathIndicator.class);
    final var expected = List.of(new FundSeriesHolding(BigDecimal.ONE, "RBF605")
        .setHoldingIdentifier(HoldingIdentifierType.FUNDSERV)
        .setType(HoldingType.CANADA_MUTUAL_FUNDS));

    doCallRealMethod().when(sut).getHoldings();

    // ACT
    final List actual = sut.getHoldings();

    // VERIFY
    assertEquals(expected, actual);
  }

}