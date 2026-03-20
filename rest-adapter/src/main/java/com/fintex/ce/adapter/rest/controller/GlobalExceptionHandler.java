package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.adapter.rest.dto.exception.ErrorRes2DTO;
import com.fintex.ce.adapter.rest.dto.exception.RuntimeExceptionDTO;
import com.fintex.ce.domain.exception.DataErrorException;
import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.ce.domain.exception.SystemException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

}
