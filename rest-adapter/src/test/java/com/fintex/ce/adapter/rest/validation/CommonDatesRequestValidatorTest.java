package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.domain.dto.command.MultiplePortfoliosCommand;
import com.fintex.ce.domain.model.holding.Holding;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CommonDatesRequestValidatorTest {

  @Test
  void validate_verifyHoldingReqValidationCheck() {
    // SETUP
    final var sut = mock(CommonDatesRequestValidator.class);

    final List<Holding> benchmarkHoldings = List.of(mock(Holding.class));
    final List<Holding> portfolioHoldings = List.of();
    final var portfolio = new MultiplePortfoliosCommand.Portfolio(portfolioHoldings);

    final HoldingReqValidation reqValidation = mock(HoldingReqValidation.class);
    doReturn(reqValidation).when(sut).buildHoldingReqValidation(benchmarkHoldings);

    doCallRealMethod().when(sut).validate(anyList(), anySet());
    // ACT
    sut.validate(benchmarkHoldings, Set.of(portfolio));

    // VERIFY
    verify(reqValidation).check();
  }

  @Test
  void validate_verifyPortfolioPreValidationForBenchmarks() {
    // SETUP
    final var sut = mock(CommonDatesRequestValidator.class);
    final List<Holding> benchmarkHoldings = List.of();
    final List<Holding> portfolioHoldings = List.of(mock(Holding.class));
    final var portfolio = new MultiplePortfoliosCommand.Portfolio(portfolioHoldings);

    final HoldingReqValidation reqValidation = mock(HoldingReqValidation.class);
    doReturn(reqValidation).when(sut).buildHoldingReqValidation(portfolioHoldings);

    doCallRealMethod().when(sut).validate(anyList(), anySet());
    // ACT
    sut.validate(benchmarkHoldings, Set.of(portfolio));

    // VERIFY
    verify(reqValidation).check();
  }

  @Test
  void buildHoldingReqValidation_checkResult() {
    // SETUP
    final var sut = mock(CommonDatesRequestValidator.class);

    final List<Holding> portfolioHoldings = List.of(mock(Holding.class));
    final var expected = new HoldingReqValidation(portfolioHoldings);

    doCallRealMethod().when(sut).buildHoldingReqValidation(anyList());
    // ACT
    final HoldingReqValidation actual = sut.buildHoldingReqValidation(portfolioHoldings);

    // VERIFY
    assertEquals(expected, actual);
  }
}
