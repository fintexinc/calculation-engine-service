package com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector;

import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomebondsector.FixedIncomeBondSectorCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.smclient.graphql.FixedIncomeSecuritiesAllocation;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.PooledFundQuery;
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

class FixedIncomeBondSectorCanadaPooledFundEndpointTest {

  @Test
  void getUsEtfsByTickers_isPresent() {
    // SETUP
    final FixedIncomeBondSectorCanadaPooledFundEndpoint FixedIncomeBondSectorCanadaPooledFundEndpoint = new FixedIncomeBondSectorCanadaPooledFundEndpoint();

    final Query q = mock(Query.class);
    final ArrayList<PooledFund> expected = new ArrayList<>();

    when(q.getGetCanadaPooledFundsByMorningstarIds()).thenReturn(expected);

    // ACT
    final Function<Query, List<PooledFund>> actual = FixedIncomeBondSectorCanadaPooledFundEndpoint
        .getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(q), expected);
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final FixedIncomeBondSectorCanadaPooledFundEndpoint FixedIncomeBondSectorCanadaPooledFundEndpoint = mock(
        FixedIncomeBondSectorCanadaPooledFundEndpoint.class);

    final PooledFundQuery pooledFundQuery = mock(PooledFundQuery.class);
    when(pooledFundQuery.fixedIncomeSecuritiesAllocation(any())).thenReturn(pooledFundQuery);
    when(pooledFundQuery.externalIdentifiers(any())).thenReturn(pooledFundQuery);

    doCallRealMethod().when(FixedIncomeBondSectorCanadaPooledFundEndpoint).requestMapper(any());
    // ACT
    final PooledFundQuery actual = FixedIncomeBondSectorCanadaPooledFundEndpoint.requestMapper(pooledFundQuery);

    // VERIFY
    verify(actual).fixedIncomeSecuritiesAllocation(any());
    verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Test
  void responseMapper_verifyEquitySectorMapper() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var sut = mock(FixedIncomeBondSectorCanadaPooledFundEndpoint.class);

      final PooledFund pooledFund = mock(PooledFund.class);
      final FixedIncomeSecuritiesAllocation allocation = mock(FixedIncomeSecuritiesAllocation.class);

      final FixedIncomeBondSecurities actual = mock(FixedIncomeBondSecurities.class);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(any(), any())).thenReturn(
          actual);
      when(pooledFund.getFixedIncomeSecuritiesAllocation()).thenReturn(allocation);
      final CanadaPooledFundHolding h = mock(CanadaPooledFundHolding.class);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      sut.responseMapper(pooledFund, h);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(allocation, h.getType()));
    }
  }

  @Test
  void responseMapper_checkResult() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var sut = mock(FixedIncomeBondSectorCanadaPooledFundEndpoint.class);

      final PooledFund pooledFund = mock(PooledFund.class);
      final FixedIncomeSecuritiesAllocation allocation = mock(FixedIncomeSecuritiesAllocation.class);
      when(pooledFund.getFixedIncomeSecuritiesAllocation()).thenReturn(allocation);
      final CanadaPooledFundHolding h = mock(CanadaPooledFundHolding.class);

      final FixedIncomeBondSecurities actual = mock(FixedIncomeBondSecurities.class);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.fixedIncomeBondSectorMapper(any(), any())).thenReturn(
          actual);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      final FixedIncomeBondSecurities expected = sut.responseMapper(pooledFund, h);

      // VERIFY
      assertSame(expected, actual);
    }
  }

}
