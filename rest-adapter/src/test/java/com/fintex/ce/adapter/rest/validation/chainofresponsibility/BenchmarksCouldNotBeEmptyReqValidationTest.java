package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.ce.domain.model.holding.Holding;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class BenchmarksCouldNotBeEmptyReqValidationTest {

  @Test
  void check_holdingsIsEmptyExpectReqValidationException() {
    // SETUP
    final var sut = new BenchmarksCouldNotBeEmptyReqValidation(List.of());

    final ReqValidationException expected = new ReqValidationException(sut.getMessage());

    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, sut::check);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void check_holdingsIsNullExpectReqValidationException() {
    // SETUP
    final var sut = new BenchmarksCouldNotBeEmptyReqValidation(List.of());

    final ReqValidationException expected = new ReqValidationException(sut.getMessage());

    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, sut::check);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void check_validCase() {
    // SETUP
    final var sut = new BenchmarksCouldNotBeEmptyReqValidation(List.of(mock(Holding.class)));

    // ACT
    sut.check();

    // VERIFY
  }

}