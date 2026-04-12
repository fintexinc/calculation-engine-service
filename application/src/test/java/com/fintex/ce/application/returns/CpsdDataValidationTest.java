package com.fintex.ce.application.returns;

import com.fintex.ce.application.validation.CpsdDataValidation;
import com.fintex.ce.domain.exception.notification.pattern.Notification;

import org.springframework.http.HttpStatus;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.fintex.ce.domain.exception.code.ErrorCode.ERR_RRC_CPED_002;
import static com.fintex.ce.domain.exception.code.ErrorCode.ERR_RRC_CPED_003;
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
    final var sut = mock(CpsdDataValidation.class);
    var notification = new Notification();

    final LocalDate cpsd = null;
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().minusMonths(2);

    doCallRealMethod().when(sut).validate(any(), any(), any(), any());
    // ACT
    sut.validate(cpsd, psd, ped, notification);

    // VERIFY
    verify(sut, never()).getCpsdIsAfterPedExceptionCode();
    verify(sut, never()).getCpsdIsBeforePsdExceptionCode();
  }

  @Test
  void shouldValidate_whenCpsdIsBeforePedNothingShouldHappen() {
    // SETUP
    final var sut = mock(CpsdDataValidation.class);
    var notification = new Notification();

    final var cpsd = LocalDate.now();
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().plusMonths(10);

    when(sut.getCpsdIsAfterPedExceptionCode()).thenReturn(ERR_RRC_CPED_002);

    doCallRealMethod().when(sut).validate(any(), any(), any(), any());
    // ACT
    sut.validate(cpsd, psd, ped, notification);

    // VERIFY
    verify(sut, never()).getCpsdIsAfterPedExceptionCode();
  }

  @Test
  void shouldValidate_whenCpsdIsAfterPedErrorShouldBeThrown() {
    // SETUP
    final var sut = mock(CpsdDataValidation.class);
    var notification = new Notification();

    final var cpsd = LocalDate.now();
    final var psd = LocalDate.now().plusMonths(1);
    final var ped = LocalDate.now().minusMonths(2);

    when(sut.getCpsdIsAfterPedExceptionCode()).thenReturn(ERR_RRC_CPED_002);
    when(sut.getCpsdIsBeforePsdExceptionCode()).thenReturn(ERR_RRC_CPED_003);

    doCallRealMethod().when(sut).validate(any(), any(), any(), any());
    // ACT
    sut.validate(cpsd, psd, ped, notification);

    // VERIFY
    assertTrue(notification.getErrors().contains(ERR_RRC_CPED_002.error(HttpStatus.BAD_REQUEST)));
  }

  @Test
  void shouldValidate_whenCpsdIsAfterPsdNothingShouldHappen() {
    // SETUP
    final var sut = mock(CpsdDataValidation.class);
    var notification = new Notification();

    final var cpsd = LocalDate.now();
    final var psd = LocalDate.now().minusMonths(1);
    final var ped = LocalDate.now().plusMonths(2);

    when(sut.getCpsdIsBeforePsdExceptionCode()).thenReturn(ERR_RRC_CPED_003);

    doCallRealMethod().when(sut).validate(any(), any(), any(), any());
    // ACT
    sut.validate(cpsd, psd, ped, notification);

    // VERIFY
    verify(sut, never()).getCpsdIsBeforePsdExceptionCode();
  }

  @Test
  void shouldValidate_whenCpsdIsBeforePsdErrorShouldBeThrown() {
    // SETUP
    final var sut = mock(CpsdDataValidation.class);
    var notification = new Notification();

    final var cpsd = LocalDate.now();
    final var psd = LocalDate.now().plusMonths(1);
    final var ped = LocalDate.now().plusMonths(10);

    when(sut.getCpsdIsBeforePsdExceptionCode()).thenReturn(ERR_RRC_CPED_003);

    doCallRealMethod().when(sut).validate(any(), any(), any(), any());
    // ACT
    sut.validate(cpsd, psd, ped, notification);

    // VERIFY
    assertTrue(notification.getErrors().contains(ERR_RRC_CPED_003.error(HttpStatus.BAD_REQUEST)));
  }

}