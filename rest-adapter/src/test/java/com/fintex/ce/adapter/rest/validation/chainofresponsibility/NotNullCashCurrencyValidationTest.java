package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import java.util.List;
import org.junit.jupiter.api.Test;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_RRC_MC_002;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotNullCashCurrencyValidationTest {

  @Test
  void check_ERR_RRC_MC_002ThrownWhenInterestRateIsNull() {
    final var sut = new NotNullCashCurrencyValidation(List.of(CashHolding.builder().build()));

    final ReqValidationException expected = ERR_RRC_MC_002.reqValidationError();

    final ReqValidationException actual = assertThrows(ReqValidationException.class, sut::check);

    assertEquals(expected, actual);
  }

  @Test
  void checkValidCashHolding() {
    final CashHolding cashHolding = CashHolding.builder()
        .currency(CurrencyType.USD)
        .build();

    final var sut = new NotNullCashCurrencyValidation(List.of());

    assertDoesNotThrow(sut::check);
  }

  @Test
  void check_exceptionNotThrownBecausePortfolioContainsSeveralCashHoldings() {
    final CashHolding cashHolding1 = CashHolding.builder().build();
    final CashHolding cashHolding2 = CashHolding.builder().build();

    final var sut = new NotNullCashCurrencyValidation(List.of(cashHolding1, cashHolding2));

    assertDoesNotThrow(sut::check);
  }

}
