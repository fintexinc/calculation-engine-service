package com.fintex.ce.adapter.rest.dto.request.core;

import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.fintex.sm.model.domain.enumeration.CurrencyType.CAD;
import static com.fintex.sm.model.domain.enumeration.CurrencyType.USD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PortfolioReqDTOTest {

  @Test
  void setReqCurrencyToCashHolding_checkResult() {
    final PortfolioReqDTO sut = new PortfolioReqDTO();
    sut.setCurrency(USD);
    final CashHolding cashHolding = new CashHolding();
    cashHolding.setHoldingType(FinancialInstrumentType.CASH);
    cashHolding.setCurrency(CAD);
    sut.setHoldings(List.of(cashHolding));

    sut.setReqCurrencyToCashHolding();

    assertEquals(CAD, cashHolding.getCurrency());
  }

  @Test
  void setReqCurrencyToCashHolding_checkResultWithCAD() {
    final PortfolioReqDTO sut = new PortfolioReqDTO();
    sut.setCurrency(CAD);
    final CashHolding cashHolding = new CashHolding();
    cashHolding.setHoldingType(FinancialInstrumentType.CASH);
    sut.setHoldings(List.of(cashHolding));

    sut.setReqCurrencyToCashHolding();

    assertEquals(CAD, cashHolding.getCurrency());
  }

  @Test
  void setReqCurrencyToCashHolding_checkResultWithUSD() {
    final PortfolioReqDTO sut = new PortfolioReqDTO();
    sut.setCurrency(USD);
    final CashHolding cashHolding = new CashHolding();
    cashHolding.setHoldingType(FinancialInstrumentType.CASH);
    sut.setHoldings(List.of(cashHolding));

    sut.setReqCurrencyToCashHolding();

    assertEquals(USD, cashHolding.getCurrency());
  }

  @Test
  void setReqCurrencyToCashHolding_checkResultWhenTwoCashHoldingsPresent() {
    final PortfolioReqDTO sut = new PortfolioReqDTO();
    sut.setCurrency(USD);
    final CashHolding cashHolding1 = new CashHolding();
    cashHolding1.setHoldingType(FinancialInstrumentType.CASH);
    final CashHolding cashHolding2 = new CashHolding();
    cashHolding2.setHoldingType(FinancialInstrumentType.CASH);
    sut.setHoldings(List.of(cashHolding1, cashHolding2));

    sut.setReqCurrencyToCashHolding();

    assertNull(cashHolding1.getCurrency());
    assertNull(cashHolding2.getCurrency());
  }

  @Test
  void setReqCurrencyToCashHolding_checkResultWhenTwoCashHoldingsPresentWithCurrencies() {
    final PortfolioReqDTO sut = new PortfolioReqDTO();
    sut.setCurrency(USD);
    final CashHolding cashHolding1 = new CashHolding().setCurrency(CAD);
    cashHolding1.setHoldingType(FinancialInstrumentType.CASH);
    final CashHolding cashHolding2 = new CashHolding().setCurrency(USD);
    cashHolding2.setHoldingType(FinancialInstrumentType.CASH);
    sut.setHoldings(List.of(cashHolding1, cashHolding2));

    sut.setReqCurrencyToCashHolding();

    assertEquals(CAD, cashHolding1.getCurrency());
    assertEquals(USD, cashHolding2.getCurrency());
  }

}
