package com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings;

import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.CommonHoldings;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.Holdings;
import com.fintex.smclient.graphql.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommonHoldingsFundCanadaEndpointTest {

  @Test
  void getUsEtfsByTickers_isPresent() {
    // SETUP
    final var fundCanadaFDSEndpoint = new CommonHoldingsFundCanadaEndpoint();
    final var query = mock(Query.class);
    final var expected = new ArrayList<FundSeries>();

    when(query.getGetFundSeriesByHoldingCodes()).thenReturn(expected);

    // ACT
    final Function<Query, List<FundSeries>> actual = fundCanadaFDSEndpoint.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(query), expected);
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final var fundCanadaFDSEndpoint = mock(CommonHoldingsFundCanadaEndpoint.class);
    final var fundSeriesQuery = mock(FundSeriesQuery.class);

    when(fundSeriesQuery.holdings(any())).thenReturn(fundSeriesQuery);
    when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);

    doCallRealMethod().when(fundCanadaFDSEndpoint).requestMapper(any());
    // ACT
    final FundSeriesQuery actual = fundCanadaFDSEndpoint.requestMapper(fundSeriesQuery);

    // VERIFY
    verify(actual).holdings(any());
    verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Test
  void responseMapper_verifyTopCommonHoldingsMapper() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var fundCanadaFDSEndpoint = mock(CommonHoldingsFundCanadaEndpoint.class);
      final var fundSeriesHolding = mock(FundSeriesHolding.class);
      final var etf = mock(FundSeries.class);
      final var allocation = mock(Holdings.class);

      when(etf.getHoldings()).thenReturn(allocation);

      doCallRealMethod().when(fundCanadaFDSEndpoint).responseMapper(any(), any());
      // ACT
      fundCanadaFDSEndpoint.responseMapper(etf, fundSeriesHolding);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.topCommonHoldingsMapper(allocation));
    }
  }

  @Test
  void responseMapper_checkResult() throws Exception {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP

      final var fundCanadaFDSEndpoint = mock(CommonHoldingsFundCanadaEndpoint.class);
      final var fundSeries = mock(FundSeries.class);
      final var holding = mock(FundSeriesHolding.class);
      final var allocation = mock(Holdings.class);
      final CommonHoldings actual = mock(CommonHoldings.class);

      when(fundSeries.getHoldings()).thenReturn(allocation);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.topCommonHoldingsMapper(any())).thenReturn(actual);

      doCallRealMethod().when(fundCanadaFDSEndpoint).responseMapper(any(), any());
      // ACT
      final CommonHoldings expected = fundCanadaFDSEndpoint.responseMapper(fundSeries, holding);

      // VERIFY
      assertSame(expected, actual);
    }
  }
}