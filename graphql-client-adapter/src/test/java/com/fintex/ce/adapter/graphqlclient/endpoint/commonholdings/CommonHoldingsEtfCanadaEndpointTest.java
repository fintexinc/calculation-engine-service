package com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings;

import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.CommonHoldings;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Holdings;
import com.fintex.smclient.graphql.HoldingsQuery;
import com.fintex.smclient.graphql.HoldingsQueryDefinition;
import com.fintex.smclient.graphql.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommonHoldingsEtfCanadaEndpointTest {

  @Test
  void getGetCanadaEtfsByTickers_isPresent() {
    // SETUP
    final var etfCanadaEndpoint = new CommonHoldingsEtfCanadaEndpoint();
    final var query = mock(Query.class);
    final var expected = new ArrayList<Etf>();

    when(query.getGetCanadaEtfsByTickers()).thenReturn(expected);

    // ACT
    final Function<Query, List<Etf>> actual = etfCanadaEndpoint.getGetSMEntityFunction();

    // VERIFY
    assertSame(actual.apply(query), expected);
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final var etfCanadaEndpoint = mock(CommonHoldingsEtfCanadaEndpoint.class);
    final var etfQuery = mock(EtfQuery.class);

    when(etfQuery.holdings(any())).thenReturn(etfQuery);
    when(etfQuery.ticker(any())).thenReturn(etfQuery);

    doCallRealMethod().when(etfCanadaEndpoint).requestMapper(any());
    // ACT
    final EtfQuery actual = etfCanadaEndpoint.requestMapper(etfQuery);

    // VERIFY
    verify(actual).holdings(any());
    verify(actual).ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
  }

  @Test
  void getCommonHoldingsQueryDefinition_checkResult() {
    // SETUP
    final HoldingsQueryDefinition actual = CommonHoldingsEtfCanadaEndpoint.getCommonHoldingsQueryDefinition();
    final HoldingsQuery commonHoldingsQuery = mock(HoldingsQuery.class);

    when(commonHoldingsQuery.dataProvider()).thenReturn(commonHoldingsQuery);
    when(commonHoldingsQuery.asOfDate()).thenReturn(commonHoldingsQuery);
    when(commonHoldingsQuery.allocation(any())).thenReturn(commonHoldingsQuery);

    // ACT
    actual.define(commonHoldingsQuery);

    // VERIFY
    verify(commonHoldingsQuery).dataProvider();
    verify(commonHoldingsQuery).allocation(any());
  }

  @Test
  void responseMapper_verifyTopCommonHoldingsMapper() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var sut = mock(CommonHoldingsEtfCanadaEndpoint.class);
      final var etf = mock(Etf.class);
      final var holdings = mock(Holdings.class);
      final var holding = mock(EtfHolding.class);

      when(etf.getHoldings()).thenReturn(holdings);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      final CommonHoldings actual = sut.responseMapper(etf, holding);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.topCommonHoldingsMapper(holdings));
    }
  }

  @Test
  void responseMapper_checkResult() throws Exception {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var sut = mock(CommonHoldingsEtfCanadaEndpoint.class);
      final var etf = mock(Etf.class);
      final var holdings = mock(Holdings.class);
      final var holding = mock(EtfHolding.class);
      final var actual = mock(CommonHoldings.class);

      when(etf.getHoldings()).thenReturn(holdings);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.topCommonHoldingsMapper(any())).thenReturn(actual);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      final CommonHoldings expected = sut.responseMapper(etf, holding);

      // VERIFY
      assertSame(expected, actual);
    }
  }
}