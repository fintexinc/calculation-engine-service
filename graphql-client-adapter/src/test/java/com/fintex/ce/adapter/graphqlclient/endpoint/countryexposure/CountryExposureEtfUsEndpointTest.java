package com.fintex.ce.adapter.graphqlclient.endpoint.countryexposure;

import com.fintex.ce.adapter.graphqlclient.endpoint.countryexposure.CountryExposureEtfUsEndpoint;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.UnaryOperator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CountryExposureEtfUsEndpointTest {

  @Test
  void queryDefinition_verify() {
    // SETUP
    final CountryExposureEtfUsEndpoint m = mock(CountryExposureEtfUsEndpoint.class);

    final QueryQuery qq = mock(QueryQuery.class);

    final List<String> equityIdentifiers = List.of("TEST");

    final UnaryOperator func = mock(UnaryOperator.class);
    when(func.apply(any())).thenReturn(mock(Object.class));

    doCallRealMethod().when(m).queryDefinition(any(), any());
    // ACT
    final QueryQueryDefinition actual = m.queryDefinition(equityIdentifiers, func);
    actual.define(qq);

    // VERIFY
    verify(qq).getUsEtfsByTickers(eq(equityIdentifiers), any());
  }

}