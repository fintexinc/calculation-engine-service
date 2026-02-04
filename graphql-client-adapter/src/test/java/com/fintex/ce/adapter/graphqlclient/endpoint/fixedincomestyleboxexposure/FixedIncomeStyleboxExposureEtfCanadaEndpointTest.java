package com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure;

import com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure.FixedIncomeStyleboxExposureEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.FixedIncomeStyleboxExposure;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.FixedIncomeStyleBoxType;
import com.fintex.smclient.graphql.FixedIncomeStyleBoxValue;
import com.fintex.smclient.graphql.FixedIncomeStyleBoxes;
import com.fintex.smclient.graphql.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FixedIncomeStyleboxExposureEtfCanadaEndpointTest {

  @Test
  void getGetCanadaEtfsByTickers_isPresent() {
    // SETUP
    final FixedIncomeStyleboxExposureEtfCanadaEndpoint m = new FixedIncomeStyleboxExposureEtfCanadaEndpoint();

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
    final FixedIncomeStyleboxExposureEtfCanadaEndpoint m = mock(FixedIncomeStyleboxExposureEtfCanadaEndpoint.class);

    final EtfQuery fundSeriesQuery = mock(EtfQuery.class);
    when(fundSeriesQuery.fixedIncomeStyleBoxes(any())).thenReturn(fundSeriesQuery);
    when(fundSeriesQuery.ticker(any())).thenReturn(fundSeriesQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final EtfQuery actual = m.requestMapper(fundSeriesQuery);

    // VERIFY
    verify(actual).fixedIncomeStyleBoxes(any());
    verify(actual).ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
  }

  @Test
  void responseMapper_verify() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final FixedIncomeStyleboxExposureEtfCanadaEndpoint sut = mock(FixedIncomeStyleboxExposureEtfCanadaEndpoint.class);

      final EtfHolding holding = mock(EtfHolding.class);

      final Etf entity = mock(Etf.class);
      final BigDecimal value = mock(BigDecimal.class);
      final FixedIncomeStyleBoxes styleBoxes = mock(FixedIncomeStyleBoxes.class);
      final FixedIncomeStyleBoxValue styleBoxValue = mock(FixedIncomeStyleBoxValue.class);

      when(entity.getFixedIncomeStyleBoxes()).thenReturn(styleBoxes);
      when(styleBoxes.getBoxValues()).thenReturn(List.of(styleBoxValue));
      when(styleBoxValue.getStyleBoxType()).thenReturn(FixedIncomeStyleBoxType.HIGH_LIMITED);
      when(styleBoxValue.getValue()).thenReturn(value);

      doCallRealMethod().when(sut).responseMapper(any(), any());

      // ACT
      final FixedIncomeStyleboxExposure result = sut.responseMapper(entity, holding);

      // VERIFY
      assertNotNull(result);
      assertNotNull(result.getBoxValues());
      assertEquals(1, result.getBoxValues().size());

      final Map.Entry<String, BigDecimal> entry = result.getBoxValues().entrySet().stream().findFirst().orElseThrow();
      assertEquals(FixedIncomeStyleBoxType.HIGH_LIMITED.toString(), entry.getKey());
      assertEquals(value, entry.getValue());
    }
  }

}
