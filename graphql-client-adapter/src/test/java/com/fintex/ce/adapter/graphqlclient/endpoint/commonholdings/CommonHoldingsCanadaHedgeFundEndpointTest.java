package com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings;

import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.CommonHoldings;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
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

class CommonHoldingsCanadaHedgeFundEndpointTest {

  @Test
  void getUsEtfsByTickers_isPresent() {
    // SETUP
    final var fundCanadaFDSEndpoint = new CommonHoldingsCanadaHedgeFundEndpoint();
    final var query = mock(Query.class);
    final var expected = new ArrayList<HedgeFund>();

    when(query.getGetCanadaHedgeFundsByMorningstarIds()).thenReturn(expected);

    // ACT
    final Function<Query, List<HedgeFund>> actual = fundCanadaFDSEndpoint.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(query), expected);
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final var fundCanadaFDSEndpoint = mock(CommonHoldingsCanadaHedgeFundEndpoint.class);
    final var hedgeFundQuery = mock(HedgeFundQuery.class);

    when(hedgeFundQuery.holdings(any())).thenReturn(hedgeFundQuery);
    when(hedgeFundQuery.externalIdentifiers(any())).thenReturn(hedgeFundQuery);

    doCallRealMethod().when(fundCanadaFDSEndpoint).requestMapper(any());
    // ACT
    final HedgeFundQuery actual = fundCanadaFDSEndpoint.requestMapper(hedgeFundQuery);

    // VERIFY
    verify(actual).holdings(any());
    verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Test
  void responseMapper_verifyTopCommonHoldingsMapper() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var fundCanadaFDSEndpoint = mock(CommonHoldingsCanadaHedgeFundEndpoint.class);
      final var canadaHedgeFundHolding = mock(CanadaHedgeFundHolding.class);
      final var hedgeFund = mock(HedgeFund.class);
      final var allocation = mock(Holdings.class);

      when(hedgeFund.getHoldings()).thenReturn(allocation);

      doCallRealMethod().when(fundCanadaFDSEndpoint).responseMapper(any(), any());
      // ACT
      fundCanadaFDSEndpoint.responseMapper(hedgeFund, canadaHedgeFundHolding);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.topCommonHoldingsMapper(allocation));
    }
  }

  @Test
  void responseMapper_checkResult() throws Exception {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP

      final var fundCanadaFDSEndpoint = mock(CommonHoldingsCanadaHedgeFundEndpoint.class);
      final var hedgeFund = mock(HedgeFund.class);
      final var holding = mock(CanadaHedgeFundHolding.class);
      final var allocation = mock(Holdings.class);
      final CommonHoldings actual = mock(CommonHoldings.class);

      when(hedgeFund.getHoldings()).thenReturn(allocation);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.topCommonHoldingsMapper(any())).thenReturn(actual);

      doCallRealMethod().when(fundCanadaFDSEndpoint).responseMapper(any(), any());
      // ACT
      final CommonHoldings expected = fundCanadaFDSEndpoint.responseMapper(hedgeFund, holding);

      // VERIFY
      assertSame(expected, actual);
    }
  }

}
