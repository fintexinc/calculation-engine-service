package com.fintex.ce.adapter.graphqlclient.endpoint.yield;

import com.fintex.ce.adapter.graphqlclient.endpoint.yield.YieldFundCanadaEndpoint;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
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

class YieldFundCanadaEndpointTest {

  @Test
  void getGetCanadaEtfsByTickers_isPresent() {
    // SETUP
    final YieldFundCanadaEndpoint m = new YieldFundCanadaEndpoint();

    final Query q = mock(Query.class);
    final ArrayList<FundSeries> expected = new ArrayList<>();

    when(q.getGetFundSeriesByHoldingCodes()).thenReturn(expected);

    // ACT
    final Function<Query, List<FundSeries>> actual = m.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(q), expected);
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final YieldFundCanadaEndpoint m = mock(YieldFundCanadaEndpoint.class);

    final FundSeriesQuery fundSeriesQuery = mock(FundSeriesQuery.class);
    when(fundSeriesQuery.dividendYield(any())).thenReturn(fundSeriesQuery);
    when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final FundSeriesQuery actual = m.requestMapper(fundSeriesQuery);

    // VERIFY
    verify(actual).dividendYield(any());
    verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Test
  void responseMapper_verifyEquityMarketCapitalizationMapper() {
    // SETUP
    final var sut = mock(YieldFundCanadaEndpoint.class);

    final var etf = mock(FundSeries.class);
    final BigDecimal dividendYield = mock(BigDecimal.class);
    final var dividendYieldDatapoint = mock(FloatDatapoint.class);
    when(etf.getDividendYield()).thenReturn(dividendYieldDatapoint);
    when(dividendYieldDatapoint.getValue()).thenReturn(dividendYield);
    final FundSeriesHolding h = mock(FundSeriesHolding.class);

    doCallRealMethod().when(sut).responseMapper(any(), any());

    // ACT
    final Yield response = sut.responseMapper(etf, h);

    // VERIFY
    assertNotNull(response);
    assertEquals(dividendYield, response.getDividendYield());
  }

}
