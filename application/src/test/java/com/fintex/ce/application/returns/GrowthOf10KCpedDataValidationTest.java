package com.fintex.ce.application.returns;

import com.fintex.ce.application.validation.GrowthOf10KCpedDataValidation;
import com.fintex.ce.model.error.PceExceptionCollector;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GrowthOf10KCpedDataValidationTest {

  @Test
  void shouldValidate_whenVerify() {
    // SETUP
    final GrowthOf10KCpedDataValidation sut = mock(GrowthOf10KCpedDataValidation.class);
    var collector = new PceExceptionCollector();

    final var cped = LocalDate.now();
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().plusMonths(10);

    doCallRealMethod().when(sut).validate(any(), any(), any(), any());
    // ACT
    sut.validate(cped, psd, ped, collector);

    // VERIFY
    verify(sut).validateCpedIsBeforePsd(cped, psd, collector);
  }

}