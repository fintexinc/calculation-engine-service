package com.fintex.ce.repository.graphql.query.endpoint.equitysector;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EquitySectorEtfUsEndpointTest {

    @Test
    void getUsEtfsByTickers_isPresent() {
        //SETUP
        final EquitySectorEtfUsEndpoint m = new EquitySectorEtfUsEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<Etf> expected = new ArrayList<>();

        when(q.getGetUsEtfsByTickers()).thenReturn(expected);

        //ACT
        final Function<Query, List<Etf>> actual = m.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void queryDefinition_verify() {
        //SETUP
        final EquitySectorEtfUsEndpoint m = mock(EquitySectorEtfUsEndpoint.class);

        final QueryQuery qq = mock(QueryQuery.class);

        final List<String> equityIdentifiers = List.of("TEST");

        final UnaryOperator func = mock(UnaryOperator.class);
        when(func.apply(any())).thenReturn(mock(Object.class));

        doCallRealMethod().when(m).queryDefinition(any(), any());
        //ACT
        final QueryQueryDefinition actual = m.queryDefinition(equityIdentifiers, func);
        actual.define(qq);

        //VERIFY
        verify(qq).getUsEtfsByTickers(eq(equityIdentifiers), any());
    }

}