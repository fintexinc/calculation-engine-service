package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.equitysector.REquitySector;
import com.fintex.ce.model.redis.equitysector.REquitySectorStock;
import com.fintex.ce.repository.graphql.query.endpoint.equitysector.EquitySectorBenchmarkEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitysector.EquitySectorCanadaHedgeFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitysector.EquitySectorCanadaPooledFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitysector.EquitySectorCanadaUsMutualFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitysector.EquitySectorEtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitysector.EquitySectorEtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitysector.EquitySectorFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.equitysector.EquitySectorStockEndpoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class EquitySectorFDSRepositoryTest {

    @Test
    void queryBenchOfFundCanada_verifyDoQuery() {
        //SETUP
        final var graphqlTransport = mock(GraphqlTransportComponent.class);
        final EquitySectorFDSRepository m = mock(EquitySectorFDSRepository.class,
                withSettings().useConstructor(graphqlTransport));
        final List<FundSeriesHolding> holdings = List.of();
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryBenchOfFundCanada(any(), anyList());
        //ACT
        m.queryBenchOfFundCanada(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == EquitySectorFundCanadaEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfFundCanada_checkResult() {
        //SETUP
        final EquitySectorFDSRepository m = mock(EquitySectorFDSRepository.class);
        final List<FundSeriesHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);
        doCallRealMethod().when(m).queryBenchOfFundCanada(any(), anyList());
        //ACT
        final Map<FundSeriesHolding, REquitySector> actual = m.queryBenchOfFundCanada(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryBenchOfOfEtfUs_verifyDoQuery() {
        //SETUP
        final EquitySectorFDSRepository m = mock(EquitySectorFDSRepository.class);
        final List<EtfHolding> holdings = List.of();
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryBenchOfOfEtfUs(any(), anyList());
        //ACT
        m.queryBenchOfOfEtfUs(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == EquitySectorEtfUsEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfOfEtfUs_checkResult() {
        //SETUP
        final EquitySectorFDSRepository m = mock(EquitySectorFDSRepository.class);
        final List<EtfHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);
        doCallRealMethod().when(m).queryBenchOfOfEtfUs(any(), anyList());
        //ACT
        final Map<EtfHolding, REquitySector> actual = m.queryBenchOfOfEtfUs(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryBenchOfEtfCanada_verifyDoQuery() {
        //SETUP
        final EquitySectorFDSRepository m = mock(EquitySectorFDSRepository.class);
        final List<EtfHolding> holdings = List.of();
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryBenchOfEtfCanada(any(), anyList());
        //ACT
        m.queryBenchOfEtfCanada(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == EquitySectorEtfCanadaEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfEtfCanada_checkResult() {
        //SETUP
        final EquitySectorFDSRepository m = mock(EquitySectorFDSRepository.class);
        final List<EtfHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryBenchOfEtfCanada(any(), anyList());
        //ACT
        final Map<EtfHolding, REquitySector> actual = m.queryBenchOfEtfCanada(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryBenchOfStock_verifyDoQuery() {
        //SETUP
        final EquitySectorFDSRepository m = mock(EquitySectorFDSRepository.class);
        final List<StockHolding> holdings = List.of(mock(StockHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryBenchOfStock(any(), anyList());
        //ACT
        m.queryBenchOfStock(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == EquitySectorStockEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfStock_checkResult() {
        //SETUP
        final EquitySectorFDSRepository m = mock(EquitySectorFDSRepository.class);
        final List<StockHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryBenchOfStock(any(), anyList());
        //ACT
        final Map<StockHolding, REquitySectorStock> actual = m.queryBenchOfStock(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryBenchOfBenchmarks_verifyDoQuery() {
        //SETUP
        final EquitySectorFDSRepository m = mock(EquitySectorFDSRepository.class);
        final List<BenchmarkIndexHolding> holdings = List.of(mock(BenchmarkIndexHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryBenchOfBenchmarks(any(), anyList());
        //ACT
        m.queryBenchOfBenchmarks(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == EquitySectorBenchmarkEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfBenchmarks_checkResult() {
        //SETUP
        final EquitySectorFDSRepository m = mock(EquitySectorFDSRepository.class);
        final List<BenchmarkIndexHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryBenchOfBenchmarks(any(), anyList());
        //ACT
        final Map<BenchmarkIndexHolding, REquitySector> actual = m.queryBenchOfBenchmarks(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryUsMutualFunds_verifyDoQuery() {
        //SETUP
        final EquitySectorFDSRepository m = mock(EquitySectorFDSRepository.class);
        final List<UsMutualFundHolding> holdings = List.of(mock(UsMutualFundHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryUsMutualFunds(any(), anyList());
        //ACT
        m.queryUsMutualFunds(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == EquitySectorCanadaUsMutualFundEndpoint.class),
                eq(providers));
    }

    @Test
    void queryCanadaHedgeFunds_verifyDoQuery() {
        //SETUP
        final EquitySectorFDSRepository m = mock(EquitySectorFDSRepository.class);
        final List<CanadaHedgeFundHolding> holdings = List.of(mock(CanadaHedgeFundHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
        //ACT
        m.queryCanadaHedgeFunds(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == EquitySectorCanadaHedgeFundEndpoint.class),
                eq(providers));
    }

    @Test
    void queryCanadaHedgeFunds_checkResult() {
        //SETUP
        final EquitySectorFDSRepository m = mock(EquitySectorFDSRepository.class);
        final List<CanadaHedgeFundHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
        //ACT
        final Map<CanadaHedgeFundHolding, REquitySector> actual = m.queryCanadaHedgeFunds(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryCanadaPooledFunds_verifyDoQuery() {
        //SETUP
        final EquitySectorFDSRepository m = mock(EquitySectorFDSRepository.class);
        final List<CanadaPooledFundHolding> holdings = List.of(mock(CanadaPooledFundHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
        //ACT
        m.queryCanadaPooledFunds(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == EquitySectorCanadaPooledFundEndpoint.class),
                eq(providers));
    }

    @Test
    void queryCanadaPooledFunds_checkResult() {
        //SETUP
        final EquitySectorFDSRepository m = mock(EquitySectorFDSRepository.class);
        final List<CanadaPooledFundHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
        //ACT
        final Map<CanadaPooledFundHolding, REquitySector> actual = m.queryCanadaPooledFunds(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

}