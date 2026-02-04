package com.fintex.ce.adapter.graphqlclient.endpoint.creditquality;

import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.smclient.graphql.CountryAllocation;
import com.fintex.smclient.graphql.CreditQualityRatings;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
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

class CreditQualityCanadaPooledFundEndpointTest {

  @Test
  void getUsEtfsByTickers_isPresent() {
    // SETUP
    final CreditQualityCanadaPooledFundEndpoint m = new CreditQualityCanadaPooledFundEndpoint();

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
    final CreditQualityCanadaPooledFundEndpoint m = mock(CreditQualityCanadaPooledFundEndpoint.class);

    final PooledFundQuery pooledFundQuery = mock(PooledFundQuery.class);
    when(pooledFundQuery.creditQualityRatings(any())).thenReturn(pooledFundQuery);
    when(pooledFundQuery.externalIdentifiers(any())).thenReturn(pooledFundQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final PooledFundQuery actual = m.requestMapper(pooledFundQuery);

    // VERIFY
    verify(actual).creditQualityRatings(any());
    verify(actual).externalIdentifiers(any());
  }

  @Test
  void responseMapper_verifyEquityCountryAllocationMapper() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final CreditQualityCanadaPooledFundEndpoint sut = mock(CreditQualityCanadaPooledFundEndpoint.class);

      final CanadaPooledFundHolding holding = mock(CanadaPooledFundHolding.class);

      final PooledFund entity = mock(PooledFund.class);
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
  void responseMapper_checkResult() throws Exception {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final CreditQualityCanadaPooledFundEndpoint sut = mock(CreditQualityCanadaPooledFundEndpoint.class);

      final PooledFund entity = mock(PooledFund.class);
      final CountryAllocation country = mock(CountryAllocation.class);
      when(entity.getEquityCountryAllocation()).thenReturn(country);
      final CanadaPooledFundHolding holding = mock(CanadaPooledFundHolding.class);
      when(holding.getType()).thenReturn(CASH);

      final Map<String, BigDecimal> asset = Map.of("T", BigDecimal.ONE);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.creditQualityMapper(any())).thenReturn(asset);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.toDomainHoldingType(any())).thenReturn(CASH);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      final CreditQuality actual = sut.responseMapper(entity, holding);

      // VERIFY
      assertEquals(new CreditQuality(holding.getType(), asset), actual);
    }
  }

}
