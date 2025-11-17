package com.fintex.ce.util.validation.request.chainofresponsibility;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class DateGreaterThanDateAbstractReqValidationTest {

    @Test
    void check_verifyThrowException() {
        //SETUP
        final var sut = mock(DateGreaterThanDateAbstractReqValidation.class, withSettings()
                .useConstructor(LocalDate.now(), LocalDate.now().minusMonths(1)));

        doNothing().when(sut).throwException();

        doCallRealMethod().when(sut).check();
        //ACT
        sut.check();

        //VERIFY
        verify(sut).throwException();
    }

    @Test
    void check_validCase1() {
        //SETUP
        final var sut = mock(DateGreaterThanDateAbstractReqValidation.class, withSettings()
                .useConstructor(LocalDate.now(), LocalDate.now().plusMonths(1)));

        //ACT
        assertDoesNotThrow(sut::check);

        //VERIFY
    }

    @Test
    void check_validCase2() {
        //SETUP
        final var sut = mock(DateGreaterThanDateAbstractReqValidation.class, withSettings()
                .useConstructor(null, LocalDate.now().plusMonths(1)));

        //ACT
        assertDoesNotThrow(sut::check);

        //VERIFY
    }

    @Test
    void check_validCase3() {
        //SETUP
        final var sut = mock(DateGreaterThanDateAbstractReqValidation.class, withSettings()
                .useConstructor(LocalDate.now(), null));

        //ACT
        assertDoesNotThrow(sut::check);

        //VERIFY
    }



}