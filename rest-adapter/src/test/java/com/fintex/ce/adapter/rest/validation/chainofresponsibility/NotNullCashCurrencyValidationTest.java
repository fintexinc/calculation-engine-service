package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.ce.domain.model.holding.CashHolding;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.fintex.ce.domain.enumeration.ExceptionCode.ERR_RRC_MC_002;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotNullCashCurrencyValidationTest {

  @Test
  void check_ERR_RRC_MC_002ThrownWhenInterestRateIsNull() {
    // SETUP
    final var sut = new NotNullCashCurrencyValidation(List.of(new CashHolding()));

    final ReqValidationException expected = ERR_RRC_MC_002.reqValidationError();

    // ACT
    final ReqValidationException actual = assertThrows(ReqValidationException.class, sut::check);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void checkValidCashHolding() {
    // SETUP
    final CashHolding cashHolding = new CashHolding();
    cashHolding.setCurrency(Currency.USD);

    final var sut = new NotNullCashCurrencyValidation(List.of());

    // ACT
    // VERIFY
    assertDoesNotThrow(sut::check);
  }

  @Test
  void check_exceptionNotThrownBecausePortfolioContainsSeveralCashHoldings() {
    // SETUP
    final CashHolding cashHolding1 = new CashHolding();
    final CashHolding cashHolding2 = new CashHolding();

    final var sut = new NotNullCashCurrencyValidation(List.of(cashHolding1, cashHolding2));

    // ACT
    // VERIFY
    assertDoesNotThrow(sut::check);
  }

}
