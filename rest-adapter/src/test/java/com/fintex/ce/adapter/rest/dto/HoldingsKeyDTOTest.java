package com.fintex.ce.adapter.rest.dto;

import com.fintex.ce.adapter.rest.dto.response.correlation.HoldingsKeyDTO;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.EquitySecurityIdentifier;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.util.List;
import org.junit.jupiter.api.Test;
import static com.fintex.ce.util.PortfolioUtils.createKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HoldingsKeyDTOTest {

  @Test
  void holdingsKeyDTO_buildHoldingsKeyDTO_checkResult() {
    final String testTicker = "TEST_TICKER";
    final String testFundServCode = "TEST_FUND_SER_CODE";
    final String exchangeCode = "ExchangeCode";
    final Holding usEtfHolding = new Holding(null, FinancialInstrumentType.ETF_US,
        new SecurityIdentifier(testTicker, FiIdentifierType.TICKER));
    final Holding mutualFundsHolding = new Holding(null, FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier(testFundServCode, FiIdentifierType.FUNDSERV));
    final Holding cash = CashHolding.builder()
        .currency(CurrencyType.CAD)
        .holdingType(FinancialInstrumentType.CASH)
        .build();
    final Holding canadaEtfHolding = new Holding(null, FinancialInstrumentType.ETF_CANADA,
        new SecurityIdentifier(testTicker, FiIdentifierType.TICKER));

    final EquitySecurityIdentifier usStockSecId = mock(EquitySecurityIdentifier.class);
    when(usStockSecId.getId()).thenReturn(testTicker);
    when(usStockSecId.getExchangeId()).thenReturn(exchangeCode);
    final Holding usStockHolding = new Holding(null, FinancialInstrumentType.STOCK_US, usStockSecId);

    final EquitySecurityIdentifier canadaStockSecId = mock(EquitySecurityIdentifier.class);
    when(canadaStockSecId.getId()).thenReturn(testTicker);
    when(canadaStockSecId.getExchangeId()).thenReturn(exchangeCode);
    final Holding canadaStockHolding = new Holding(null, FinancialInstrumentType.STOCK_CANADA, canadaStockSecId);

    final List<Holding> holdings = List.of(usEtfHolding, mutualFundsHolding, cash, canadaEtfHolding, usStockHolding,
        canadaStockHolding);

    final List<HoldingsKeyDTO> results = holdings.stream().map(HoldingsKeyDTO::buildHoldingsKeyDTO).toList();

    assertEquals(6, results.size());

    assertEquals(createKey(usEtfHolding), results.get(0).getKey());
    assertEquals(testTicker, results.get(0).getSecurityIdentifier().getId());

    assertEquals(createKey(mutualFundsHolding), results.get(1).getKey());
    assertEquals(testFundServCode, results.get(1).getSecurityIdentifier().getId());

    assertEquals(createKey(cash), results.get(2).getKey());
    assertNull(results.get(2).getSecurityIdentifier());

    assertEquals(createKey(canadaEtfHolding), results.get(3).getKey());
    assertEquals(testTicker, results.get(3).getSecurityIdentifier().getId());

    assertEquals(createKey(usStockHolding), results.get(4).getKey());
    assertInstanceOf(EquitySecurityIdentifier.class, results.get(4).getSecurityIdentifier());
    assertEquals(testTicker, results.get(4).getSecurityIdentifier().getId());
    assertEquals(exchangeCode, ((EquitySecurityIdentifier) results.get(4).getSecurityIdentifier()).getExchangeId());

    assertEquals(createKey(canadaStockHolding), results.get(5).getKey());
    assertInstanceOf(EquitySecurityIdentifier.class, results.get(5).getSecurityIdentifier());
    assertEquals(testTicker, results.get(5).getSecurityIdentifier().getId());
    assertEquals(exchangeCode, ((EquitySecurityIdentifier) results.get(5).getSecurityIdentifier()).getExchangeId());
  }

}
