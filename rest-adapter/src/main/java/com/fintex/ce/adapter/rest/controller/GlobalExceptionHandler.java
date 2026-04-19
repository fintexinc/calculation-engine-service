package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.BasePceException;
import com.fintex.ce.model.error.exceptions.CalculationsFailedException;
import com.fintex.wm.commons.error.ErrorResponse;
import com.fintex.wm.commons.error.Notification;
import com.fintex.wm.commons.error.Severity;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Central REST exception handler. Every exception thrown by the calculation pipeline or the HTTP machinery is
 * translated here into an {@link ErrorResponse} payload containing a list of {@link Notification}s.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(CalculationsFailedException.class)
  public ResponseEntity<ErrorResponse> handleCalculationsFailed(CalculationsFailedException e) {
    log.error(e.getMessage(), e);
    List<Notification> notifications = e.getExceptions().stream()
        .map(GlobalExceptionHandler::toNotification)
        .toList();
    HttpStatus status = resolveAggregateHttpStatus(e.getExceptions());
    return ResponseEntity.status(status).body(new ErrorResponse(notifications));
  }

  @ExceptionHandler(BasePceException.class)
  public ResponseEntity<ErrorResponse> handlePceException(BasePceException e) {
    log.error(e.getMessage(), e);
    HttpStatus status = toSpringHttpStatus(e.getErrorCode().getHttpStatus());
    return ResponseEntity.status(status).body(new ErrorResponse(List.of(toNotification(e))));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
    log.error(e.getMessage());
    List<Notification> notifications = e.getBindingResult().getAllErrors().stream()
        .map(GlobalExceptionHandler::toNotification)
        .toList();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(notifications));
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException e) {
    log.error(e.getMessage());
    List<Notification> notifications = e.getAllValidationResults().stream()
        .flatMap(result -> result.getResolvableErrors().stream())
        .map(error -> error instanceof ObjectError oe
            ? toNotification(oe)
            : buildNotification(null, null, error.getDefaultMessage(), Severity.ERROR, null, null, null))
        .toList();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(notifications));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
    log.error(e.getMessage());
    List<Notification> notifications = e.getConstraintViolations().stream()
        .map(GlobalExceptionHandler::toNotification)
        .toList();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(notifications));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
    BasePceException cause = unwrapPceException(e);
    if (cause != null) {
      return handlePceException(cause);
    }
    log.error("Request body is missing or unreadable", e);
    Notification notification = ErrorCode.BAD_INPUT.toNotification();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(List.of(notification)));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
    log.error(e.getMessage(), e);
    Notification notification = buildNotification(null, null, e.getMessage(), Severity.ERROR,
        ErrorCode.BAD_INPUT.getCode(), ErrorCode.BAD_INPUT.getDescription(), ErrorCode.BAD_INPUT.getAction());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(List.of(notification)));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
    log.error("Unexpected error", e);
    Notification notification = ErrorCode.INTERNAL_SERVER_ERROR.asNotification();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(List.of(notification)));
  }

  private static Notification toNotification(BasePceException e) {
    ErrorCode code = e.getErrorCode();
    return code.toNotification(e.getId(), e.getFieldName(), e.getMessage(), e.getMetadata());
  }

  private static Notification toNotification(ObjectError error) {
    String defaultMessage = error.getDefaultMessage();
    String fieldName = error instanceof FieldError fe ? fe.getField() : null;
    return resolveErrorCode(defaultMessage)
        .map(code -> code.toNotificationForField(fieldName, fieldName))
        .orElseGet(() -> buildNotification(null, fieldName, defaultMessage, Severity.ERROR, error.getCode(), null,
            null));
  }

  private static Notification toNotification(ConstraintViolation<?> violation) {
    String message = violation.getMessage();
    String fieldName = extractFieldName(violation.getPropertyPath());
    return resolveErrorCode(message)
        .map(code -> code.toNotificationForField(fieldName, fieldName))
        .orElseGet(() -> buildNotification(null, fieldName, message, Severity.ERROR, null, null, null));
  }

  private static Notification buildNotification(String id, String fieldName, String message,
      Severity severity, String code, String description, String action) {
    return Notification.builder()
        .uuid(id)
        .code(code)
        .message(message)
        .description(description)
        .action(action)
        .severity(severity == null ? Severity.ERROR : severity)
        .fieldName(fieldName)
        .build();
  }

  private static String extractFieldName(Path propertyPath) {
    if (propertyPath == null) {
      return null;
    }
    String name = null;
    for (Path.Node node : propertyPath) {
      name = node.getName();
    }
    return name;
  }

  private static BasePceException unwrapPceException(Throwable throwable) {
    Throwable current = throwable;
    while (current != null) {
      if (current instanceof BasePceException pe) {
        return pe;
      }
      current = current.getCause();
    }
    return null;
  }

  private static Optional<ErrorCode> resolveErrorCode(String value) {
    if (value == null) {
      return Optional.empty();
    }
    for (ErrorCode errorCode : ErrorCode.values()) {
      if (errorCode.getCode().equals(value) || errorCode.name().equals(value)) {
        return Optional.of(errorCode);
      }
    }
    return Optional.empty();
  }

  private static HttpStatus toSpringHttpStatus(com.fintex.wm.commons.error.HttpStatus coreStatus) {
    if (coreStatus == null) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    HttpStatus resolved = HttpStatus.resolve(coreStatus.getValue());
    return resolved == null ? HttpStatus.INTERNAL_SERVER_ERROR : resolved;
  }

  private static HttpStatus resolveAggregateHttpStatus(List<BasePceException> exceptions) {
    HttpStatus result = HttpStatus.OK;
    for (BasePceException exception : exceptions) {
      if (exception.getErrorCode().getSeverity() == Severity.WARNING) {
        continue;
      }
      HttpStatus current = toSpringHttpStatus(exception.getErrorCode().getHttpStatus());
      if (current.is5xxServerError()) {
        return current;
      }
      result = current;
    }
    return result;
  }

}
