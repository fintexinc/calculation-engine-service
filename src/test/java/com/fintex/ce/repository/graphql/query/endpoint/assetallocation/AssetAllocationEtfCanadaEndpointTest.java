package com.fintex.ce.repository.graphql.query.endpoint.assetallocation;

import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.DataProvider;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.RAssetAllocation;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssetAllocationEtfCanadaEndpointTest {

    @Test
    void queryDefinition_verify() {
        //SETUP
        final AssetAllocationEtfCanadaEndpoint m = mock(AssetAllocationEtfCanadaEndpoint.class);

        final QueryQuery qq = mock(QueryQuery.class);

        final String tickets = "TICKETS";
        final List<String> equityIdentifiers = List.of(tickets);

        doCallRealMethod().when(m).queryDefinition(any(), any());
        //ACT
        final QueryQueryDefinition actual = m.queryDefinition(equityIdentifiers, mock(UnaryOperator.class));
        actual.define(qq);

        //VERIFY
        verify(qq).getCanadaEtfsByTickers(eq(equityIdentifiers), any());
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final AssetAllocationEtfCanadaEndpoint m = mock(AssetAllocationEtfCanadaEndpoint.class);

        final EtfQuery etfQuery = mock(EtfQuery.class);
        when(etfQuery.assetAllocation(any())).thenReturn(etfQuery);
        when(etfQuery.ticker(any())).thenReturn(etfQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final EtfQuery actual = m.requestMapper(etfQuery);

        //VERIFY
        verify(actual).assetAllocation(any());
        verify(actual).ticker(any());
    }

    @Test
    void responseMapper_verifyAssetAllocation() {
        try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
            //SETUP
            final var sut = mock(AssetAllocationEtfCanadaEndpoint.class);

            final Etf etf = mock(Etf.class);
            final AssetAllocation assetAllocation = mock(AssetAllocation.class);
            when(etf.getAssetAllocation()).thenReturn(assetAllocation);

            final EtfHolding etfHolding = mock(EtfHolding.class);
            final HoldingType cmd = HoldingType.CANADA_ETF;
            when(etfHolding.getType()).thenReturn(cmd);

            doCallRealMethod().when(sut).responseMapper(any(), any());
            //ACT
            sut.responseMapper(etf, etfHolding);

            //VERIFY
            mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.assetAllocation(assetAllocation, cmd));
        }
    }

    @Test
    void responseMapper_checkResult() {
        //SETUP
        final var sut = mock(AssetAllocationEtfCanadaEndpoint.class);

        final Etf etf = mock(Etf.class);
        final AssetAllocation assetAllocation = mock(AssetAllocation.class);
        when(etf.getAssetAllocation()).thenReturn(assetAllocation);

        when(assetAllocation.getDataProvider()).thenReturn(DataProvider.EAGLE);

        final EtfHolding etfHolding = mock(EtfHolding.class);
        final HoldingType cmd = HoldingType.CANADA_ETF;
        when(etfHolding.getType()).thenReturn(cmd);

        final RAssetAllocation expected = new RAssetAllocation(HoldingType.CANADA_ETF, Map.of());
        expected.setProvider(DataProvider.EAGLE.name());
        doCallRealMethod().when(sut).responseMapper(any(), any());
        //ACT
        sut.responseMapper(etf, etfHolding);
        RAssetAllocation actual = GraphQlMapperUtils.assetAllocation(assetAllocation, cmd);
        //VERIFY
        assertEquals(expected, actual);
    }

}