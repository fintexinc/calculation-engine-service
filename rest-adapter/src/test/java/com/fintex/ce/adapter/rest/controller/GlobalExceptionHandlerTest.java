package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.ce.model.error.exceptions.CalculationsFailedException;
import com.fintex.wm.commons.error.ErrorResponse;
import com.fintex.wm.commons.error.Notification;
import com.fintex.wm.commons.error.Severity;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

  private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void unexpectedException_returns500WithGenericNotificationAndNoLeakedMessage() {
    Exception exception = new RuntimeException("internal stack trace that must not leak");

    ResponseEntity<ErrorResponse> response = handler.handleUnexpected(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getNotifications()).hasSize(1);
    Notification notification = response.getBody().getNotifications().get(0);
    assertThat(notification.getCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getCode());
    assertThat(notification.getMessage()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    assertThat(notification.getMessage()).doesNotContain("internal stack trace");
    assertThat(notification.getSeverity()).isEqualTo(Severity.ERROR);
  }

  @Test
  void calculationException_returnsMatchingHttpStatus() {
    CalculationException exception = ErrorCode.ACCUMULATE_HOLDING_TYPES_EXCEED_MAX.toException();

    ResponseEntity<ErrorResponse> response = handler.handlePceException(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getNotifications()).hasSize(1);
    Notification notification = response.getBody().getNotifications().get(0);
    assertThat(notification.getCode()).isEqualTo(ErrorCode.ACCUMULATE_HOLDING_TYPES_EXCEED_MAX.getCode());
    assertThat(notification.getSeverity()).isEqualTo(Severity.ERROR);
  }

  @Test
  void calculationsFailedException_aggregatesAllNotifications() {
    CalculationException error1 = ErrorCode.TIME_INTERVAL_PERIOD_NOT_POSITIVE.toException();
    CalculationException error2 = ErrorCode.REQUEST_CONTAINS_CUSTOM_INTERVAL_PSD.toException();
    CalculationsFailedException composite = new CalculationsFailedException(List.of(error1, error2));

    ResponseEntity<ErrorResponse> response = handler.handleCalculationsFailed(composite);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getNotifications()).hasSize(2);
    assertThat(response.getBody().getNotifications())
        .extracting(Notification::getCode, Notification::getMessage, Notification::getSeverity)
        .containsExactly(
            tuple(ErrorCode.TIME_INTERVAL_PERIOD_NOT_POSITIVE.getCode(),
                ErrorCode.TIME_INTERVAL_PERIOD_NOT_POSITIVE.getMessage(), Severity.ERROR),
            tuple(ErrorCode.REQUEST_CONTAINS_CUSTOM_INTERVAL_PSD.getCode(),
                ErrorCode.REQUEST_CONTAINS_CUSTOM_INTERVAL_PSD.getMessage(), Severity.ERROR));
  }

  @Test
  void methodArgumentNotValid_mapsKnownErrorCode() {
    var bindingResult = new BeanPropertyBindingResult(new Object(), "command");
    bindingResult.addError(new FieldError("command", "holdings",
        null, false, null, null, ErrorCode.HOLDING_VALUE_NEGATIVE_OR_NULL.name()));
    var exception = mock(MethodArgumentNotValidException.class);
    when(exception.getMessage()).thenReturn("validation failed");
    when(exception.getBindingResult()).thenReturn(bindingResult);

    ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValid(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getNotifications()).hasSize(1);
    assertThat(response.getBody().getNotifications().get(0).getCode())
        .isEqualTo(ErrorCode.HOLDING_VALUE_NEGATIVE_OR_NULL.getCode());
  }

  @Test
  void methodArgumentNotValid_includesFieldNameWhenTemplateHasPlaceholder() {
    var bindingResult = new BeanPropertyBindingResult(new Object(), "command");
    bindingResult.addError(new FieldError("command", "currency",
        null, false, null, null, ErrorCode.FIELD_NOT_NULL.getCode()));
    var exception = mock(MethodArgumentNotValidException.class);
    when(exception.getMessage()).thenReturn("validation failed");
    when(exception.getBindingResult()).thenReturn(bindingResult);

    ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValid(exception);

    assertThat(response.getBody()).isNotNull();
    Notification notification = response.getBody().getNotifications().get(0);
    assertThat(notification.getCode()).isEqualTo(ErrorCode.FIELD_NOT_NULL.getCode());
    assertThat(notification.getMessage()).isEqualTo("currency must not be null");
    assertThat(notification.getFieldName()).isEqualTo("currency");
  }

  @Test
  void methodArgumentNotValid_passesThroughUnknownMessage() {
    var bindingResult = new BeanPropertyBindingResult(new Object(), "command");
    bindingResult.addError(new FieldError("command", "field",
        null, false, new String[] {"NotNull"}, null, "free-text message"));
    var exception = mock(MethodArgumentNotValidException.class);
    when(exception.getMessage()).thenReturn("validation failed");
    when(exception.getBindingResult()).thenReturn(bindingResult);

    ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValid(exception);

    assertThat(response.getBody()).isNotNull();
    Notification notification = response.getBody().getNotifications().get(0);
    assertThat(notification.getMessage()).isEqualTo("free-text message");
    assertThat(notification.getFieldName()).isEqualTo("field");
  }

  @Test
  void constraintViolation_mapsKnownErrorCode() {
    Set<ConstraintViolation<TimeIntervalBean>> violations = VALIDATOR.validate(new TimeIntervalBean(0));
    var exception = new ConstraintViolationException("violation", violations);

    ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getNotifications()).hasSize(1);
    Notification notification = response.getBody().getNotifications().get(0);
    assertThat(notification.getCode()).isEqualTo(ErrorCode.TIME_INTERVAL_PERIOD_NOT_POSITIVE.getCode());
    assertThat(notification.getFieldName()).isEqualTo("timeIntervalPeriods");
  }

  private record TimeIntervalBean(
      @Min(value = 1, message = "TIME_INTERVAL_PERIOD_NOT_POSITIVE") int timeIntervalPeriods) {
  }
}
