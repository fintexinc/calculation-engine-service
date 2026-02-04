package com.fintex.ce.adapter.graphqlclient.endpoint.countryexposure;

import com.fintex.ce.adapter.graphqlclient.endpoint.countryexposure.CountryExposureCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.smclient.graphql.CountryAllocation;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static com.fintex.ce.domain.enumeration.HoldingType.CASH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CountryExposureCanadaHedgeFundEndpointTest {

  @Test
  void getUsEtfsByTickers_isPresent() {
    // SETUP
    final CountryExposureCanadaHedgeFundEndpoint m = new CountryExposureCanadaHedgeFundEndpoint();

    final Query q = mock(Query.class);
    final ArrayList<HedgeFund> expected = new ArrayList<>();

    when(q.getGetCanadaHedgeFundsByMorningstarIds()).thenReturn(expected);

    // ACT
    final Function<Query, List<HedgeFund>> actual = m.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(q), expected);
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final CountryExposureCanadaHedgeFundEndpoint m = mock(CountryExposureCanadaHedgeFundEndpoint.class);

    final HedgeFundQuery hedgeFundQuery = mock(HedgeFundQuery.class);
    when(hedgeFundQuery.externalIdentifiers(any())).thenReturn(hedgeFundQuery);
    when(hedgeFundQuery.fixedIncomeCountryAllocation(any())).thenReturn(hedgeFundQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final HedgeFundQuery actual = m.requestMapper(hedgeFundQuery);

    // VERIFY
    verify(actual).externalIdentifiers(any());
    verify(actual).fixedIncomeCountryAllocation(any());
  }

  @Test
  void responseMapper_verifyCountryExposureMapper() {
    try (MockedStatic<GraphQlMapperUtils> mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final CountryExposureCanadaHedgeFundEndpoint m = mock(CountryExposureCanadaHedgeFundEndpoint.class);

      final CanadaHedgeFundHolding holding = mock(CanadaHedgeFundHolding.class);

      final HedgeFund entity = mock(HedgeFund.class);
      final CountryAllocation country = mock(CountryAllocation.class);
      when(entity.getFixedIncomeCountryAllocation()).thenReturn(country);

      doCallRealMethod().when(m).responseMapper(any(), any());

      // ACT
      m.responseMapper(entity, holding);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.countryExposureMapper(country));
    }
  }

  @Test
  void responseMapper_checkResult() throws Exception {
    try (MockedStatic<GraphQlMapperUtils> mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final CountryExposureCanadaHedgeFundEndpoint m = mock(CountryExposureCanadaHedgeFundEndpoint.class);

      final HedgeFund entity = mock(HedgeFund.class);
      final CountryAllocation country = mock(CountryAllocation.class);
      when(entity.getFixedIncomeCountryAllocation()).thenReturn(country);
      final CanadaHedgeFundHolding holding = mock(CanadaHedgeFundHolding.class);
      when(holding.getType()).thenReturn(CASH);

      final Map<String, BigDecimal> asset = Map.of("T", BigDecimal.ONE);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.countryExposureMapper(any())).thenReturn(asset);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.toDomainHoldingType(any())).thenReturn(CASH);

      doCallRealMethod().when(m).responseMapper(any(), any());
      // ACT
      final CountryExposure actual = m.responseMapper(entity, holding);

      // VERIFY
      assertEquals(new CountryExposure(holding.getType(), asset), actual);
    }
  }

}
