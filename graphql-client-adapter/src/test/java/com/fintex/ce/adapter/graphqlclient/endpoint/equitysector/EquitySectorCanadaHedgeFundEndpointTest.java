package com.fintex.ce.adapter.graphqlclient.endpoint.equitysector;

import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.smclient.graphql.EquitySectorAllocation;
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

class EquitySectorCanadaHedgeFundEndpointTest {

  @Test
  void getUsEtfsByTickers_isPresent() {
    // SETUP
    final EquitySectorCanadaHedgeFundEndpoint m = new EquitySectorCanadaHedgeFundEndpoint();

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
    final EquitySectorCanadaHedgeFundEndpoint m = mock(EquitySectorCanadaHedgeFundEndpoint.class);

    final HedgeFundQuery hedgeFundQuery = mock(HedgeFundQuery.class);
    when(hedgeFundQuery.equitySectorAllocation(any())).thenReturn(hedgeFundQuery);
    when(hedgeFundQuery.externalIdentifiers(any())).thenReturn(hedgeFundQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final HedgeFundQuery actual = m.requestMapper(hedgeFundQuery);

    // VERIFY
    verify(actual).equitySectorAllocation(any());
    verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Test
  void responseMapper_verifyEquitySectorMapper() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var sut = mock(EquitySectorCanadaHedgeFundEndpoint.class);

      final HedgeFund hedgeFund = mock(HedgeFund.class);
      final EquitySectorAllocation allocation = mock(EquitySectorAllocation.class);
      when(hedgeFund.getEquitySectorAllocation()).thenReturn(allocation);
      final CanadaHedgeFundHolding h = mock(CanadaHedgeFundHolding.class);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      sut.responseMapper(hedgeFund, h);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.equitySectorMapper(allocation));
    }
  }

  @Test
  void responseMapper_checkResult() throws Exception {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final EquitySectorCanadaHedgeFundEndpoint sut = mock(EquitySectorCanadaHedgeFundEndpoint.class);

      final HedgeFund hedgeFund = mock(HedgeFund.class);
      final EquitySectorAllocation allocation = mock(EquitySectorAllocation.class);
      when(hedgeFund.getEquitySectorAllocation()).thenReturn(allocation);
      final CanadaHedgeFundHolding h = mock(CanadaHedgeFundHolding.class);

      final EquitySector actual = mock(EquitySector.class);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.equitySectorMapper(any())).thenReturn(actual);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      final EquitySector expected = sut.responseMapper(hedgeFund, h);

      // VERIFY
      assertSame(expected, actual);
    }
  }

}
