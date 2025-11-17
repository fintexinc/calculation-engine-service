package com.fintex.ce.util.validation.request.chainofresponsibility;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class NotIncludePropertyAbstractReqValidationTest {

    @Test
    void check_throwException() {
        //SETUP
        final var sut = mock(NotIncludePropertyAbstractReqValidation.class, withSettings().useConstructor(new Object()));

        doNothing().when(sut).throwException();

        doCallRealMethod().when(sut).check();
        //ACT
        sut.check();

        //VERIFY
        verify(sut).throwException();
    }

    @Test
    void check_validCase() {
        //SETUP
        final var sut = mock(NotIncludePropertyAbstractReqValidation.class, withSettings().useConstructor((String) null));

        doNothing().when(sut).throwException();

        doCallRealMethod().when(sut).check();
        //ACT
        sut.check();

        //VERIFY
        verify(sut, times(0)).throwException();
    }



}