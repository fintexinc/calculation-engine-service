package com.fintex.ce.application.returns;

import com.fintex.ce.application.validation.CpedDataValidation;
import com.fintex.ce.model.error.PceExceptionCollector;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.fintex.ce.model.error.ErrorCode.CPED_AFTER_PORTFOLIO_PED;
import static com.fintex.ce.model.error.ErrorCode.CPED_BEFORE_PORTFOLIO_PSD;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CpedDataValidationTest {

  @Test
  void shouldValidate_whenVerifyValidateCpedIsAfterPed() {
    // SETUP
    final var validation = mock(CpedDataValidation.class);
    var notification = new PceExceptionCollector();

    final var cped = LocalDate.now();
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().minusMonths(2);

    doCallRealMethod().when(validation).validate(any(), any(), any(), any());
    // ACT
    validation.validate(cped, psd, ped, notification);

    // VERIFY
    verify(validation).validateCpedIsAfterPed(cped, ped, notification);
  }

  @Test
  void shouldValidate_whenVerifyValidateCpedIsBeforePsd() {
    // SETUP
    final var validation = mock(CpedDataValidation.class);
    var notification = new PceExceptionCollector();

    final var cped = LocalDate.now();
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().minusMonths(2);

    doCallRealMethod().when(validation).validate(any(), any(), any(), any());
    // ACT
    validation.validate(cped, psd, ped, notification);

    // VERIFY
    verify(validation).validateCpedIsBeforePsd(cped, psd, notification);
  }

  @Test
  void shouldValidateCpedIsBeforePsd_whenCpedIsNulLThenNothingShouldHappen() {
    // SETUP
    final var validation = mock(CpedDataValidation.class);
    var notification = new PceExceptionCollector();

    final LocalDate cped = null;
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().minusMonths(2);

    doCallRealMethod().when(validation).validateCpedIsBeforePsd(any(), any(), any());
    // ACT
    validation.validateCpedIsBeforePsd(cped, psd, notification);

    // VERIFY
    verify(validation, never()).getCpedIsBeforePsdExceptionCode();
  }

  @Test
  void shouldValidateCpedIsBeforePsd_whenCpedIsAfterPsdNothingShouldHappen() {
    // SETUP
    final var validation = mock(CpedDataValidation.class);
    var notification = new PceExceptionCollector();

    final var cped = LocalDate.now();
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().minusMonths(2);

    when(validation.getCpedIsBeforePsdExceptionCode()).thenReturn(CPED_BEFORE_PORTFOLIO_PSD);

    doCallRealMethod().when(validation).validateCpedIsBeforePsd(any(), any(), any());
    // ACT
    validation.validateCpedIsBeforePsd(cped, psd, notification);

    // VERIFY
    verify(validation, never()).getCpedIsBeforePsdExceptionCode();
  }

  @Test
  void shouldValidateCpedIsBeforePsd_whenCpedIsBeforePsdErrorShouldBeThrown() {
    // SETUP
    final var validation = mock(CpedDataValidation.class);
    var notification = new PceExceptionCollector();

    final var cped = LocalDate.now();
    final var psd = LocalDate.now().plusMonths(1);
    final var ped = LocalDate.now().minusMonths(2);

    when(validation.getCpedIsBeforePsdExceptionCode()).thenReturn(CPED_BEFORE_PORTFOLIO_PSD);

    doCallRealMethod().when(validation).validateCpedIsBeforePsd(any(), any(), any());
    // ACT
    validation.validateCpedIsBeforePsd(cped, psd, notification);

    // VERIFY
    assertTrue(notification.getExceptions().contains(CPED_BEFORE_PORTFOLIO_PSD.toException()));
  }

  @Test
  void shouldValidateCpedIsAfterPed_whenCpedIsNulLThenNothingShouldHappen() {
    // SETUP
    final var validation = mock(CpedDataValidation.class);
    final PceExceptionCollector notification = new PceExceptionCollector();

    final LocalDate cped = null;
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().minusMonths(2);

    doCallRealMethod().when(validation).validateCpedIsAfterPed(any(), any(), any());
    // ACT
    validation.validateCpedIsAfterPed(cped, ped, notification);

    // VERIFY
    verify(validation, never()).getCpedIsAfterPedExceptionCode();
  }

  @Test
  void shouldValidateCpedIsAfterPed_whenCpedIsBeforePedNothingShouldHappen() {
    // SETUP
    final var validation = mock(CpedDataValidation.class);
    final PceExceptionCollector notification = new PceExceptionCollector();

    final var cped = LocalDate.now();
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().plusMonths(2);

    when(validation.getCpedIsBeforePsdExceptionCode()).thenReturn(CPED_AFTER_PORTFOLIO_PED);

    doCallRealMethod().when(validation).validateCpedIsAfterPed(any(), any(), any());
    // ACT
    validation.validateCpedIsAfterPed(cped, ped, notification);

    // VERIFY
    verify(validation, never()).getCpedIsAfterPedExceptionCode();
  }

  @Test
  void shouldValidateCpedIsAfterPed_whenCpedIsAfterPedErrorShouldBeThrown() {
    // SETUP
    final var validation = mock(CpedDataValidation.class);
    final PceExceptionCollector notification = new PceExceptionCollector();

    final var cped = LocalDate.now();
    final var psd = LocalDate.now().plusMonths(1);
    final var ped = LocalDate.now().minusMonths(2);

    when(validation.getCpedIsAfterPedExceptionCode()).thenReturn(CPED_AFTER_PORTFOLIO_PED);

    doCallRealMethod().when(validation).validateCpedIsAfterPed(any(), any(), any());
    // ACT
    validation.validateCpedIsAfterPed(cped, ped, notification);

    // VERIFY
    assertTrue(notification.getExceptions().contains(CPED_AFTER_PORTFOLIO_PED.toException()));
  }

}