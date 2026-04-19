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
    final var sut = mock(CpedDataValidation.class);
    var notification = new PceExceptionCollector();

    final var cped = LocalDate.now();
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().minusMonths(2);

    doCallRealMethod().when(sut).validate(any(), any(), any(), any());
    // ACT
    sut.validate(cped, psd, ped, notification);

    // VERIFY
    verify(sut).validateCpedIsAfterPed(cped, ped, notification);
  }

  @Test
  void shouldValidate_whenVerifyValidateCpedIsBeforePsd() {
    // SETUP
    final var sut = mock(CpedDataValidation.class);
    var notification = new PceExceptionCollector();

    final var cped = LocalDate.now();
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().minusMonths(2);

    doCallRealMethod().when(sut).validate(any(), any(), any(), any());
    // ACT
    sut.validate(cped, psd, ped, notification);

    // VERIFY
    verify(sut).validateCpedIsBeforePsd(cped, psd, notification);
  }

  @Test
  void shouldValidateCpedIsBeforePsd_whenCpedIsNulLThenNothingShouldHappen() {
    // SETUP
    final var sut = mock(CpedDataValidation.class);
    var notification = new PceExceptionCollector();

    final LocalDate cped = null;
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().minusMonths(2);

    doCallRealMethod().when(sut).validateCpedIsBeforePsd(any(), any(), any());
    // ACT
    sut.validateCpedIsBeforePsd(cped, psd, notification);

    // VERIFY
    verify(sut, never()).getCpedIsBeforePsdExceptionCode();
  }

  @Test
  void shouldValidateCpedIsBeforePsd_whenCpedIsAfterPsdNothingShouldHappen() {
    // SETUP
    final var sut = mock(CpedDataValidation.class);
    var notification = new PceExceptionCollector();

    final var cped = LocalDate.now();
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().minusMonths(2);

    when(sut.getCpedIsBeforePsdExceptionCode()).thenReturn(CPED_BEFORE_PORTFOLIO_PSD);

    doCallRealMethod().when(sut).validateCpedIsBeforePsd(any(), any(), any());
    // ACT
    sut.validateCpedIsBeforePsd(cped, psd, notification);

    // VERIFY
    verify(sut, never()).getCpedIsBeforePsdExceptionCode();
  }

  @Test
  void shouldValidateCpedIsBeforePsd_whenCpedIsBeforePsdErrorShouldBeThrown() {
    // SETUP
    final var sut = mock(CpedDataValidation.class);
    var notification = new PceExceptionCollector();

    final var cped = LocalDate.now();
    final var psd = LocalDate.now().plusMonths(1);
    final var ped = LocalDate.now().minusMonths(2);

    when(sut.getCpedIsBeforePsdExceptionCode()).thenReturn(CPED_BEFORE_PORTFOLIO_PSD);

    doCallRealMethod().when(sut).validateCpedIsBeforePsd(any(), any(), any());
    // ACT
    sut.validateCpedIsBeforePsd(cped, psd, notification);

    // VERIFY
    assertTrue(notification.getExceptions().contains(CPED_BEFORE_PORTFOLIO_PSD.toException()));
  }

  @Test
  void shouldValidateCpedIsAfterPed_whenCpedIsNulLThenNothingShouldHappen() {
    // SETUP
    final var sut = mock(CpedDataValidation.class);
    final PceExceptionCollector notification = new PceExceptionCollector();

    final LocalDate cped = null;
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().minusMonths(2);

    doCallRealMethod().when(sut).validateCpedIsAfterPed(any(), any(), any());
    // ACT
    sut.validateCpedIsAfterPed(cped, ped, notification);

    // VERIFY
    verify(sut, never()).getCpedIsAfterPedExceptionCode();
  }

  @Test
  void shouldValidateCpedIsAfterPed_whenCpedIsBeforePedNothingShouldHappen() {
    // SETUP
    final var sut = mock(CpedDataValidation.class);
    final PceExceptionCollector notification = new PceExceptionCollector();

    final var cped = LocalDate.now();
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().plusMonths(2);

    when(sut.getCpedIsBeforePsdExceptionCode()).thenReturn(CPED_AFTER_PORTFOLIO_PED);

    doCallRealMethod().when(sut).validateCpedIsAfterPed(any(), any(), any());
    // ACT
    sut.validateCpedIsAfterPed(cped, ped, notification);

    // VERIFY
    verify(sut, never()).getCpedIsAfterPedExceptionCode();
  }

  @Test
  void shouldValidateCpedIsAfterPed_whenCpedIsAfterPedErrorShouldBeThrown() {
    // SETUP
    final var sut = mock(CpedDataValidation.class);
    final PceExceptionCollector notification = new PceExceptionCollector();

    final var cped = LocalDate.now();
    final var psd = LocalDate.now().plusMonths(1);
    final var ped = LocalDate.now().minusMonths(2);

    when(sut.getCpedIsAfterPedExceptionCode()).thenReturn(CPED_AFTER_PORTFOLIO_PED);

    doCallRealMethod().when(sut).validateCpedIsAfterPed(any(), any(), any());
    // ACT
    sut.validateCpedIsAfterPed(cped, ped, notification);

    // VERIFY
    assertTrue(notification.getExceptions().contains(CPED_AFTER_PORTFOLIO_PED.toException()));
  }

}