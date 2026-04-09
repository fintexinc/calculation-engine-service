package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.ce.domain.model.holding.Holding;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HoldingsCouldNotBeEmptyAbstractReqValidationTest {

  @Test
  void check_holdingsIsNull() {
    // SETUP
    final var sut = mock(HoldingsCouldNotBeEmptyAbstractReqValidation.class, withSettings()
        .useConstructor((List) null));

    final var expected = new ReqValidationException("str");

    doReturn("str").when(sut).getMessage();

    doCallRealMethod().when(sut).check();
    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void check_holdingsIsEmpty() {
    // SETUP
    final var sut = mock(HoldingsCouldNotBeEmptyAbstractReqValidation.class, withSettings()
        .useConstructor(List.of()));

    final var expected = new ReqValidationException("str");

    doReturn("str").when(sut).getMessage();

    doCallRealMethod().when(sut).check();
    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void check_validCase() {
    // SETUP
    final var sut = mock(HoldingsCouldNotBeEmptyAbstractReqValidation.class, withSettings()
        .useConstructor(List.of(mock(Holding.class))));

    doReturn("str").when(sut).getMessage();

    doCallRealMethod().when(sut).check();
    // ACT
    assertDoesNotThrow(() -> sut.check());

    // VERIFY
  }

}