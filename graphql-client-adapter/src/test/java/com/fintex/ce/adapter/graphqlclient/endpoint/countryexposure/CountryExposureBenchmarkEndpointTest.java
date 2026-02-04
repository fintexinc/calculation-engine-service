package com.fintex.ce.adapter.graphqlclient.endpoint.countryexposure;

import com.fintex.ce.adapter.graphqlclient.endpoint.countryexposure.CountryExposureBenchmarkEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.domain.model.holding.BenchmarkIndexHolding;
import com.fintex.smclient.graphql.CountryAllocation;
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

class CountryExposureBenchmarkEndpointTest {

  @Test
  void getUsEtfsByTickers_isPresent() {
    // SETUP
    final CountryExposureBenchmarkEndpoint m = new CountryExposureBenchmarkEndpoint();

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
    final CountryExposureBenchmarkEndpoint m = mock(CountryExposureBenchmarkEndpoint.class);

    final IndexQuery query = mock(IndexQuery.class);
    when(query.externalIdentifiers(any())).thenReturn(query);
    when(query.fixedIncomeCountryAllocation(any())).thenReturn(query);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final IndexQuery actual = m.requestMapper(query);

    // VERIFY
    verify(actual).externalIdentifiers(any());
    verify(actual).fixedIncomeCountryAllocation(any());
  }

  @Test
  void responseMapper_verifyCountryExposureMapper() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final CountryExposureBenchmarkEndpoint sut = mock(CountryExposureBenchmarkEndpoint.class);

      final BenchmarkIndexHolding holding = mock(BenchmarkIndexHolding.class);

      final Index entity = mock(Index.class);
      final CountryAllocation country = mock(CountryAllocation.class);
      when(entity.getFixedIncomeCountryAllocation()).thenReturn(country);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      sut.responseMapper(entity, holding);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.countryExposureMapper(country));
    }
  }

  @Test
  void responseMapper_checkResult() throws Exception {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final CountryExposureBenchmarkEndpoint sut = mock(CountryExposureBenchmarkEndpoint.class);

      final Index entity = mock(Index.class);
      final CountryAllocation country = mock(CountryAllocation.class);
      when(entity.getFixedIncomeCountryAllocation()).thenReturn(country);
      final BenchmarkIndexHolding holding = mock(BenchmarkIndexHolding.class);
      when(holding.getType()).thenReturn(CASH);

      final Map<String, BigDecimal> asset = Map.of("T", BigDecimal.ONE);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.countryExposureMapper(any())).thenReturn(asset);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.toDomainHoldingType(any())).thenReturn(CASH);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      final CountryExposure actual = sut.responseMapper(entity, holding);

      // VERIFY
      assertEquals(new CountryExposure(holding.getType(), asset), actual);
    }
  }

}
