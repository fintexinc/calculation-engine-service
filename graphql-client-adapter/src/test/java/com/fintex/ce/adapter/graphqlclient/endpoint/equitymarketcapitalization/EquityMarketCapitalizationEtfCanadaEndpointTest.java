package com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization;

import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.equitymarketcapitalization.EquityMarketCapitalizationFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.smclient.graphql.EquityMarketCapitalization;
import com.fintex.smclient.graphql.EquityMarketCapitalizationQuery;
import com.fintex.smclient.graphql.EquityMarketCapitalizationQueryDefinition;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EquityMarketCapitalizationEtfCanadaEndpointTest {

  @Test
  void getGetCanadaEtfsByTickers_isPresent() {
    // SETUP
    final EquityMarketCapitalizationEtfCanadaEndpoint m = new EquityMarketCapitalizationEtfCanadaEndpoint();

    final Query q = mock(Query.class);
    final ArrayList<Etf> expected = new ArrayList<>();

    when(q.getGetCanadaEtfsByTickers()).thenReturn(expected);

    // ACT
    final Function<Query, List<Etf>> actual = m.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(q), expected);
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final EquityMarketCapitalizationEtfCanadaEndpoint m = mock(EquityMarketCapitalizationEtfCanadaEndpoint.class);

    final EtfQuery fundSeriesQuery = mock(EtfQuery.class);
    when(fundSeriesQuery.equityMarketCapitalization(any())).thenReturn(fundSeriesQuery);
    when(fundSeriesQuery.ticker(any())).thenReturn(fundSeriesQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final EtfQuery actual = m.requestMapper(fundSeriesQuery);

    // VERIFY
    verify(actual).equityMarketCapitalization(any());
    verify(actual).ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
  }

  @Test
  void equitySectorAllocationQueryDefinition_checkResult() {
    // SETUP
    final EquityMarketCapitalizationQueryDefinition actual = EquityMarketCapitalizationFundCanadaEndpoint
        .getEquityMarketCapitalizationQueryDefinition();

    final EquityMarketCapitalizationQuery equityM = mock(EquityMarketCapitalizationQuery.class);

    when(equityM.dataProvider()).thenReturn(equityM);
    when(equityM.values(any())).thenReturn(equityM);

    // ACT
    actual.define(equityM);

    // VERIFY
    verify(equityM).dataProvider();
    verify(equityM).values(any());
  }

  @Test
  void responseMapper_verifyEquityMarketCapitalizationMapper() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var sut = mock(EquityMarketCapitalizationEtfCanadaEndpoint.class);

      final var etf = mock(Etf.class);
      final var equityMarketCapitalization = mock(EquityMarketCapitalization.class);
      when(etf.getEquityMarketCapitalization()).thenReturn(equityMarketCapitalization);
      final EtfHolding h = mock(EtfHolding.class);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      sut.responseMapper(etf, h);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.equityMarketCapitalizationMapper(
          equityMarketCapitalization));
    }
  }

  @Test
  void responseMapper_checkResult() throws Exception {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final EquityMarketCapitalizationEtfCanadaEndpoint sut = mock(EquityMarketCapitalizationEtfCanadaEndpoint.class);

      final Etf etf = mock(Etf.class);
      final EquityMarketCapitalization allocation = mock(EquityMarketCapitalization.class);
      when(etf.getEquityMarketCapitalization()).thenReturn(allocation);
      final EtfHolding h = mock(EtfHolding.class);

      final com.fintex.ce.domain.model.EquityMarketCapitalization actual = mock(
          com.fintex.ce.domain.model.EquityMarketCapitalization.class);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.equityMarketCapitalizationMapper(any())).thenReturn(
          actual);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      final com.fintex.ce.domain.model.EquityMarketCapitalization expected = sut.responseMapper(etf, h);

      // VERIFY
      assertSame(expected, actual);
    }
  }

}