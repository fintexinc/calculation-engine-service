package com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure;

import com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure.EquityStyleboxExposureCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.HedgeFundQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.StyleBoxType;
import com.fintex.smclient.graphql.StyleBoxValue;
import com.fintex.smclient.graphql.StyleBoxes;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EquityStyleboxExposureCanadaHedgeFundEndpointTest {

  @Test
  void getUsEtfsByTickers_isPresent() {
    // SETUP
    final EquityStyleboxExposureCanadaHedgeFundEndpoint m = new EquityStyleboxExposureCanadaHedgeFundEndpoint();

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
    final EquityStyleboxExposureCanadaHedgeFundEndpoint m = mock(EquityStyleboxExposureCanadaHedgeFundEndpoint.class);

    final HedgeFundQuery hedgeFundQuery = mock(HedgeFundQuery.class);
    when(hedgeFundQuery.styleBoxes(any())).thenReturn(hedgeFundQuery);
    when(hedgeFundQuery.externalIdentifiers(any())).thenReturn(hedgeFundQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final HedgeFundQuery actual = m.requestMapper(hedgeFundQuery);

    // VERIFY
    verify(actual).styleBoxes(any());
    verify(actual).externalIdentifiers(any());
  }

  @Test
  void responseMapper_verify() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final EquityStyleboxExposureCanadaHedgeFundEndpoint sut = mock(
          EquityStyleboxExposureCanadaHedgeFundEndpoint.class);

      final CanadaHedgeFundHolding holding = mock(CanadaHedgeFundHolding.class);

      final HedgeFund entity = mock(HedgeFund.class);
      final BigDecimal value = mock(BigDecimal.class);
      final StyleBoxes styleBoxes = mock(StyleBoxes.class);
      final StyleBoxValue styleBoxValue = mock(StyleBoxValue.class);

      when(entity.getStyleBoxes()).thenReturn(styleBoxes);
      when(styleBoxes.getBoxValues()).thenReturn(List.of(styleBoxValue));
      when(styleBoxValue.getStyleBoxType()).thenReturn(StyleBoxType.LARGE_CORE);
      when(styleBoxValue.getValue()).thenReturn(value);

      doCallRealMethod().when(sut).responseMapper(any(), any());

      // ACT
      final EquityStyleboxExposure result = sut.responseMapper(entity, holding);

      // VERIFY
      assertNotNull(result);
      assertNotNull(result.getBoxValues());
      assertEquals(1, result.getBoxValues().size());

      final Map.Entry<String, BigDecimal> entry = result.getBoxValues().entrySet().stream().findFirst().orElseThrow();
      assertEquals(StyleBoxType.LARGE_CORE.toString(), entry.getKey());
      assertEquals(value, entry.getValue());
    }
  }

}
