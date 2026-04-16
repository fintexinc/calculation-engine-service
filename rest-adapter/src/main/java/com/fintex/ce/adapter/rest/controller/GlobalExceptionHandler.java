package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.adapter.rest.dto.exception.ErrorRes2DTO;
import com.fintex.ce.adapter.rest.dto.exception.RuntimeExceptionDTO;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.DataErrorException;
import com.fintex.ce.model.error.exceptions.ReqValidationException;
import com.fintex.ce.model.error.exceptions.SystemException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(value = {Exception.class})
  @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
  public RuntimeExceptionDTO globalExceptionHandler1(final Exception e, final HttpServletRequest request) {
    log.error("Occurred error", e);
    final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    return new RuntimeExceptionDTO(List.of(new ErrorRes2DTO(status.name(), e.getMessage())));
  }

  @ExceptionHandler(value = {NullPointerException.class})
  @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
  public RuntimeExceptionDTO globalExceptionHandler12(final NullPointerException e, final HttpServletRequest request) {
    log.error("Occurred error", e);
    final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    return new RuntimeExceptionDTO(List.of(new ErrorRes2DTO(status.name(), e.getMessage())));
  }

  @ExceptionHandler(value = {DataErrorException.class})
  public ResponseEntity<RuntimeExceptionDTO> globalExceptionHandler12(final DataErrorException e,
      final HttpServletRequest request) {
    log.error(e.getMessage());
    return new ResponseEntity<>(new RuntimeExceptionDTO(List.of(new ErrorRes2DTO(e))), HttpStatus.resolve(e
        .getHttpStatusCode()));
  }

  @ExceptionHandler(SystemException.class)
  @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
  public RuntimeExceptionDTO generalExceptionHandler14(final SystemException e, final HttpServletRequest request) {
    log.error("Occurred error", e);
    final HttpStatus status = HttpStatus.resolve(e.getErrorCode().getHttpStatusCode());
    return new RuntimeExceptionDTO(List.of(new ErrorRes2DTO(status.name(), e.getMessage())));
  }

  @ExceptionHandler(ReqValidationException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public RuntimeExceptionDTO requestValidationExceptionHandler(final ReqValidationException e,
      final HttpServletRequest request) {
    log.error(e.getMessage());

    final List<ErrorRes2DTO> errors = e.getReqValidationExceptions()
        .stream()
        .map(ve -> new ErrorRes2DTO(ve.getId(), ve.getCode(), ve.getMessage()))
        .collect(Collectors.toList());

    return new RuntimeExceptionDTO(errors);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public RuntimeExceptionDTO methodArgumentNotValidExceptionHandler(final MethodArgumentNotValidException e,
      final HttpServletRequest request) {
    log.error(e.getMessage());

    final List<ErrorRes2DTO> errors = e.getBindingResult().getAllErrors().stream()
        .map(GlobalExceptionHandler::toErrorRes2DTO)
        .collect(Collectors.toList());

    return new RuntimeExceptionDTO(errors);
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public RuntimeExceptionDTO handlerMethodValidationExceptionHandler(final HandlerMethodValidationException e,
      final HttpServletRequest request) {
    log.error(e.getMessage());

    final List<ErrorRes2DTO> errors = e.getAllValidationResults().stream()
        .flatMap(result -> result.getResolvableErrors().stream())
        .map(error -> error instanceof ObjectError oe
            ? toErrorRes2DTO(oe)
            : new ErrorRes2DTO(null, null, error.getDefaultMessage()))
        .collect(Collectors.toList());

    return new RuntimeExceptionDTO(errors);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public RuntimeExceptionDTO constraintViolationExceptionHandler(final ConstraintViolationException e,
      final HttpServletRequest request) {
    log.error(e.getMessage());

    final List<ErrorRes2DTO> errors = e.getConstraintViolations().stream()
        .map(GlobalExceptionHandler::toErrorRes2DTO)
        .collect(Collectors.toList());

    return new RuntimeExceptionDTO(errors);
  }

  private static ErrorRes2DTO toErrorRes2DTO(ObjectError error) {
    String defaultMessage = error.getDefaultMessage();
    String fieldName = error instanceof FieldError fe ? fe.getField() : null;
    return resolveExceptionCode(defaultMessage)
        .map(code -> new ErrorRes2DTO(null, code.name(), formatMessage(code.getMessage(), fieldName)))
        .orElseGet(() -> new ErrorRes2DTO(null, error.getCode(), defaultMessage));
  }

  private static ErrorRes2DTO toErrorRes2DTO(ConstraintViolation<?> violation) {
    String message = violation.getMessage();
    String fieldName = extractFieldName(violation.getPropertyPath());
    return resolveExceptionCode(message)
        .map(code -> new ErrorRes2DTO(null, code.name(), formatMessage(code.getMessage(), fieldName)))
        .orElseGet(() -> new ErrorRes2DTO(null, null, message));
  }

  private static String formatMessage(String messageTemplate, String fieldName) {
    if (fieldName == null || !messageTemplate.contains("%s")) {
      return messageTemplate;
    }
    return String.format(messageTemplate, fieldName);
  }

  private static String extractFieldName(jakarta.validation.Path propertyPath) {
    if (propertyPath == null) {
      return null;
    }
    String name = null;
    for (jakarta.validation.Path.Node node : propertyPath) {
      name = node.getName();
    }
    return name;
  }

  private static java.util.Optional<ErrorCode> resolveExceptionCode(String message) {
    if (message == null) {
      return java.util.Optional.empty();
    }
    try {
      return java.util.Optional.of(ErrorCode.valueOf(message));
    } catch (IllegalArgumentException ex) {
      return java.util.Optional.empty();
    }
  }

}
