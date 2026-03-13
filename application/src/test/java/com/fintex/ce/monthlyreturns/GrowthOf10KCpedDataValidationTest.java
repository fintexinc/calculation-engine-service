package com.fintex.ce.monthlyreturns;

import com.fintex.ce.application.validation.GrowthOf10KCpedDataValidation;
import com.fintex.ce.domain.exception.notification.pattern.Notification;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

class GrowthOf10KCpedDataValidationTest {

  @Test
  void shouldValidate_whenVerify() {
    // SETUP
    final GrowthOf10KCpedDataValidation sut = mock(GrowthOf10KCpedDataValidation.class);
    var notification = new Notification();

    final var cped = LocalDate.now();
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().plusMonths(10);

    doCallRealMethod().when(sut).validate(any(), any(), any(), any());
    // ACT
    sut.validate(cped, psd, ped, notification);

    // VERIFY
    verify(sut).validateCpedIsBeforePsd(cped, psd, notification);
  }

}