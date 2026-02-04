package com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization;

import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.smclient.graphql.EquityMarketCapitalization;
import com.fintex.smclient.graphql.EquityMarketCapitalizationQuery;
import com.fintex.smclient.graphql.EquityMarketCapitalizationQueryDefinition;
import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EquityMarketCapitalizationBenchmarkEndpointTest {

  @Test
  void requestMapper_verify() {
    // SETUP
    final EquityMarketCapitalizationBenchmarkEndpoint m = mock(EquityMarketCapitalizationBenchmarkEndpoint.class);

    final IndexQuery indexQuery = mock(IndexQuery.class);
    when(indexQuery.equityMarketCapitalization(any())).thenReturn(indexQuery);
    when(indexQuery.externalIdentifiers(any())).thenReturn(indexQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final IndexQuery actual = m.requestMapper(indexQuery);

    // VERIFY
    verify(actual).equityMarketCapitalization(any());
    verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Test
  void equitySectorAllocationQueryDefinition_checkResult() {
    // SETUP
    final EquityMarketCapitalizationQueryDefinition actual = EquityMarketCapitalizationBenchmarkEndpoint
        .getEquityMarketCapitalizationQueryDefinition();

    final EquityMarketCapitalizationQuery equityM = mock(EquityMarketCapitalizationQuery.class);

    when(equityM.dataProvider()).thenReturn(equityM);
    when(equityM.values(any())).thenReturn(equityM);

    // ACT
    actual.define(equityM);

    // VERIFY
    verify(equityM).dataProvider();
    verify(equityM).values(any());
  }

  @Test
  void responseMapper_verifyEquityMarketCapitalizationMapper() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var sut = mock(EquityMarketCapitalizationBenchmarkEndpoint.class);

      final var index = mock(Index.class);
      final var equityMarketCapitalization = mock(EquityMarketCapitalization.class);
      when(index.getEquityMarketCapitalization()).thenReturn(equityMarketCapitalization);
      final BenchmarkIndexHolding h = mock(BenchmarkIndexHolding.class);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      sut.responseMapper(index, h);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.equityMarketCapitalizationMapper(
          equityMarketCapitalization));
    }
  }

  @Test
  void responseMapper_checkResult() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final EquityMarketCapitalizationBenchmarkEndpoint sut = mock(EquityMarketCapitalizationBenchmarkEndpoint.class);

      final Index index = mock(Index.class);
      final EquityMarketCapitalization allocation = mock(EquityMarketCapitalization.class);
      when(index.getEquityMarketCapitalization()).thenReturn(allocation);
      final BenchmarkIndexHolding h = mock(BenchmarkIndexHolding.class);

      final com.fintex.ce.domain.model.EquityMarketCapitalization actual = mock(
          com.fintex.ce.domain.model.EquityMarketCapitalization.class);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.equityMarketCapitalizationMapper(any())).thenReturn(
          actual);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      final com.fintex.ce.domain.model.EquityMarketCapitalization expected = sut.responseMapper(index, h);

      // VERIFY
      assertSame(expected, actual);
    }
  }

}
