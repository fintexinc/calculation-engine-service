package com.fintex.ce.adapter.rest.dto;

import com.fintex.ce.adapter.rest.dto.response.correlation.HoldingsKeyDTO;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.EquitySecurityIdentifier;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import org.junit.jupiter.api.Test;

import java.util.List;

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
    final Holding usEtfHolding = new Holding().setSecurityIdentifier(new SecurityIdentifier(testTicker, FiIdentifierType.TICKER)).setHoldingType(FinancialInstrumentType.ETF_US);
    final Holding mutualFundsHolding = new Holding().setSecurityIdentifier(new SecurityIdentifier(testFundServCode, FiIdentifierType.FUNDSERV)).setHoldingType(
        FinancialInstrumentType.MUTUAL_FUND_CANADA);
    final Holding cash = new CashHolding().setCurrency(CurrencyType.CAD).setHoldingType(FinancialInstrumentType.CASH);
    final Holding canadaEtfHolding = new Holding().setSecurityIdentifier(new SecurityIdentifier(testTicker, FiIdentifierType.TICKER)).setHoldingType(FinancialInstrumentType.ETF_CANADA);

    final EquitySecurityIdentifier usStockSecId = mock(EquitySecurityIdentifier.class);
    when(usStockSecId.getId()).thenReturn(testTicker);
    when(usStockSecId.getExchangeId()).thenReturn(exchangeCode);
    final Holding usStockHolding = new Holding().setSecurityIdentifier(usStockSecId).setHoldingType(FinancialInstrumentType.STOCK_US);

    final EquitySecurityIdentifier canadaStockSecId = mock(EquitySecurityIdentifier.class);
    when(canadaStockSecId.getId()).thenReturn(testTicker);
    when(canadaStockSecId.getExchangeId()).thenReturn(exchangeCode);
    final Holding canadaStockHolding = new Holding().setSecurityIdentifier(canadaStockSecId).setHoldingType(FinancialInstrumentType.STOCK_CANADA);

    final List<Holding> holdings = List.of(usEtfHolding, mutualFundsHolding, cash, canadaEtfHolding, usStockHolding,
        canadaStockHolding);

    final List<HoldingsKeyDTO> results = holdings.stream().map(HoldingsKeyDTO::buildHoldingsKeyDTO).toList();

    assertEquals(6, results.size());

    // US ETF - has SecurityIdentifier with ticker
    assertEquals(createKey(usEtfHolding), results.get(0).getKey());
    assertEquals(testTicker, results.get(0).getSecurityIdentifier().getId());

    // Mutual Fund - has SecurityIdentifier with fundserv code
    assertEquals(createKey(mutualFundsHolding), results.get(1).getKey());
    assertEquals(testFundServCode, results.get(1).getSecurityIdentifier().getId());

    // Cash - no SecurityIdentifier
    assertEquals(createKey(cash), results.get(2).getKey());
    assertNull(results.get(2).getSecurityIdentifier());

    // Canada ETF - has SecurityIdentifier with ticker
    assertEquals(createKey(canadaEtfHolding), results.get(3).getKey());
    assertEquals(testTicker, results.get(3).getSecurityIdentifier().getId());

    // US Stock - has EquitySecurityIdentifier with ticker and exchangeCode
    assertEquals(createKey(usStockHolding), results.get(4).getKey());
    assertInstanceOf(EquitySecurityIdentifier.class, results.get(4).getSecurityIdentifier());
    assertEquals(testTicker, results.get(4).getSecurityIdentifier().getId());
    assertEquals(exchangeCode, ((EquitySecurityIdentifier) results.get(4).getSecurityIdentifier()).getExchangeId());

    // Canada Stock - has EquitySecurityIdentifier with ticker and exchangeCode
    assertEquals(createKey(canadaStockHolding), results.get(5).getKey());
    assertInstanceOf(EquitySecurityIdentifier.class, results.get(5).getSecurityIdentifier());
    assertEquals(testTicker, results.get(5).getSecurityIdentifier().getId());
    assertEquals(exchangeCode, ((EquitySecurityIdentifier) results.get(5).getSecurityIdentifier()).getExchangeId());
  }

}
