package com.fintex.ce.application.returns;

import com.fintex.ce.application.validation.CpsdDataValidation;
import com.fintex.ce.model.error.PceExceptionCollector;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.fintex.ce.model.error.ErrorCode.CPED_AFTER_PORTFOLIO_PED;
import static com.fintex.ce.model.error.ErrorCode.CPED_BEFORE_PORTFOLIO_PSD;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CpsdDataValidationTest {

  @Test
  void shouldValidate_whenCpsdIsNulLThenNothingShouldHappen() {
    // SETUP
    final var validation = mock(CpsdDataValidation.class);
    var notification = new PceExceptionCollector();

    final LocalDate cpsd = null;
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().minusMonths(2);

    doCallRealMethod().when(validation).validate(any(), any(), any(), any());
    // ACT
    validation.validate(cpsd, psd, ped, notification);

    // VERIFY
    verify(validation, never()).getCpsdIsAfterPedExceptionCode();
    verify(validation, never()).getCpsdIsBeforePsdExceptionCode();
  }

  @Test
  void shouldValidate_whenCpsdIsBeforePedNothingShouldHappen() {
    // SETUP
    final var validation = mock(CpsdDataValidation.class);
    var notification = new PceExceptionCollector();

    final var cpsd = LocalDate.now();
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().plusMonths(10);

    when(validation.getCpsdIsAfterPedExceptionCode()).thenReturn(CPED_BEFORE_PORTFOLIO_PSD);

    doCallRealMethod().when(validation).validate(any(), any(), any(), any());
    // ACT
    validation.validate(cpsd, psd, ped, notification);

    // VERIFY
    verify(validation, never()).getCpsdIsAfterPedExceptionCode();
  }

  @Test
  void shouldValidate_whenCpsdIsAfterPedErrorShouldBeThrown() {
    // SETUP
    final var validation = mock(CpsdDataValidation.class);
    var notification = new PceExceptionCollector();

    final var cpsd = LocalDate.now();
    final var psd = LocalDate.now().plusMonths(1);
    final var ped = LocalDate.now().minusMonths(2);

    when(validation.getCpsdIsAfterPedExceptionCode()).thenReturn(CPED_BEFORE_PORTFOLIO_PSD);
    when(validation.getCpsdIsBeforePsdExceptionCode()).thenReturn(CPED_AFTER_PORTFOLIO_PED);

    doCallRealMethod().when(validation).validate(any(), any(), any(), any());
    // ACT
    validation.validate(cpsd, psd, ped, notification);

    // VERIFY
    assertTrue(notification.getExceptions().contains(CPED_BEFORE_PORTFOLIO_PSD.toException()));
  }

  @Test
  void shouldValidate_whenCpsdIsAfterPsdNothingShouldHappen() {
    // SETUP
    final var validation = mock(CpsdDataValidation.class);
    var notification = new PceExceptionCollector();

    final var cpsd = LocalDate.now();
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().plusMonths(2);

    when(validation.getCpsdIsBeforePsdExceptionCode()).thenReturn(CPED_AFTER_PORTFOLIO_PED);

    doCallRealMethod().when(validation).validate(any(), any(), any(), any());
    // ACT
    validation.validate(cpsd, psd, ped, notification);

    // VERIFY
    verify(validation, never()).getCpsdIsBeforePsdExceptionCode();
  }

  @Test
  void shouldValidate_whenCpsdIsBeforePsdErrorShouldBeThrown() {
    // SETUP
    final var validation = mock(CpsdDataValidation.class);
    var notification = new PceExceptionCollector();

    final var cpsd = LocalDate.now();
    final var psd = LocalDate.now().plusMonths(1);
    final var ped = LocalDate.now().plusMonths(10);

    when(validation.getCpsdIsBeforePsdExceptionCode()).thenReturn(CPED_AFTER_PORTFOLIO_PED);

    doCallRealMethod().when(validation).validate(any(), any(), any(), any());
    // ACT
    validation.validate(cpsd, psd, ped, notification);

    // VERIFY
    assertTrue(notification.getExceptions().contains(CPED_AFTER_PORTFOLIO_PED.toException()));
  }

}