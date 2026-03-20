package com.fintex.ce.adapter.rest.dto;

import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.adapter.rest.dto.response.correlation.HoldingsKeyDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.fintex.ce.util.PortfolioUtils.createKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HoldingsKeyDTOTest {

  @Test
  void holdingsKeyDTO_buildHoldingsKeyDTO_checkResult() {
    // SETUP
    final String testTicker = "TEST_TICKER";
    final String testFundServCode = "TEST_FUND_SER_CODE";
    final String exchangeCode = "ExchangeCode";
    final Holding usEtfHolding = new EtfHolding().setTicker(testTicker).setType(HoldingType.US_ETF);
    final Holding mutualFundsHolding = new FundSeriesHolding().setFundServCode(testFundServCode).setType(
        HoldingType.CANADA_MUTUAL_FUNDS);
    final Holding cash = new CashHolding().setCurrency(Currency.CAD).setType(HoldingType.CASH);
    final Holding canadaEtfHolding = new EtfHolding().setTicker(testTicker).setType(HoldingType.CANADA_ETF);
    final Holding usStockHolding = new StockHolding().setExchangeCode(exchangeCode).setTicker(testTicker).setType(
        HoldingType.US_STOCKS);
    final Holding canadaStockHolding = new StockHolding().setExchangeCode(exchangeCode).setTicker(testTicker).setType(
        HoldingType.CANADA_STOCKS);
    final List<Holding> holdings = List.of(usEtfHolding, mutualFundsHolding, cash, canadaEtfHolding, usStockHolding,
        canadaStockHolding);

    // ACT
    final List<HoldingsKeyDTO> results = holdings.stream().map(HoldingsKeyDTO::buildHoldingsKeyDTO).collect(Collectors
        .toList());

    // VERIFY
    assertEquals(6, results.size());
    assertEquals(createKey(usEtfHolding), results.get(0).getKey());
    assertEquals(testTicker, results.get(0).getTicker());
    assertNull(results.get(0).getExchangeCode());

    assertEquals(createKey(mutualFundsHolding), results.get(1).getKey());
    assertEquals(testFundServCode, results.get(1).getFundServCode());

    assertEquals(createKey(cash), results.get(2).getKey());

    assertEquals(createKey(canadaEtfHolding), results.get(3).getKey());
    assertEquals(testTicker, results.get(3).getTicker());
    assertNull(results.get(3).getExchangeCode());

    assertEquals(createKey(usStockHolding), results.get(4).getKey());
    assertEquals(testTicker, results.get(4).getTicker());
    assertEquals(exchangeCode, results.get(4).getExchangeCode());

    assertEquals(createKey(canadaStockHolding), results.get(5).getKey());
    assertEquals(testTicker, results.get(5).getTicker());
    assertEquals(exchangeCode, results.get(5).getExchangeCode());
  }

}
