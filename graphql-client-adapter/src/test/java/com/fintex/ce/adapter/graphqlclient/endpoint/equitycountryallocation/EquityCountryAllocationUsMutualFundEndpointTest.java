package com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation;

import com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation.EquityCountryAllocationUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.smclient.graphql.CountryAllocation;
import com.fintex.smclient.graphql.DataProvider;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
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

class EquityCountryAllocationUsMutualFundEndpointTest {

  @Test
  void getUsEtfsByTickers_isPresent() {
    // SETUP
    final EquityCountryAllocationUsMutualFundEndpoint m = new EquityCountryAllocationUsMutualFundEndpoint();

    final Query q = mock(Query.class);
    final ArrayList<UsFund> expected = new ArrayList<>();

    when(q.getGetUsFundsByTickers()).thenReturn(expected);

    // ACT
    final Function<Query, List<UsFund>> actual = m.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(q), expected);
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final EquityCountryAllocationUsMutualFundEndpoint m = mock(EquityCountryAllocationUsMutualFundEndpoint.class);

    final UsFundQuery fundSeriesQuery = mock(UsFundQuery.class);
    when(fundSeriesQuery.equityCountryAllocation(any())).thenReturn(fundSeriesQuery);
    when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final UsFundQuery actual = m.requestMapper(fundSeriesQuery);

    // VERIFY
    verify(actual).equityCountryAllocation(any());
    verify(actual).externalIdentifiers(any());
  }

  @Test
  void responseMapper_verifyEquityCountryAllocationMapper() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var sut = mock(EquityCountryAllocationUsMutualFundEndpoint.class);

      final UsMutualFundHolding holding = mock(UsMutualFundHolding.class);
      final UsFund entity = mock(UsFund.class);
      final CountryAllocation country = mock(CountryAllocation.class);

      when(entity.getEquityCountryAllocation()).thenReturn(country);
      when(country.getDataProvider()).thenReturn(DataProvider.EAGLE);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      sut.responseMapper(entity, holding);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.equityCountryAllocationMapper(country));
    }
  }

  @Test
  void responseMapper_checkResult() throws Exception {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var sut = mock(EquityCountryAllocationUsMutualFundEndpoint.class);

      final UsFund entity = mock(UsFund.class);
      final CountryAllocation country = mock(CountryAllocation.class);
      final UsMutualFundHolding holding = mock(UsMutualFundHolding.class);
      final Map<String, BigDecimal> asset = Map.of("T", BigDecimal.ONE);
      final var expected = new EquityCountryAllocation();
      expected.setHoldingType(CASH);
      expected.setAllocations(asset);
      expected.setProvider(DataProvider.EAGLE.name());

      when(entity.getEquityCountryAllocation()).thenReturn(country);
      when(country.getDataProvider()).thenReturn(DataProvider.EAGLE);
      when(holding.getType()).thenReturn(CASH);

      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.equityCountryAllocationMapper(any())).thenReturn(asset);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.toDomainHoldingType(any())).thenReturn(CASH);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      final var actual = sut.responseMapper(entity, holding);

      // VERIFY
      assertEquals(expected, actual);
    }
  }

}
