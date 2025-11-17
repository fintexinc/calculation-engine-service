package com.fintex.ce.rest;

import com.fintex.ce.config.enumeration.ExceptionCode;
import com.fintex.ce.dto.exception.ErrorRes2DTO;
import com.fintex.ce.dto.exception.RuntimeExceptionDTO;
import com.fintex.ce.exception.DataErrorException;
import com.fintex.ce.exception.ReqValidationException;
import com.fintex.ce.exception.SystemException;
import com.fintex.ce.exception.code.ErrorCode;
import com.fintex.ce.service.interfaces.ExceptionHandlingService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class GlobalExceptionHandlerTest {

    GlobalExceptionHandlerTest() {
    }

    @Test
    void globalExceptionHandler1_checkResult() {
        //SETUP
        final var sut = mock(GlobalExceptionHandler.class);
        final var exception = mock(Exception.class);
        final var expectedMessage = "message";

        when(exception.getMessage()).thenReturn(expectedMessage);
        doCallRealMethod().when(sut).globalExceptionHandler1(any(), any());

        //ACT
        final RuntimeExceptionDTO exceptionDTO =
                sut.globalExceptionHandler1(exception, mock(HttpServletRequest.class));

        //VERIFY
        assertEquals(1, exceptionDTO.getErrors().size());
        assertTrue(exceptionDTO.getErrors().contains(
                new ErrorRes2DTO(HttpStatus.INTERNAL_SERVER_ERROR.name(), expectedMessage)));
    }

    @Test
    void globalExceptionHandler12_checkResult() {
        //SETUP
        final var sut = mock(GlobalExceptionHandler.class);
        final var exception = mock(NullPointerException.class);
        final var expectedMessage = "message";

        when(exception.getMessage()).thenReturn(expectedMessage);
        doCallRealMethod().when(sut).globalExceptionHandler12(any(NullPointerException.class), any());

        //ACT
        final RuntimeExceptionDTO exceptionDTO =
                sut.globalExceptionHandler12(exception, mock(HttpServletRequest.class));

        //VERIFY
        assertEquals(1, exceptionDTO.getErrors().size());
        assertTrue(exceptionDTO.getErrors().contains(
                new ErrorRes2DTO(HttpStatus.INTERNAL_SERVER_ERROR.name(), expectedMessage)));
    }

    @Test
    void globalExceptionHandler12_checkResult1() {
        //SETUP
        final var exceptionHandlingService = mock(ExceptionHandlingService.class);
        final var sut = mock(GlobalExceptionHandler.class, withSettings().useConstructor(exceptionHandlingService));

        final var exception = mock(DataErrorException.class);
        final var expectedMessage = "message";

        when(exception.getCode()).thenReturn(ExceptionCode.ERR_TCH_AHT_001);
        when(exception.getMessage()).thenReturn(expectedMessage);
        when(exception.getHttpStatus()).thenReturn(HttpStatus.BAD_REQUEST);
        doCallRealMethod().when(sut).globalExceptionHandler12(any(DataErrorException.class), any());
        doNothing().when(exceptionHandlingService).ifFxRatesErrorRemoveFxRatesFromRedisCache(exception);

        //ACT
        final RuntimeExceptionDTO exceptionDTO =
                sut.globalExceptionHandler12(exception, mock(HttpServletRequest.class)).getBody();

        //VERIFY
        assertEquals(1, exceptionDTO.getErrors().size());
        assertTrue(exceptionDTO.getErrors().contains(new ErrorRes2DTO(exception)));
    }

    @Test
    void globalExceptionHandler12_verifyIfFxRatesErrorRemoveFxRatesFromRedisCache() {
        //SETUP
        final var exceptionHandlingService = mock(ExceptionHandlingService.class);
        final var sut = mock(GlobalExceptionHandler.class,  withSettings().useConstructor(exceptionHandlingService));
        final var exception = mock(DataErrorException.class);
        final var expectedMessage = "message";

        when(exception.getCode()).thenReturn(ExceptionCode.ERR_TCH_AHT_001);
        when(exception.getMessage()).thenReturn(expectedMessage);
        when(exception.getHttpStatus()).thenReturn(HttpStatus.BAD_REQUEST);
        doCallRealMethod().when(sut).globalExceptionHandler12(any(DataErrorException.class), any());

        //ACT
        sut.globalExceptionHandler12(exception, mock(HttpServletRequest.class)).getBody();

        //VERIFY
        verify(exceptionHandlingService).ifFxRatesErrorRemoveFxRatesFromRedisCache(same(exception));
    }

    @Test
    void generalExceptionHandler14_checkResult() {
        //SETUP
        final var sut = mock(GlobalExceptionHandler.class);
        final var exception = mock(SystemException.class);
        final var expectedMessage = "message";
        final var expectedError = ErrorCode.BAD_REQUEST;

        when(exception.getErrorCode()).thenReturn(expectedError);
        when(exception.getMessage()).thenReturn(expectedMessage);
        doCallRealMethod().when(sut).generalExceptionHandler14(any(), any());

        //ACT
        final RuntimeExceptionDTO response = sut.generalExceptionHandler14(exception, mock(HttpServletRequest.class));

        //VERIFY
        verify(exception).getErrorCode();
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().contains(new ErrorRes2DTO(expectedError.name(), expectedMessage)));
    }

    @Test
    void requestValidationExceptionHandler_checkResult() {
        //SETUP
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

        //ACT
        final var response = sut.requestValidationExceptionHandler(exception, mock(HttpServletRequest.class));

        //VERIFY
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().contains(new ErrorRes2DTO(exceptionId, exceptionCode, expectedMessage)));
    }

}
