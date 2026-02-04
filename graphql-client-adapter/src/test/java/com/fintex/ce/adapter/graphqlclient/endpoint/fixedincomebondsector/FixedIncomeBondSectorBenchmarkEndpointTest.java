package com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector;

import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.smclient.graphql.FixedIncomeSecuritiesAllocation;
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

class FixedIncomeBondSectorBenchmarkEndpointTest {

  @Test
  void requestMapper_verify() {
    // SETUP
    final FixedIncomeBondSectorBenchmarkEndpoint fixedIncomeBondSectorBenchmarkEndpoint = mock(
        FixedIncomeBondSectorBenchmarkEndpoint.class);

    final IndexQuery indexQuery = mock(IndexQuery.class);
    when(indexQuery.fixedIncomeSecuritiesAllocation(any())).thenReturn(indexQuery);
    when(indexQuery.externalIdentifiers(any())).thenReturn(indexQuery);

    doCallRealMethod().when(fixedIncomeBondSectorBenchmarkEndpoint).requestMapper(any());
    // ACT
    final IndexQuery actual = fixedIncomeBondSectorBenchmarkEndpoint.requestMapper(indexQuery);

    // VERIFY
    verify(actual).fixedIncomeSecuritiesAllocation(any());
    verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Test
  void responseMapper_verifyEquitySectorMapper() throws Exception {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var sut = mock(FixedIncomeBondSectorBenchmarkEndpoint.class);

      final Index index = mock(Index.class);
      final FixedIncomeSecuritiesAllocation allocation = mock(FixedIncomeSecuritiesAllocation.class);

      final FixedIncomeBondSecurities actual = mock(FixedIncomeBondSecurities.class);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(any(), any())).thenReturn(
          actual);
      when(index.getFixedIncomeSecuritiesAllocation()).thenReturn(allocation);
      final BenchmarkIndexHolding h = mock(BenchmarkIndexHolding.class);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      sut.responseMapper(index, h);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(allocation, h.getType()));
    }
  }

  @Test
  void responseMapper_checkResult() throws Exception {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final FixedIncomeBondSectorBenchmarkEndpoint sut = mock(FixedIncomeBondSectorBenchmarkEndpoint.class);

      final Index index = mock(Index.class);
      final FixedIncomeSecuritiesAllocation allocation = mock(FixedIncomeSecuritiesAllocation.class);
      when(index.getFixedIncomeSecuritiesAllocation()).thenReturn(allocation);
      final BenchmarkIndexHolding h = mock(BenchmarkIndexHolding.class);

      final FixedIncomeBondSecurities expected = mock(FixedIncomeBondSecurities.class);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(any(), any())).thenReturn(
          expected);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      final FixedIncomeBondSecurities actual = sut.responseMapper(index, h);

      // VERIFY
      assertSame(expected, actual);
    }
  }

}
