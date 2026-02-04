package com.fintex.ce.adapter.graphqlclient.endpoint.equitysector;

import com.fintex.ce.adapter.graphqlclient.endpoint.equitysector.EquitySectorEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.smclient.graphql.EquitySectorAllocation;
import com.fintex.smclient.graphql.EquitySectorAllocationQuery;
import com.fintex.smclient.graphql.EquitySectorAllocationQueryDefinition;
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

class EquitySectorEtfCanadaEndpointTest {

  @Test
  void getGetCanadaEtfsByTickers_isPresent() {
    // SETUP
    final EquitySectorEtfCanadaEndpoint m = new EquitySectorEtfCanadaEndpoint();

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
    final EquitySectorEtfCanadaEndpoint m = mock(EquitySectorEtfCanadaEndpoint.class);

    final EtfQuery fundSeriesQuery = mock(EtfQuery.class);
    when(fundSeriesQuery.equitySectorAllocation(any())).thenReturn(fundSeriesQuery);
    when(fundSeriesQuery.ticker(any())).thenReturn(fundSeriesQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final EtfQuery actual = m.requestMapper(fundSeriesQuery);

    // VERIFY
    verify(actual).equitySectorAllocation(any());
    verify(actual).ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
  }

  @Test
  void equitySectorAllocationQueryDefinition_checkResult() {
    // SETUP
    final EquitySectorAllocationQueryDefinition actual = EquitySectorEtfCanadaEndpoint
        .equitySectorAllocationQueryDefinition();

    final EquitySectorAllocationQuery equityM = mock(EquitySectorAllocationQuery.class);

    when(equityM.dataProvider()).thenReturn(equityM);
    when(equityM.allocation(any())).thenReturn(equityM);

    // ACT
    actual.define(equityM);

    // VERIFY
    verify(equityM).dataProvider();
    verify(equityM).allocation(any());
  }

  @Test
  void responseMapper_verifyEquitySectorMapper() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var sut = mock(EquitySectorEtfCanadaEndpoint.class);

      final Etf etf = mock(Etf.class);
      final EquitySectorAllocation allocation = mock(EquitySectorAllocation.class);
      when(etf.getEquitySectorAllocation()).thenReturn(allocation);
      final EtfHolding h = mock(EtfHolding.class);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      sut.responseMapper(etf, h);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.equitySectorMapper(allocation));
    }
  }

  @Test
  void responseMapper_checkResult() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final EquitySectorEtfCanadaEndpoint sut = mock(EquitySectorEtfCanadaEndpoint.class);

      final Etf etf = mock(Etf.class);
      final EquitySectorAllocation allocation = mock(EquitySectorAllocation.class);
      when(etf.getEquitySectorAllocation()).thenReturn(allocation);
      final EtfHolding h = mock(EtfHolding.class);

      final EquitySector actual = mock(EquitySector.class);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.equitySectorMapper(any())).thenReturn(actual);

      doCallRealMethod().when(sut).responseMapper(any(), any());
      // ACT
      final EquitySector expected = sut.responseMapper(etf, h);

      // VERIFY
      assertSame(expected, actual);
    }
  }

}