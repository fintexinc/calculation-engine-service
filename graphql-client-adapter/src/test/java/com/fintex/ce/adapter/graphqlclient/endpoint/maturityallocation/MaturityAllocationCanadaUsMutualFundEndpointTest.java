package com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation;

import com.fintex.ce.adapter.graphqlclient.endpoint.maturityallocation.MaturityAllocationCanadaUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.MaturityAllocation;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.smclient.graphql.Maturities;
import com.fintex.smclient.graphql.MaturityDurationValue;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.TimeDuration;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MaturityAllocationCanadaUsMutualFundEndpointTest {

  @Test
  void getGetBy_isPresent() {
    // SETUP
    final MaturityAllocationCanadaUsMutualFundEndpoint m = new MaturityAllocationCanadaUsMutualFundEndpoint();

    final Query q = mock(Query.class);
    final ArrayList<UsFund> expected = new ArrayList<>();

    when(q.getGetUsFundsByTickers()).thenReturn(expected);

    // ACT
    final Function<Query, List<UsFund>> actual = m.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(q), expected);
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final MaturityAllocationCanadaUsMutualFundEndpoint m = mock(MaturityAllocationCanadaUsMutualFundEndpoint.class);

    final UsFundQuery usFundQuery = mock(UsFundQuery.class);
    when(usFundQuery.maturities(any())).thenReturn(usFundQuery);
    when(usFundQuery.externalIdentifiers(any())).thenReturn(usFundQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final UsFundQuery actual = m.requestMapper(usFundQuery);

    // VERIFY
    verify(actual).maturities(any());
    verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Test
  void responseMapper_verify() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final MaturityAllocationCanadaUsMutualFundEndpoint sut = mock(MaturityAllocationCanadaUsMutualFundEndpoint.class);

      final UsMutualFundHolding holding = mock(UsMutualFundHolding.class);

      final UsFund entity = mock(UsFund.class);
      final BigDecimal value = mock(BigDecimal.class);
      final Maturities maturities = mock(Maturities.class);
      final MaturityDurationValue maturityDurationValue = mock(MaturityDurationValue.class);
      when(entity.getMaturities()).thenReturn(maturities);
      when(maturities.getPeriods()).thenReturn(List.of(maturityDurationValue));
      when(maturityDurationValue.getValue()).thenReturn(value);
      when(maturityDurationValue.getMaturityDuration()).thenReturn(TimeDuration.FIVE_TO_SEVEN_YEARS);

      doCallRealMethod().when(sut).responseMapper(any(), any());

      // ACT
      final MaturityAllocation result = sut.responseMapper(entity, holding);

      // VERIFY
      assertNotNull(result);
      assertNotNull(result.getMaturityDurationValues());
      assertEquals(1, result.getMaturityDurationValues().size());
      final Map.Entry<String, BigDecimal> entry = result.getMaturityDurationValues().entrySet().stream().findFirst()
          .orElseThrow();
      assertEquals(TimeDuration.FIVE_TO_SEVEN_YEARS.toString(), entry.getKey());
      assertEquals(value, entry.getValue());
    }
  }

}
