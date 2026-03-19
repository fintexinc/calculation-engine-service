package com.fintex.ce.adapter.rest.dto.request.core;

import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.util.List;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.domain.model.enumeration.Currency.CAD;
import static com.fintex.ce.domain.model.enumeration.Currency.USD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PortfolioReqDTOTest {

  @Test
  void setReqCurrencyToCashHolding_checkResult() {
    // SETUP
    final PortfolioReqDTO sut = new PortfolioReqDTO();
    sut.setCurrency(USD);
    final CashHolding cashHolding = new CashHolding();
    cashHolding.setHoldingType(FinancialInstrumentType.CASH);
    cashHolding.setCurrency(CAD);
    sut.setHoldings(List.of(cashHolding));

    // ACT
    sut.setReqCurrencyToCashHolding();

    // VERIFY
    assertEquals(CAD, cashHolding.getCurrency());
  }

  @Test
  void setReqCurrencyToCashHolding_checkResultWithCAD() {
    // SETUP
    final PortfolioReqDTO sut = new PortfolioReqDTO();
    sut.setCurrency(CAD);
    final CashHolding cashHolding = new CashHolding();
    cashHolding.setHoldingType(FinancialInstrumentType.CASH);
    sut.setHoldings(List.of(cashHolding));

    // ACT
    sut.setReqCurrencyToCashHolding();

    // VERIFY
    assertEquals(CAD, cashHolding.getCurrency());
  }

  @Test
  void setReqCurrencyToCashHolding_checkResultWithUSD() {
    // SETUP
    final PortfolioReqDTO sut = new PortfolioReqDTO();
    sut.setCurrency(USD);
    final CashHolding cashHolding = new CashHolding();
    cashHolding.setHoldingType(FinancialInstrumentType.CASH);
    sut.setHoldings(List.of(cashHolding));

    // ACT
    sut.setReqCurrencyToCashHolding();

    // VERIFY
    assertEquals(USD, cashHolding.getCurrency());
  }

  @Test
  void setReqCurrencyToCashHolding_checkResultWhenTwoCashHoldingsPresent() {
    // SETUP
    final PortfolioReqDTO sut = new PortfolioReqDTO();
    sut.setCurrency(USD);
    final CashHolding cashHolding1 = new CashHolding();
    cashHolding1.setHoldingType(FinancialInstrumentType.CASH);
    final CashHolding cashHolding2 = new CashHolding();
    cashHolding2.setHoldingType(FinancialInstrumentType.CASH);
    sut.setHoldings(List.of(cashHolding1, cashHolding2));

    // ACT
    sut.setReqCurrencyToCashHolding();

    // VERIFY
    assertNull(cashHolding1.getCurrency());
    assertNull(cashHolding2.getCurrency());
  }

  @Test
  void setReqCurrencyToCashHolding_checkResultWhenTwoCashHoldingsPresentWithCurrencies() {
    // SETUP
    final PortfolioReqDTO sut = new PortfolioReqDTO();
    sut.setCurrency(USD);
    final CashHolding cashHolding1 = new CashHolding().setCurrency(CAD);
    cashHolding1.setHoldingType(FinancialInstrumentType.CASH);
    final CashHolding cashHolding2 = new CashHolding().setCurrency(USD);
    cashHolding2.setHoldingType(FinancialInstrumentType.CASH);
    sut.setHoldings(List.of(cashHolding1, cashHolding2));

    // ACT
    sut.setReqCurrencyToCashHolding();

    // VERIFY
    assertEquals(CAD, cashHolding1.getCurrency());
    assertEquals(USD, cashHolding2.getCurrency());
  }

}
