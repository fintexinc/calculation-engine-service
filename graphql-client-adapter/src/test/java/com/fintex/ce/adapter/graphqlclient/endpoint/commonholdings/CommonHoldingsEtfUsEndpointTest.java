package com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings;

import com.fintex.ce.adapter.graphqlclient.endpoint.commonholdings.CommonHoldingsEtfUsEndpoint;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommonHoldingsEtfUsEndpointTest {

  @Test
  void getUsEtfsByTickers_isPresent() {
    // SETUP
    final var UsEtfFDSEndpoint = new CommonHoldingsEtfUsEndpoint();
    final var query = mock(Query.class);
    final var expected = new ArrayList<Etf>();

    when(query.getGetUsEtfsByTickers()).thenReturn(expected);

    // ACT
    final Function<Query, List<Etf>> actual = UsEtfFDSEndpoint.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(query), expected);
  }

  @Test
  void queryDefinition_verify() {
    // SETUP
    final var m = mock(CommonHoldingsEtfUsEndpoint.class);
    final var qq = mock(QueryQuery.class);
    final var commonHoldingsIdentifiers = List.of("test");
    final var func = mock(UnaryOperator.class);
    when(func.apply(any())).thenReturn(mock(Object.class));

    doCallRealMethod().when(m).queryDefinition(any(), any());
    // ACT
    final QueryQueryDefinition actual = m.queryDefinition(commonHoldingsIdentifiers, func);
    actual.define(qq);

    // VERIFY
    verify(qq).getUsEtfsByTickers(eq(commonHoldingsIdentifiers), any());
  }
}