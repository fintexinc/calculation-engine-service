package com.fintex.ce.adapter.graphqlclient.endpoint.yield;

import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldPooledFundEndpoint;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
import com.fintex.smclient.graphql.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class YieldPooledFundEndpointTest {

  @Test
  void getGetBy_isPresent() {
    // SETUP
    final YieldPooledFundEndpoint m = new YieldPooledFundEndpoint();

    final Query q = mock(Query.class);
    final ArrayList<PooledFund> expected = new ArrayList<>();

    when(q.getGetCanadaPooledFundsByMorningstarIds()).thenReturn(expected);

    // ACT
    final Function<Query, List<PooledFund>> actual = m.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(q), expected);
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final YieldPooledFundEndpoint m = mock(YieldPooledFundEndpoint.class);

    final PooledFundQuery pooledFundQuery = mock(PooledFundQuery.class);
    when(pooledFundQuery.dividendYield(any())).thenReturn(pooledFundQuery);
    when(pooledFundQuery.externalIdentifiers(any())).thenReturn(pooledFundQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final PooledFundQuery actual = m.requestMapper(pooledFundQuery);

    // VERIFY
    verify(actual).dividendYield(any());
    verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Test
  void responseMapper_verifyEquityMarketCapitalizationMapper() {
    // SETUP
    final var sut = mock(YieldPooledFundEndpoint.class);

    final var pooledFund = mock(PooledFund.class);
    final BigDecimal dividendYield = mock(BigDecimal.class);
    final var dividendYieldDatapoint = mock(FloatDatapoint.class);
    when(pooledFund.getDividendYield()).thenReturn(dividendYieldDatapoint);
    when(dividendYieldDatapoint.getValue()).thenReturn(dividendYield);
    final CanadaPooledFundHolding h = mock(CanadaPooledFundHolding.class);

    doCallRealMethod().when(sut).responseMapper(any(), any());

    // ACT
    final Yield response = sut.responseMapper(pooledFund, h);

    // VERIFY
    assertNotNull(response);
    assertEquals(dividendYield, response.getDividendYield());
  }

}
