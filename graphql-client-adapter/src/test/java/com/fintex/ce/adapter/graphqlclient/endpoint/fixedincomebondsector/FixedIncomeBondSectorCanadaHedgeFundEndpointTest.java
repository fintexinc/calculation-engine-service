package com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector;

import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.smclient.graphql.FixedIncomeSecuritiesAllocation;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
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

class FixedIncomeBondSectorCanadaHedgeFundEndpointTest {

  @Test
  void getUsEtfsByTickers_isPresent() {
    // SETUP
    final FixedIncomeBondSectorCanadaHedgeFundEndpoint FixedIncomeBondSectorCanadaHedgeFundEndpoint = new FixedIncomeBondSectorCanadaHedgeFundEndpoint();

    final Query q = mock(Query.class);
    final ArrayList<HedgeFund> expected = new ArrayList<>();

    when(q.getGetCanadaHedgeFundsByMorningstarIds()).thenReturn(expected);

    // ACT
    final Function<Query, List<HedgeFund>> actual = FixedIncomeBondSectorCanadaHedgeFundEndpoint
        .getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(q), expected);
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final FixedIncomeBondSectorCanadaHedgeFundEndpoint FixedIncomeBondSectorCanadaHedgeFundEndpoint = mock(
        FixedIncomeBondSectorCanadaHedgeFundEndpoint.class);

    final HedgeFundQuery hedgeFundQuery = mock(HedgeFundQuery.class);
    when(hedgeFundQuery.fixedIncomeSecuritiesAllocation(any())).thenReturn(hedgeFundQuery);
    when(hedgeFundQuery.externalIdentifiers(any())).thenReturn(hedgeFundQuery);

    doCallRealMethod().when(FixedIncomeBondSectorCanadaHedgeFundEndpoint).requestMapper(any());
    // ACT
    final HedgeFundQuery actual = FixedIncomeBondSectorCanadaHedgeFundEndpoint.requestMapper(hedgeFundQuery);

    // VERIFY
    verify(actual).fixedIncomeSecuritiesAllocation(any());
    verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Test
  void responseMapper_verifyEquitySectorMapper() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var sut = mock(FixedIncomeBondSectorCanadaHedgeFundEndpoint.class);

      final HedgeFund fundSeries = mock(HedgeFund.class);
      final FixedIncomeSecuritiesAllocation allocation = mock(FixedIncomeSecuritiesAllocation.class);

      final FixedIncomeBondSecurities actual = mock(FixedIncomeBondSecurities.class);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(any(), any())).thenReturn(
          actual);
      when(fundSeries.getFixedIncomeSecuritiesAllocation()).thenReturn(allocation);
      final CanadaHedgeFundHolding h = mock(CanadaHedgeFundHolding.class);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      sut.responseMapper(fundSeries, h);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(allocation, h.getType()));
    }
  }

  @Test
  void responseMapper_checkResult() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var sut = mock(FixedIncomeBondSectorCanadaHedgeFundEndpoint.class);

      final HedgeFund hedgeFund = mock(HedgeFund.class);
      final FixedIncomeSecuritiesAllocation allocation = mock(FixedIncomeSecuritiesAllocation.class);
      when(hedgeFund.getFixedIncomeSecuritiesAllocation()).thenReturn(allocation);
      final CanadaHedgeFundHolding h = mock(CanadaHedgeFundHolding.class);

      final FixedIncomeBondSecurities actual = mock(FixedIncomeBondSecurities.class);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(any(), any())).thenReturn(
          actual);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      final FixedIncomeBondSecurities expected = sut.responseMapper(hedgeFund, h);

      // VERIFY
      assertSame(expected, actual);
    }
  }

}
