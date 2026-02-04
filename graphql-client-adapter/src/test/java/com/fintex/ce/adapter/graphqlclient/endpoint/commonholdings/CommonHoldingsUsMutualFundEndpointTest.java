package com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings;

import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.model.CommonHoldings;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.smclient.graphql.Holdings;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;
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

class CommonHoldingsUsMutualFundEndpointTest {

  @Test
  void getUsEtfsByTickers_isPresent() {
    // SETUP
    final var fundCanadaFDSEndpoint = new CommonHoldingsUsMutualFundEndpoint();
    final var query = mock(Query.class);
    final var expected = new ArrayList<UsFund>();

    when(query.getGetUsFundsByTickers()).thenReturn(expected);

    // ACT
    final Function<Query, List<UsFund>> actual = fundCanadaFDSEndpoint.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(query), expected);
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final var fundCanadaFDSEndpoint = mock(CommonHoldingsUsMutualFundEndpoint.class);
    final var usFundQuery = mock(UsFundQuery.class);

    when(usFundQuery.holdings(any())).thenReturn(usFundQuery);
    when(usFundQuery.externalIdentifiers(any())).thenReturn(usFundQuery);

    doCallRealMethod().when(fundCanadaFDSEndpoint).requestMapper(any());
    // ACT
    final UsFundQuery actual = fundCanadaFDSEndpoint.requestMapper(usFundQuery);

    // VERIFY
    verify(actual).holdings(any());
    verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
  }

  @Test
  void responseMapper_verifyTopCommonHoldingsMapper() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var fundCanadaFDSEndpoint = mock(CommonHoldingsUsMutualFundEndpoint.class);
      final var usMutualFundHolding = mock(UsMutualFundHolding.class);
      final var usFund = mock(UsFund.class);
      final var allocation = mock(Holdings.class);

      when(usFund.getHoldings()).thenReturn(allocation);

      doCallRealMethod().when(fundCanadaFDSEndpoint).responseMapper(any(), any());
      // ACT
      fundCanadaFDSEndpoint.responseMapper(usFund, usMutualFundHolding);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.topCommonHoldingsMapper(allocation));
    }
  }

  @Test
  void responseMapper_checkResult() throws Exception {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP

      final var fundCanadaFDSEndpoint = mock(CommonHoldingsUsMutualFundEndpoint.class);
      final var usFund = mock(UsFund.class);
      final var holding = mock(UsMutualFundHolding.class);
      final var allocation = mock(Holdings.class);
      final CommonHoldings actual = mock(CommonHoldings.class);

      when(usFund.getHoldings()).thenReturn(allocation);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.topCommonHoldingsMapper(any())).thenReturn(actual);

      doCallRealMethod().when(fundCanadaFDSEndpoint).responseMapper(any(), any());
      // ACT
      final CommonHoldings expected = fundCanadaFDSEndpoint.responseMapper(usFund, holding);

      // VERIFY
      assertSame(expected, actual);
    }
  }

}
