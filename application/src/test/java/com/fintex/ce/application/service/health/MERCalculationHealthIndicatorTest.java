package com.fintex.ce.application.service.health;

import com.fintex.ce.application.service.calculation.MERCalculationServiceImpl;
import com.fintex.ce.application.service.health.MERCalculationHealthIndicator;
import com.fintex.ce.domain.enumeration.ParameterType;
import com.fintex.ce.application.command.AverageMerCommand;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MERCalculationHealthIndicatorTest {

  @Test
  void calculateResponse_verifyPerform() {
    // SETUP
    final var merCalculationService = mock(MERCalculationServiceImpl.class);
    final var sut = mock(MERCalculationHealthIndicator.class, withSettings().useConstructor(merCalculationService));
    final var averageMerRequestDTO = mock(AverageMerCommand.class);

    doCallRealMethod().when(sut).calculateResponse(any(AverageMerCommand.class));

    // ACT
    sut.calculateResponse(averageMerRequestDTO);

    // VERIFY
    verify(merCalculationService).perform(averageMerRequestDTO);
  }

  @Test
  void buildInput() {
    // SETUP
    final var sut = mock(MERCalculationHealthIndicator.class);

    doCallRealMethod().when(sut).buildInput();

    // ACT
    final AverageMerCommand actual = sut.buildInput();

    // VERIFY
    assertEquals(List.of(ParameterType.ABSOLUTE), actual.getParameterTypes());
  }
}