package com.fintex.ce.repository.graphql.query.endpoint.assetallocation;

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

class AssetAllocationEtfUsEndpointTest {

    @Test
    void queryDefinition_verify() {
        //SETUP
        final AssetAllocationEtfUsEndpoint m = mock(AssetAllocationEtfUsEndpoint.class);

        final QueryQuery qq = mock(QueryQuery.class);

        final String tickets = "TICKETS";
        final List<String> equityIdentifiers = List.of(tickets);

        doCallRealMethod().when(m).queryDefinition(any(), any());
        //ACT
        final QueryQueryDefinition actual = m.queryDefinition(equityIdentifiers, mock(UnaryOperator.class));
        actual.define(qq);

        //VERIFY
        verify(qq).getUsEtfsByTickers(eq(equityIdentifiers), any());
    }

}