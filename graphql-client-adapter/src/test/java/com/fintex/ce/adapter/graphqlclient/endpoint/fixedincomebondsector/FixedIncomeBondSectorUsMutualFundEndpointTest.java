package com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector;

import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.smclient.graphql.FixedIncomeSecuritiesAllocation;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
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

class FixedIncomeBondSectorUsMutualFundEndpointTest {

  @Test
  void getUsEtfsByTickers_isPresent() {
    // SETUP
    final FixedIncomeBondSectorUsMutualFundEndpoint FixedIncomeBondSectorUsMutualFundEndpoint = new FixedIncomeBondSectorUsMutualFundEndpoint();

    final Query q = mock(Query.class);
    final ArrayList<UsFund> expected = new ArrayList<>();

    when(q.getGetUsFundsByTickers()).thenReturn(expected);

    // ACT
    final Function<Query, List<UsFund>> actual = FixedIncomeBondSectorUsMutualFundEndpoint.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(q), expected);
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final FixedIncomeBondSectorUsMutualFundEndpoint FixedIncomeBondSectorUsMutualFundEndpoint = mock(
        FixedIncomeBondSectorUsMutualFundEndpoint.class);

    final UsFundQuery usFundQuery = mock(UsFundQuery.class);
    when(usFundQuery.fixedIncomeSecuritiesAllocation(any())).thenReturn(usFundQuery);
    when(usFundQuery.externalIdentifiers(any())).thenReturn(usFundQuery);

    doCallRealMethod().when(FixedIncomeBondSectorUsMutualFundEndpoint).requestMapper(any());
    // ACT
    final UsFundQuery actual = FixedIncomeBondSectorUsMutualFundEndpoint.requestMapper(usFundQuery);

    // VERIFY
    verify(actual).fixedIncomeSecuritiesAllocation(any());
    verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Test
  void responseMapper_verifyEquitySectorMapper() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var sut = mock(FixedIncomeBondSectorUsMutualFundEndpoint.class);

      final UsFund usFund = mock(UsFund.class);
      final FixedIncomeSecuritiesAllocation allocation = mock(FixedIncomeSecuritiesAllocation.class);

      final FixedIncomeBondSecurities actual = mock(FixedIncomeBondSecurities.class);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(any(), any())).thenReturn(
          actual);
      when(usFund.getFixedIncomeSecuritiesAllocation()).thenReturn(allocation);
      final UsMutualFundHolding h = mock(UsMutualFundHolding.class);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      sut.responseMapper(usFund, h);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(allocation, h.getType()));
    }
  }

  @Test
  void responseMapper_checkResult() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var sut = mock(FixedIncomeBondSectorUsMutualFundEndpoint.class);

      final UsFund usFund = mock(UsFund.class);
      final FixedIncomeSecuritiesAllocation allocation = mock(FixedIncomeSecuritiesAllocation.class);
      when(usFund.getFixedIncomeSecuritiesAllocation()).thenReturn(allocation);
      final UsMutualFundHolding h = mock(UsMutualFundHolding.class);

      final FixedIncomeBondSecurities actual = mock(FixedIncomeBondSecurities.class);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(any(), any())).thenReturn(
          actual);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      final FixedIncomeBondSecurities expected = sut.responseMapper(usFund, h);

      // VERIFY
      assertSame(expected, actual);
    }
  }

}
