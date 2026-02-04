package com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation;

import com.fintex.ce.adapter.graphqlclient.endpoint.equitycountryallocation.EquityCountryAllocationFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.smclient.graphql.CountryAllocation;
import com.fintex.smclient.graphql.DataProvider;
import com.fintex.smclient.graphql.FundHoldingIdentifier;
import com.fintex.smclient.graphql.FundHoldingIdentifiersCodes;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.domain.enumeration.HoldingType.CASH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EquityCountryAllocationFundCanadaEndpointTest {

  @Test
  void getUsEtfsByTickers_isPresent() {
    // SETUP
    final EquityCountryAllocationFundCanadaEndpoint m = new EquityCountryAllocationFundCanadaEndpoint();

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
    final EquityCountryAllocationFundCanadaEndpoint m = mock(EquityCountryAllocationFundCanadaEndpoint.class);

    final FundSeriesQuery fundSeriesQuery = mock(FundSeriesQuery.class);
    when(fundSeriesQuery.equityCountryAllocation(any())).thenReturn(fundSeriesQuery);
    when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final FundSeriesQuery actual = m.requestMapper(fundSeriesQuery);

    // VERIFY
    verify(actual).equityCountryAllocation(any());
    verify(actual).externalIdentifiers(any());
  }

  @Test
  void queryDefinition_verify() {
    // SETUP
    final EquityCountryAllocationFundCanadaEndpoint m = mock(EquityCountryAllocationFundCanadaEndpoint.class);

    final QueryQuery qq = mock(QueryQuery.class);

    final FundHoldingIdentifiersCodes codes = mock(FundHoldingIdentifiersCodes.class);
    final String code = "CODE";
    when(codes.getCode()).thenReturn(code);
    final FundHoldingIdentifier cash = FundHoldingIdentifier.CASH;
    when(codes.getFundholdingIdentifier()).thenReturn(cash);
    final List<FundHoldingIdentifiersCodes> equityIdentifiers = List.of(codes);

    final UnaryOperator func = mock(UnaryOperator.class);
    when(func.apply(any())).thenReturn(mock(Object.class));

    doCallRealMethod().when(m).queryDefinition(any(), any());
    // ACT
    final QueryQueryDefinition actual = m.queryDefinition(equityIdentifiers, func);
    actual.define(qq);

    // VERIFY
    verify(qq).getFundSeriesByHoldingCodes(eq(equityIdentifiers), any());
  }

  @Test
  void responseMapper_verifyEquityCountryAllocationMapper() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var sut = mock(EquityCountryAllocationFundCanadaEndpoint.class);

      final FundSeriesHolding holding = mock(FundSeriesHolding.class);
      final FundSeries entity = mock(FundSeries.class);
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
      final var sut = mock(EquityCountryAllocationFundCanadaEndpoint.class);

      final FundSeries entity = mock(FundSeries.class);
      final CountryAllocation country = mock(CountryAllocation.class);
      final FundSeriesHolding holding = mock(FundSeriesHolding.class);
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