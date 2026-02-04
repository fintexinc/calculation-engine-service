package com.fintex.ce.adapter.graphqlclient.endpoint.averagemer;

import com.fintex.ce.adapter.graphqlclient.endpoint.averagemer.AverageMERFundCanadaEndpoint;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.AverageMer;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.FundHoldingIdentifier;
import com.fintex.smclient.graphql.FundHoldingIdentifiersCodes;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.ManagementFeeDatapoint;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION;
import static com.fintex.smclient.graphql.DataProvider.EAGLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AverageMERFundCanadaEndpointTest {

  @Test
  void getGetUsEtfsByTickers_isPresent() {
    // SETUP
    final AverageMERFundCanadaEndpoint m = new AverageMERFundCanadaEndpoint();

    final Query q = mock(Query.class);
    final ArrayList<FundSeries> expected = new ArrayList<>();

    when(q.getGetFundSeriesByHoldingCodes()).thenReturn(expected);

    // ACT
    final Function<Query, List<FundSeries>> actual = m.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(q), expected);
  }

  @Test
  void queryDefinition_verify() {
    // SETUP
    final AverageMERFundCanadaEndpoint m = mock(AverageMERFundCanadaEndpoint.class);

    final QueryQuery qq = mock(QueryQuery.class);

    final FundHoldingIdentifiersCodes codes = mock(FundHoldingIdentifiersCodes.class);
    final String code = "CODE";
    when(codes.getCode()).thenReturn(code);
    final FundHoldingIdentifier cash = FundHoldingIdentifier.CASH;
    when(codes.getFundholdingIdentifier()).thenReturn(cash);
    final List<FundHoldingIdentifiersCodes> equityIdentifiers = List.of(codes);

    doCallRealMethod().when(m).queryDefinition(any(), any());
    // ACT
    final QueryQueryDefinition actual = m.queryDefinition(equityIdentifiers, mock(UnaryOperator.class));
    actual.define(qq);

    // VERIFY
    verify(qq).getFundSeriesByHoldingCodes(eq(equityIdentifiers), any());
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final AverageMERFundCanadaEndpoint m = mock(AverageMERFundCanadaEndpoint.class);

    final FundSeriesQuery fundSeriesQuery = mock(FundSeriesQuery.class);
    when(fundSeriesQuery.managementExpenseRatio(any())).thenReturn(fundSeriesQuery);
    when(fundSeriesQuery.managementFee(any())).thenReturn(fundSeriesQuery);
    when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);

    doCallRealMethod().when(m).requestMapper(any());
    // ACT
    final FundSeriesQuery actual = m.requestMapper(fundSeriesQuery);

    // VERIFY
    verify(actual).managementExpenseRatio(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION);
    verify(actual).managementFee(MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION);
    verify(actual).externalIdentifiers(any());
  }

  @Test
  void responseMapper_checkResult() {
    // SETUP
    final AverageMERFundCanadaEndpoint a = mock(AverageMERFundCanadaEndpoint.class);

    final FundSeries etf = mock(FundSeries.class);
    final FloatDatapoint managementExpenseFloatDatapoint = mock(FloatDatapoint.class);
    when(etf.getManagementExpenseRatio()).thenReturn(managementExpenseFloatDatapoint);
    when(managementExpenseFloatDatapoint.getValue()).thenReturn(BigDecimal.ONE);
    when(managementExpenseFloatDatapoint.getDataProvider()).thenReturn(EAGLE);

    final ManagementFeeDatapoint managementFloatDatapoint = mock(ManagementFeeDatapoint.class);
    when(managementFloatDatapoint.getValue()).thenReturn(BigDecimal.TEN);
    when(etf.getManagementFee()).thenReturn(managementFloatDatapoint);
    when(managementFloatDatapoint.getDataProvider()).thenReturn(EAGLE);

    final var expected = new AverageMer();
    expected.setMer(managementExpenseFloatDatapoint.getValue());
    expected.setActualManagementFee(managementFloatDatapoint.getValue());
    expected.setMerProvider(DataProvider.EAGLE.name());
    expected.setActualManagementFeeProvider(DataProvider.EAGLE.name());

    doCallRealMethod().when(a).responseMapper(any(), any());
    // ACT
    final var actual = a.responseMapper(etf, null);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void responseMapper_checkResultNull() {
    // SETUP
    final var sut = mock(AverageMERFundCanadaEndpoint.class);

    final FundSeries etf = mock(FundSeries.class);
    final ManagementFeeDatapoint managementFloatDatapoint = mock(ManagementFeeDatapoint.class);
    when(managementFloatDatapoint.getValue()).thenReturn(BigDecimal.TEN);
    when(etf.getManagementFee()).thenReturn(managementFloatDatapoint);
    when(managementFloatDatapoint.getDataProvider()).thenReturn(EAGLE);

    final AverageMer expected = new AverageMer();
    expected.setMer(null);
    expected.setActualManagementFee(managementFloatDatapoint.getValue());
    expected.setActualManagementFeeProvider(DataProvider.EAGLE.name());

    doCallRealMethod().when(sut).responseMapper(any(), any());
    // ACT
    final var actual = sut.responseMapper(etf, null);

    // VERIFY

    assertEquals(expected, actual);
  }

}
