package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.adapter.rest.dto.exception.ErrorRes2DTO;
import com.fintex.ce.adapter.rest.dto.exception.RuntimeExceptionDTO;
import com.fintex.ce.domain.exception.DataErrorException;
import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.ce.domain.exception.SystemException;
import com.fintex.ce.domain.exception.code.ErrorCode;
import com.fintex.ce.domain.exception.code.HttpCode;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

  @Test
  void globalExceptionHandler1_checkResult() {
    final var sut = mock(GlobalExceptionHandler.class);
    final var exception = mock(Exception.class);
    final var expectedMessage = "message";

    when(exception.getMessage()).thenReturn(expectedMessage);
    doCallRealMethod().when(sut).globalExceptionHandler1(any(), any());

    final RuntimeExceptionDTO exceptionDTO = sut.globalExceptionHandler1(exception, mock(HttpServletRequest.class));

    assertEquals(1, exceptionDTO.getErrors().size());
    assertTrue(exceptionDTO.getErrors().contains(
        new ErrorRes2DTO(HttpStatus.INTERNAL_SERVER_ERROR.name(), expectedMessage)));
  }

  @Test
  void globalExceptionHandler12_checkResult() {
    final var sut = mock(GlobalExceptionHandler.class);
    final var exception = mock(NullPointerException.class);
    final var expectedMessage = "message";

    when(exception.getMessage()).thenReturn(expectedMessage);
    doCallRealMethod().when(sut).globalExceptionHandler12(any(NullPointerException.class), any());

    final RuntimeExceptionDTO exceptionDTO = sut.globalExceptionHandler12(exception, mock(HttpServletRequest.class));

    assertEquals(1, exceptionDTO.getErrors().size());
    assertTrue(exceptionDTO.getErrors().contains(
        new ErrorRes2DTO(HttpStatus.INTERNAL_SERVER_ERROR.name(), expectedMessage)));
  }

  @Test
  void globalExceptionHandler12_checkResultForDataErrorException() {
    final var sut = mock(GlobalExceptionHandler.class);
    final var exception = mock(DataErrorException.class);
    final var expectedMessage = "message";

    when(exception.getCode()).thenReturn(ErrorCode.ERR_TCH_AHT_001);
    when(exception.getMessage()).thenReturn(expectedMessage);
    when(exception.getHttpStatusCode()).thenReturn(HttpStatus.BAD_REQUEST.value());
    doCallRealMethod().when(sut).globalExceptionHandler12(any(DataErrorException.class), any());

    final RuntimeExceptionDTO exceptionDTO = sut.globalExceptionHandler12(exception, mock(HttpServletRequest.class))
        .getBody();

    assertEquals(1, exceptionDTO.getErrors().size());
    assertTrue(exceptionDTO.getErrors().contains(new ErrorRes2DTO(exception)));
  }

  @Test
  void generalExceptionHandler14_checkResult() {
    final var sut = mock(GlobalExceptionHandler.class);
    final var exception = mock(SystemException.class);
    final var expectedMessage = "message";
    final var expectedError = HttpCode.BAD_REQUEST;

    when(exception.getErrorCode()).thenReturn(expectedError);
    when(exception.getMessage()).thenReturn(expectedMessage);
    doCallRealMethod().when(sut).generalExceptionHandler14(any(), any());

    final RuntimeExceptionDTO response = sut.generalExceptionHandler14(exception, mock(HttpServletRequest.class));

    assertEquals(1, response.getErrors().size());
    assertTrue(response.getErrors().contains(new ErrorRes2DTO(expectedError.name(), expectedMessage)));
  }

  @Test
  void methodArgumentNotValidExceptionHandler_mapsKnownExceptionCode() {
    var sut = new GlobalExceptionHandler();
    var bindingResult = new BeanPropertyBindingResult(new Object(), "command");
    bindingResult.addError(new FieldError("command", "bestWorstTimeIntervalPeriods",
        null, false, null, null, ErrorCode.ERR_BWP_BWPTIP_002.name()));
    var exception = mock(MethodArgumentNotValidException.class);
    when(exception.getMessage()).thenReturn("validation failed");
    when(exception.getBindingResult()).thenReturn(bindingResult);

    RuntimeExceptionDTO response = sut.methodArgumentNotValidExceptionHandler(exception, mock(
        HttpServletRequest.class));

    assertEquals(1, response.getErrors().size());
    assertTrue(response.getErrors().contains(new ErrorRes2DTO(
        null, ErrorCode.ERR_BWP_BWPTIP_002.name(), ErrorCode.ERR_BWP_BWPTIP_002.getMessage())));
  }

  @Test
  void methodArgumentNotValidExceptionHandler_includesFieldNameInMessage() {
    var sut = new GlobalExceptionHandler();
    var bindingResult = new BeanPropertyBindingResult(new Object(), "command");
    bindingResult.addError(new FieldError("command", "currency",
        null, false, null, null, ErrorCode.ERR_VAL_NN_001.name()));
    var exception = mock(MethodArgumentNotValidException.class);
    when(exception.getMessage()).thenReturn("validation failed");
    when(exception.getBindingResult()).thenReturn(bindingResult);

    RuntimeExceptionDTO response = sut.methodArgumentNotValidExceptionHandler(exception, mock(
        HttpServletRequest.class));

    assertEquals(1, response.getErrors().size());
    assertTrue(response.getErrors().contains(new ErrorRes2DTO(
        null, ErrorCode.ERR_VAL_NN_001.name(), "currency must not be null")));
  }

  @Test
  void methodArgumentNotValidExceptionHandler_passesThroughUnknownMessage() {
    var sut = new GlobalExceptionHandler();
    var bindingResult = new BeanPropertyBindingResult(new Object(), "command");
    bindingResult.addError(new FieldError("command", "field",
        null, false, new String[] {"NotNull"}, null, "free-text message"));
    var exception = mock(MethodArgumentNotValidException.class);
    when(exception.getMessage()).thenReturn("validation failed");
    when(exception.getBindingResult()).thenReturn(bindingResult);

    RuntimeExceptionDTO response = sut.methodArgumentNotValidExceptionHandler(exception, mock(
        HttpServletRequest.class));

    assertEquals(1, response.getErrors().size());
    assertTrue(response.getErrors().contains(new ErrorRes2DTO(null, "NotNull", "free-text message")));
  }

  @Test
  void constraintViolationExceptionHandler_mapsKnownExceptionCode() {
    var sut = new GlobalExceptionHandler();
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn(ErrorCode.ERR_RRC_TIP_003.name());
    var exception = new ConstraintViolationException("violation", Set.of(violation));

    RuntimeExceptionDTO response = sut.constraintViolationExceptionHandler(exception, mock(HttpServletRequest.class));

    assertEquals(1, response.getErrors().size());
    assertTrue(response.getErrors().contains(new ErrorRes2DTO(
        null, ErrorCode.ERR_RRC_TIP_003.name(), ErrorCode.ERR_RRC_TIP_003.getMessage())));
  }

  @Test
  void requestValidationExceptionHandler_checkResult() {
    final var sut = mock(GlobalExceptionHandler.class);
    final var exception = mock(ReqValidationException.class);
    final var exceptionId = "id";
    final var exceptionCode = "code";
    final var expectedMessage = "message";

    when(exception.getId()).thenReturn(exceptionId);
    when(exception.getCode()).thenReturn(exceptionCode);
    when(exception.getMessage()).thenReturn(expectedMessage);
    when(exception.getReqValidationExceptions()).thenReturn(List.of(exception));
    doCallRealMethod().when(sut).requestValidationExceptionHandler(any(), any());

    final var response = sut.requestValidationExceptionHandler(exception, mock(HttpServletRequest.class));

    assertEquals(1, response.getErrors().size());
    assertTrue(response.getErrors().contains(new ErrorRes2DTO(exceptionId, exceptionCode, expectedMessage)));
  }

}
