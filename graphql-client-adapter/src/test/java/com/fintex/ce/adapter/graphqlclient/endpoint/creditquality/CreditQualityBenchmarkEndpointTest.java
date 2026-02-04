package com.fintex.ce.adapter.graphqlclient.endpoint.creditquality;

import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.smclient.graphql.CreditQualityRatings;
import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.IndexQuery;
import com.fintex.smclient.graphql.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.domain.enumeration.HoldingType.CASH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreditQualityBenchmarkEndpointTest {

  @Test
  void getUsEtfsByTickers_isPresent() {
    // SETUP
    final CreditQualityBenchmarkEndpoint m = new CreditQualityBenchmarkEndpoint();

    final Query q = mock(Query.class);
    final ArrayList<Index> expected = new ArrayList<>();

    when(q.getGetIndexesByMorningstarIds()).thenReturn(expected);

    // ACT
    final Function<Query, List<Index>> actual = m.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(q), expected);
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final CreditQualityBenchmarkEndpoint m = mock(CreditQualityBenchmarkEndpoint.class);

    final IndexQuery indexQuery = mock(IndexQuery.class);
    when(indexQuery.creditQualityRatings(any())).thenReturn(indexQuery);
    when(indexQuery.externalIdentifiers(any())).thenReturn(indexQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final IndexQuery actual = m.requestMapper(indexQuery);

    // VERIFY
    verify(actual).creditQualityRatings(any());
    verify(actual).externalIdentifiers(any());
  }

  @Test
  void responseMapper_verifyEquityCountryAllocationMapper() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final CreditQualityBenchmarkEndpoint sut = mock(CreditQualityBenchmarkEndpoint.class);

      final BenchmarkIndexHolding holding = mock(BenchmarkIndexHolding.class);

      final Index entity = mock(Index.class);
      final CreditQualityRatings country = mock(CreditQualityRatings.class);
      when(entity.getCreditQualityRatings()).thenReturn(country);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      sut.responseMapper(entity, holding);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.creditQualityMapper(country));
    }
  }

  @Test
  void responseMapper_checkResult() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final CreditQualityBenchmarkEndpoint sut = mock(CreditQualityBenchmarkEndpoint.class);

      final Index entity = mock(Index.class);
      final CreditQualityRatings creditQualityRatings = mock(CreditQualityRatings.class);
      when(entity.getCreditQualityRatings()).thenReturn(creditQualityRatings);
      final BenchmarkIndexHolding holding = mock(BenchmarkIndexHolding.class);
      when(holding.getType()).thenReturn(CASH);

      final Map<String, BigDecimal> asset = Map.of("T", BigDecimal.ONE);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.creditQualityMapper(creditQualityRatings)).thenReturn(
          asset);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.toDomainHoldingType(any())).thenReturn(CASH);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      final CreditQuality actual = sut.responseMapper(entity, holding);

      // VERIFY
      assertEquals(new CreditQuality(holding.getType(), asset), actual);
    }
  }

}
