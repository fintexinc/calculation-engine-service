package com.fintex.ce.adapter.graphqlclient.endpoint.creditquality;

import com.fintex.ce.adapter.graphqlclient.endpoint.creditquality.CreditQualityEtfUsEndpoint;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.UnaryOperator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CreditQualityEtfUsEndpointTest {

  @Test
  void queryDefinition_verify() {
    // SETUP
    final CreditQualityEtfUsEndpoint m = mock(CreditQualityEtfUsEndpoint.class);

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