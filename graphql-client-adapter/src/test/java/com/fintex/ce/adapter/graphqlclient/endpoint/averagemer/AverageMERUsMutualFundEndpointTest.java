package com.fintex.ce.adapter.graphqlclient.endpoint.averagemer;

import com.fintex.ce.adapter.graphqlclient.endpoint.averagemer.AverageMERUsMutualFundEndpoint;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.AverageMer;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.smclient.graphql.DataProvider.EAGLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AverageMERUsMutualFundEndpointTest {

  @Test
  void getGetUsEtfsByTickers_isPresent() {
    // SETUP
    final AverageMERUsMutualFundEndpoint m = new AverageMERUsMutualFundEndpoint();

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
    final AverageMERUsMutualFundEndpoint m = mock(AverageMERUsMutualFundEndpoint.class);

    final UsFundQuery usFundQuery = mock(UsFundQuery.class);

    when(usFundQuery.netExpenseRatio(any())).thenReturn(usFundQuery);
    when(usFundQuery.grossExpenseRatio(any())).thenReturn(usFundQuery);
    when(usFundQuery.externalIdentifiers(any())).thenReturn(usFundQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final UsFundQuery actual = m.requestMapper(usFundQuery);

    // VERIFY
    verify(actual).netExpenseRatio(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION);
    verify(actual).grossExpenseRatio(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION);
    verify(actual).externalIdentifiers(any());
  }

  @Test
  void responseMapper_checkResult() {
    // SETUP
    final AverageMERUsMutualFundEndpoint a = mock(AverageMERUsMutualFundEndpoint.class);

    final UsFund usFund = mock(UsFund.class);
    final FloatDatapoint netExpenseRatioDatapoint = mock(FloatDatapoint.class);
    when(usFund.getNetExpenseRatio()).thenReturn(netExpenseRatioDatapoint);
    when(netExpenseRatioDatapoint.getValue()).thenReturn(BigDecimal.ONE);
    when(netExpenseRatioDatapoint.getDataProvider()).thenReturn(EAGLE);

    final FloatDatapoint grossExpenseRatioDatapoint = mock(FloatDatapoint.class);
    when(grossExpenseRatioDatapoint.getValue()).thenReturn(BigDecimal.TEN);
    when(usFund.getGrossExpenseRatio()).thenReturn(grossExpenseRatioDatapoint);
    when(grossExpenseRatioDatapoint.getDataProvider()).thenReturn(EAGLE);

    final var expected = new AverageMer();
    expected.setNetExpenseRatio(netExpenseRatioDatapoint.getValue());
    expected.setGrossExpenseRatio(grossExpenseRatioDatapoint.getValue());
    expected.setNetExpenseRatioProvider(DataProvider.EAGLE.name());
    expected.setGrossExpenseRatioProvider(DataProvider.EAGLE.name());

    doCallRealMethod().when(a).responseMapper(any(), any());
    // ACT
    final var actual = a.responseMapper(usFund, null);

    // VERIFY
    assertEquals(expected, actual);
  }

}
