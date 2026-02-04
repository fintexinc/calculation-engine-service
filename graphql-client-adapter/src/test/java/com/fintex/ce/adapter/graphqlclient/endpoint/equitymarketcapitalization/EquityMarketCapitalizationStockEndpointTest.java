package com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization;

import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationStockEndpoint;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.EquityMarketCapitalizationStock;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.Stock;
import com.fintex.smclient.graphql.StockQuery;
import com.fintex.smclient.graphql.StringDatapoint;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static com.fintex.smclient.graphql.DataProvider.MORNINGSTAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EquityMarketCapitalizationStockEndpointTest {

  @Test
  void getGetStocksByTickersAndExchangeIds_isPresent() {
    // SETUP
    final EquityMarketCapitalizationStockEndpoint m = new EquityMarketCapitalizationStockEndpoint();

    final Query q = mock(Query.class);
    final ArrayList<Stock> expected = new ArrayList<>();

    when(q.getGetStocksByTickersAndExchangeIds()).thenReturn(expected);

    // ACT
    final Function<Query, List<Stock>> actual = m.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(q), expected);
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final EquityMarketCapitalizationStockEndpoint m = mock(EquityMarketCapitalizationStockEndpoint.class);

    final StockQuery etfQuery = mock(StockQuery.class);
    when(etfQuery.stylebox(any())).thenReturn(etfQuery);
    when(etfQuery.externalIdentifiers(any())).thenReturn(etfQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final StockQuery actual = m.requestMapper(etfQuery);

    // VERIFY
    verify(actual).stylebox(STRING_WITH_DATA_PROVIDER_DEFINITION);
    verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Test
  void responseMapper_checkResult2() {
    // SETUP
    final EquityMarketCapitalizationStockEndpoint m = mock(EquityMarketCapitalizationStockEndpoint.class);

    final StockHolding holding = mock(StockHolding.class);
    final HoldingType cash = HoldingType.CASH;
    when(holding.getType()).thenReturn(cash);

    final Stock stock = mock(Stock.class);
    when(stock.getSectorName()).thenReturn(null);

    doCallRealMethod().when(m).responseMapper(any(), any());
    // ACT
    final EquityMarketCapitalizationStock rEquityMarketCapitalizationStock = m.responseMapper(stock, holding);

    // VERIFY
    assertNull(rEquityMarketCapitalizationStock.getStyleBox());
  }

  @Test
  void responseMapper_checkResult3() {
    // SETUP
    final EquityMarketCapitalizationStockEndpoint m = mock(EquityMarketCapitalizationStockEndpoint.class);

    final StockHolding holding = mock(StockHolding.class);
    final HoldingType cash = HoldingType.CANADA_STOCKS;
    when(holding.getType()).thenReturn(cash);
    when(holding.generateUserIdentifier()).thenReturn("ID");

    final Stock stock = mock(Stock.class);
    final StringDatapoint sData = mock(StringDatapoint.class);
    when(stock.getStylebox()).thenReturn(sData);
    when(sData.getValue()).thenReturn("F");
    when(sData.getDataProvider()).thenReturn(MORNINGSTAR);

    doCallRealMethod().when(m).responseMapper(any(), any());
    // ACT
    final EquityMarketCapitalizationStock actual = m.responseMapper(stock, holding);

    // verify
    final EquityMarketCapitalizationStock expected = new EquityMarketCapitalizationStock("F");
    expected.setProvider(MORNINGSTAR.name());

    assertEquals(expected, actual);
  }

}