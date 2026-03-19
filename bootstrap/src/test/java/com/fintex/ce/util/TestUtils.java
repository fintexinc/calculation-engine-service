package com.fintex.ce.util;

import com.fintex.ce.adapter.rest.dto.request.core.PortfolioReqDTO;
import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.util.ArrayList;
import java.util.List;
import org.mockito.Mockito;

public class TestUtils {
  private TestUtils() {
  }

  public static PortfolioReqDTO holdingsWithOneCash(final Currency currency) {
    final List<Holding> holdings = cashHolding();
    final PortfolioReqDTO portfolioReqDTO = Mockito.mock(PortfolioReqDTO.class);
    Mockito.when(portfolioReqDTO.getHoldings()).thenReturn(holdings);
    Mockito.when(portfolioReqDTO.getCurrency()).thenReturn(currency);
    return portfolioReqDTO;
  }

  public static PortfolioReqDTO holdingsWithOneCash() {
    return holdingsWithOneCash(Currency.CAD);
  }

  public static List<Holding> cashHolding() {
    final Holding holding = getCashHolding();
    final List<Holding> holdings = new ArrayList<>();
    holdings.add(holding);
    return holdings;
  }

  public static Holding getCashHolding() {
    final Holding holding = Mockito.mock(Holding.class);
    Mockito.when(holding.getHoldingType()).thenReturn(FinancialInstrumentType.CASH);
    return holding;
  }

}
