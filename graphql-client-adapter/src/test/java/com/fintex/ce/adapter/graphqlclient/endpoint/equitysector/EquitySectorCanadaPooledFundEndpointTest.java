package com.fintex.ce.adapter.graphqlclient.endpoint.equitysector;

import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorCanadaPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.smclient.graphql.EquitySectorAllocation;
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

class EquitySectorCanadaPooledFundEndpointTest {

  @Test
  void getUsEtfsByTickers_isPresent() {
    // SETUP
    final EquitySectorCanadaPooledFundEndpoint m = new EquitySectorCanadaPooledFundEndpoint();

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
    final EquitySectorCanadaPooledFundEndpoint m = mock(EquitySectorCanadaPooledFundEndpoint.class);

    final PooledFundQuery pooledFundQuery = mock(PooledFundQuery.class);
    when(pooledFundQuery.equitySectorAllocation(any())).thenReturn(pooledFundQuery);
    when(pooledFundQuery.externalIdentifiers(any())).thenReturn(pooledFundQuery);

    doCallRealMethod().when(m).requestMapper(any());

    // ACT
    final PooledFundQuery actual = m.requestMapper(pooledFundQuery);

    // VERIFY
    verify(actual).equitySectorAllocation(any());
    verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Test
  void responseMapper_verifyEquitySectorMapper() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var sut = mock(EquitySectorCanadaPooledFundEndpoint.class);

      final PooledFund pooledFund = mock(PooledFund.class);
      final EquitySectorAllocation allocation = mock(EquitySectorAllocation.class);
      when(pooledFund.getEquitySectorAllocation()).thenReturn(allocation);
      final CanadaPooledFundHolding h = mock(CanadaPooledFundHolding.class);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      sut.responseMapper(pooledFund, h);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.equitySectorMapper(allocation));
    }
  }

  @Test
  void responseMapper_checkResult() throws Exception {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final EquitySectorCanadaPooledFundEndpoint sut = mock(EquitySectorCanadaPooledFundEndpoint.class);

      final PooledFund pooledFund = mock(PooledFund.class);
      final EquitySectorAllocation allocation = mock(EquitySectorAllocation.class);
      when(pooledFund.getEquitySectorAllocation()).thenReturn(allocation);
      final CanadaPooledFundHolding h = mock(CanadaPooledFundHolding.class);

      final EquitySector actual = mock(EquitySector.class);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.equitySectorMapper(any())).thenReturn(actual);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      final EquitySector expected = sut.responseMapper(pooledFund, h);

      // VERIFY
      assertSame(expected, actual);
    }
  }

}
