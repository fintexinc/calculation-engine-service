package com.fintex.ce.adapter.graphqlclient.endpoint.equitysector;

import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorStockEndpoint;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.EquitySectorStock;
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

class EquitySectorStockEndpointTest {

  @Test
  void getGetStocksByTickersAndExchangeIds_isPresent() {
    // SETUP
    final EquitySectorStockEndpoint m = new EquitySectorStockEndpoint();

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
    final EquitySectorStockEndpoint m = mock(EquitySectorStockEndpoint.class);

    final StockQuery etfQuery = mock(StockQuery.class);
    when(etfQuery.sectorName(any())).thenReturn(etfQuery);
    when(etfQuery.externalIdentifiers(any())).thenReturn(etfQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final StockQuery actual = m.requestMapper(etfQuery);

    // VERIFY
    verify(actual).sectorName(STRING_WITH_DATA_PROVIDER_DEFINITION);
    verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Test
  void responseMapper_checkResult2() {
    // SETUP
    final EquitySectorStockEndpoint m = mock(EquitySectorStockEndpoint.class);

    final StockHolding holding = mock(StockHolding.class);
    final HoldingType cash = HoldingType.CASH;
    when(holding.getType()).thenReturn(cash);

    final Stock stock = mock(Stock.class);
    when(stock.getSectorName()).thenReturn(null);

    doCallRealMethod().when(m).responseMapper(any(), any());
    // ACT
    final EquitySectorStock rEquitySectorStock = m.responseMapper(stock, holding);

    // VERIFY
    assertNull(rEquitySectorStock.getSectorName());
  }

  @Test
  void responseMapper_checkResult3() {
    // SETUP
    final EquitySectorStockEndpoint m = mock(EquitySectorStockEndpoint.class);

    final StockHolding holding = mock(StockHolding.class);
    final HoldingType cash = HoldingType.CANADA_STOCKS;
    when(holding.getType()).thenReturn(cash);
    when(holding.generateUserIdentifier()).thenReturn("ID");

    final Stock stock = mock(Stock.class);
    final StringDatapoint sData = mock(StringDatapoint.class);
    when(stock.getSectorName()).thenReturn(sData);
    when(sData.getValue()).thenReturn("F");
    when(sData.getDataProvider()).thenReturn(MORNINGSTAR);

    doCallRealMethod().when(m).responseMapper(any(), any());
    // ACT
    final EquitySectorStock actual = m.responseMapper(stock, holding);

    // verify
    final EquitySectorStock expected = new EquitySectorStock("F");
    expected.setProvider(MORNINGSTAR.name());

    assertEquals(expected, actual);
  }

}