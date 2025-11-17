package com.fintex.ce.util.validation.request.chainofresponsibility;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

class LastDayOfMonthAbstractReqValidatorTest {

    @Test
    void check_verifyThrowException() {
        //SETUP
        final var sut = mock(LastDayOfMonthAbstractReqValidator.class, withSettings()
                .useConstructor(LocalDate.of(200, 5, 5)));

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
        final var sut = mock(LastDayOfMonthAbstractReqValidator.class, withSettings()
                .useConstructor(LocalDate.of(200, 5, 31)));

        doNothing().when(sut).throwException();

        doCallRealMethod().when(sut).check();
        //ACT
        sut.check();

        //VERIFY
        verify(sut, times(0)).throwException();
    }

    @Test
    void check_validCase2() {
        //SETUP
        final var sut = mock(LastDayOfMonthAbstractReqValidator.class, withSettings()
                .useConstructor((String) null));

        doNothing().when(sut).throwException();

        doCallRealMethod().when(sut).check();
        //ACT
        sut.check();

        //VERIFY
        verify(sut, times(0)).throwException();
    }


}