package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RFixedIncomeBondSecurities;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector.FixedIncomeBondSectorBenchmarkEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector.FixedIncomeBondSectorCanadaHedgeFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector.FixedIncomeBondSectorCanadaPooledFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector.FixedIncomeBondSectorEtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector.FixedIncomeBondSectorEtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector.FixedIncomeBondSectorFixedIncomeEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector.FixedIncomeBondSectorFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.fixedincomebondsector.FixedIncomeBondSectorUsMutualFundEndpoint;
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

class FixedIncomeBondSectorFDSRepositoryTest {

    @Test
    void queryBenchOfFundCanada_verifyDoQuery() {
        //SETUP
        final var graphqlTransport = mock(GraphqlTransportComponent.class);
        final FixedIncomeBondSectorFDSRepository fixedIncomeBondSectorFDSRepository = mock(FixedIncomeBondSectorFDSRepository.class,
                withSettings().useConstructor(graphqlTransport));
        final List<FundSeriesHolding> holdings = List.of();
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(fixedIncomeBondSectorFDSRepository).queryBenchOfFundCanada(any(), anyList());
        //ACT
        fixedIncomeBondSectorFDSRepository.queryBenchOfFundCanada(holdings, providers);

        //VERIFY
        verify(fixedIncomeBondSectorFDSRepository).doQuery(eq(holdings), argThat(argument -> argument.getClass() == FixedIncomeBondSectorFundCanadaEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfFundCanada_checkResult() {
        //SETUP
        final FixedIncomeBondSectorFDSRepository fixedIncomeBondSectorFDSRepository = mock(FixedIncomeBondSectorFDSRepository.class);
        final List<FundSeriesHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(fixedIncomeBondSectorFDSRepository.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);
        doCallRealMethod().when(fixedIncomeBondSectorFDSRepository).queryBenchOfFundCanada(any(), anyList());
        //ACT
        final Map<FundSeriesHolding, RFixedIncomeBondSecurities> actual = fixedIncomeBondSectorFDSRepository.queryBenchOfFundCanada(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryBenchOfOfEtfUs_verifyDoQuery() {
        //SETUP
        final FixedIncomeBondSectorFDSRepository fixedIncomeBondSectorFDSRepository = mock(FixedIncomeBondSectorFDSRepository.class);
        final List<EtfHolding> holdings = List.of();
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(fixedIncomeBondSectorFDSRepository).queryBenchOfOfEtfUs(any(), anyList());
        //ACT
        fixedIncomeBondSectorFDSRepository.queryBenchOfOfEtfUs(holdings, providers);

        //VERIFY
        verify(fixedIncomeBondSectorFDSRepository).doQuery(eq(holdings), argThat(argument -> argument.getClass() == FixedIncomeBondSectorEtfUsEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfOfEtfUs_checkResult() {
        //SETUP
        final FixedIncomeBondSectorFDSRepository fixedIncomeBondSectorFDSRepository = mock(FixedIncomeBondSectorFDSRepository.class);
        final List<EtfHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(fixedIncomeBondSectorFDSRepository.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);
        doCallRealMethod().when(fixedIncomeBondSectorFDSRepository).queryBenchOfOfEtfUs(any(), anyList());
        //ACT
        final Map<EtfHolding, RFixedIncomeBondSecurities> actual = fixedIncomeBondSectorFDSRepository.queryBenchOfOfEtfUs(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryBenchOfEtfCanada_verifyDoQuery() {
        //SETUP
        final FixedIncomeBondSectorFDSRepository fixedIncomeBondSectorFDSRepository = mock(FixedIncomeBondSectorFDSRepository.class);
        final List<EtfHolding> holdings = List.of();
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(fixedIncomeBondSectorFDSRepository).queryBenchOfEtfCanada(any(), anyList());
        //ACT
        fixedIncomeBondSectorFDSRepository.queryBenchOfEtfCanada(holdings, providers);

        //VERIFY
        verify(fixedIncomeBondSectorFDSRepository).doQuery(eq(holdings), argThat(argument -> argument.getClass() == FixedIncomeBondSectorEtfCanadaEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfEtfCanada_checkResult() {
        //SETUP
        final FixedIncomeBondSectorFDSRepository fixedIncomeBondSectorFDSRepository = mock(FixedIncomeBondSectorFDSRepository.class);
        final List<EtfHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(fixedIncomeBondSectorFDSRepository.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(fixedIncomeBondSectorFDSRepository).queryBenchOfEtfCanada(any(), anyList());
        //ACT
        final Map<EtfHolding, RFixedIncomeBondSecurities> actual = fixedIncomeBondSectorFDSRepository.queryBenchOfEtfCanada(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryBenchOfBenchmarks_verifyDoQuery() {
        //SETUP
        final FixedIncomeBondSectorFDSRepository fixedIncomeBondSectorFDSRepository = mock(FixedIncomeBondSectorFDSRepository.class);
        final List<BenchmarkIndexHolding> holdings = List.of();
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(fixedIncomeBondSectorFDSRepository).queryBenchOfBenchmarks(any(), anyList());
        //ACT
        fixedIncomeBondSectorFDSRepository.queryBenchOfBenchmarks(holdings, providers);

        //VERIFY
        verify(fixedIncomeBondSectorFDSRepository).doQuery(eq(holdings), argThat(argument -> argument.getClass() == FixedIncomeBondSectorBenchmarkEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfBenchmarks_checkResult() {
        //SETUP
        final FixedIncomeBondSectorFDSRepository fixedIncomeBondSectorFDSRepository = mock(FixedIncomeBondSectorFDSRepository.class);
        final List<BenchmarkIndexHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(fixedIncomeBondSectorFDSRepository.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(fixedIncomeBondSectorFDSRepository).queryBenchOfBenchmarks(any(), anyList());
        //ACT
        final Map<BenchmarkIndexHolding, RFixedIncomeBondSecurities> actual = fixedIncomeBondSectorFDSRepository.queryBenchOfBenchmarks(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryBenchOfFixedIncomes_verifyDoQuery() {
        //SETUP
        final FixedIncomeBondSectorFDSRepository fixedIncomeBondSectorFDSRepository = mock(FixedIncomeBondSectorFDSRepository.class);
        final List<FixedIncomeHolding> holdings = List.of();
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(fixedIncomeBondSectorFDSRepository).queryBenchOfFixedIncomes(any(), anyList());
        //ACT
        fixedIncomeBondSectorFDSRepository.queryBenchOfFixedIncomes(holdings, providers);

        //VERIFY
        verify(fixedIncomeBondSectorFDSRepository).doQuery(eq(holdings), argThat(argument -> argument.getClass() == FixedIncomeBondSectorFixedIncomeEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfFixedIncomes_checkResult() {
        //SETUP
        final FixedIncomeBondSectorFDSRepository fixedIncomeBondSectorFDSRepository = mock(FixedIncomeBondSectorFDSRepository.class);
        final List<FixedIncomeHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(fixedIncomeBondSectorFDSRepository.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(fixedIncomeBondSectorFDSRepository).queryBenchOfFixedIncomes(any(), anyList());
        //ACT
        final Map<FixedIncomeHolding, RFixedIncomeBondSecurities> actual = fixedIncomeBondSectorFDSRepository
                .queryBenchOfFixedIncomes(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryUsMutualFunds_verifyDoQuery() {
        //SETUP
        final FixedIncomeBondSectorFDSRepository m = mock(FixedIncomeBondSectorFDSRepository.class);
        final List<UsMutualFundHolding> holdings = List.of(mock(UsMutualFundHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryUsMutualFunds(any(), anyList());
        //ACT
        m.queryUsMutualFunds(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == FixedIncomeBondSectorUsMutualFundEndpoint.class),
                eq(providers));
    }

    @Test
    void queryCanadaHedgeFunds_verifyDoQuery() {
        //SETUP
        final FixedIncomeBondSectorFDSRepository m = mock(FixedIncomeBondSectorFDSRepository.class);
        final List<CanadaHedgeFundHolding> holdings = List.of(mock(CanadaHedgeFundHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
        //ACT
        m.queryCanadaHedgeFunds(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == FixedIncomeBondSectorCanadaHedgeFundEndpoint.class),
                eq(providers));
    }

    @Test
    void queryCanadaHedgeFunds_checkResult() {
        //SETUP
        final FixedIncomeBondSectorFDSRepository m = mock(FixedIncomeBondSectorFDSRepository.class);
        final List<CanadaHedgeFundHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
        //ACT
        final Map<CanadaHedgeFundHolding, RFixedIncomeBondSecurities> actual = m.queryCanadaHedgeFunds(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryCanadaPooledFunds_verifyDoQuery() {
        //SETUP
        final FixedIncomeBondSectorFDSRepository m = mock(FixedIncomeBondSectorFDSRepository.class);
        final List<CanadaPooledFundHolding> holdings = List.of(mock(CanadaPooledFundHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
        //ACT
        m.queryCanadaPooledFunds(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == FixedIncomeBondSectorCanadaPooledFundEndpoint.class),
                eq(providers));
    }

    @Test
    void queryCanadaPooledFunds_checkResult() {
        //SETUP
        final FixedIncomeBondSectorFDSRepository m = mock(FixedIncomeBondSectorFDSRepository.class);
        final List<CanadaPooledFundHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
        //ACT
        final Map<CanadaPooledFundHolding, RFixedIncomeBondSecurities> actual = m.queryCanadaPooledFunds(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

}
