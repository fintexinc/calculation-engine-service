package com.fintex.ce.util.validation.request.chainofresponsibility;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.fintex.ce.config.enumeration.Period.YEAR_TO_DATE;
import static org.mockito.Mockito.*;

class PeriodsNotContainingAbstractReqValidationTest {

    @Test
    void check_periodsContainsNotAllowedPeriod() {
        //SETUP
        final var sut = mock(PeriodsNotContainingAbstractReqValidation.class, withSettings()
                .useConstructor(Set.of(YEAR_TO_DATE.name())));

        doReturn(YEAR_TO_DATE).when(sut).getNotAllowedPeriod();

        doCallRealMethod().when(sut).check();
        //ACT
        sut.check();

        //VERIFY
        verify(sut).throwException();
    }

    @Test
    void check_validCase1() {
        //SETUP
        final var sut = mock(PeriodsNotContainingAbstractReqValidation.class, withSettings()
                .useConstructor(Set.of()));

        //ACT
        sut.check();

        //VERIFY
        verify(sut, times(0)).throwException();
    }

    @Test
    void check_validCase2() {
        //SETUP
        final var sut = mock(PeriodsNotContainingAbstractReqValidation.class, withSettings()
                .useConstructor(Set.of("1")));

        //ACT
        sut.check();

        //VERIFY
        verify(sut, times(0)).throwException();
    }

}