package com.fintex.ce.adapter.graphqlclient.endpoint.yield;

import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldStockEndpoint;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.Stock;
import com.fintex.smclient.graphql.StockQuery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class YieldStockEndpointTest {

  @Test
  public void getGetStocksByTickersAndExchangeIds_isPresent() {
    // SETUP
    final YieldStockEndpoint sut = new YieldStockEndpoint();

    final Query q = mock(Query.class);
    final ArrayList<Stock> expected = new ArrayList<>();

    when(q.getGetStocksByTickersAndExchangeIds()).thenReturn(expected);

    // ACT
    final Function<Query, List<Stock>> actual = sut.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(q), expected);
  }

  @Test
  public void requestMapper_verify() {
    // SETUP
    final YieldStockEndpoint sut = Mockito.mock(YieldStockEndpoint.class);

    final StockQuery stockQuery = mock(StockQuery.class);
    when(stockQuery.dividendYield(any())).thenReturn(stockQuery);
    when(stockQuery.externalIdentifiers(any())).thenReturn(stockQuery);

    doCallRealMethod().when(sut).requestMapper(any());

    // ACT
    final StockQuery actual = sut.requestMapper(stockQuery);

    // VERIFY
    verify(actual).dividendYield(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION);
    verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Test
  public void responseMapper_verify() {
    // SETUP
    final YieldStockEndpoint sut = Mockito.mock(YieldStockEndpoint.class);

    final StockHolding holding = mock(StockHolding.class);
    final FloatDatapoint dividendYield = mock(FloatDatapoint.class);
    final BigDecimal yieldValue = mock(BigDecimal.class);

    final Stock entity = mock(Stock.class);
    when(entity.getDividendYield()).thenReturn(dividendYield);
    when(dividendYield.getValue()).thenReturn(yieldValue);

    doCallRealMethod().when(sut).responseMapper(any(), any());

    // ACT
    final Yield result = sut.responseMapper(entity, holding);

    // VERIFY
    Assertions.assertNotNull(result);
    Assertions.assertEquals(yieldValue, result.getDividendYield());
  }

}
