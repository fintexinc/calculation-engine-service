package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RMaturityAllocation;
import com.fintex.ce.repository.graphql.query.endpoint.maturityallocation.MaturityAllocationCanadaHedgeFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.maturityallocation.MaturityAllocationCanadaUsMutualFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.maturityallocation.MaturityAllocationEtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.maturityallocation.MaturityAllocationEtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.maturityallocation.MaturityAllocationFixedIncomeEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.maturityallocation.MaturityAllocationFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.maturityallocation.MaturityAllocationPooledFundEndpoint;
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

class MaturityAllocationFDSRepositoryTest {

    @Test
    void queryBenchOfFundCanada_verifyDoQuery() {
        //SETUP
        final var graphqlTransport = mock(GraphqlTransportComponent.class);
        final MaturityAllocationFDSRepository m = mock(MaturityAllocationFDSRepository.class,
                withSettings().useConstructor(graphqlTransport));
        final List<FundSeriesHolding> holdings = List.of();
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryBenchOfFundCanada(any(), anyList());
        //ACT
        m.queryBenchOfFundCanada(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == MaturityAllocationFundCanadaEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfFundCanada_checkResult() {
        //SETUP
        final MaturityAllocationFDSRepository m = mock(MaturityAllocationFDSRepository.class);
        final List<FundSeriesHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);
        doCallRealMethod().when(m).queryBenchOfFundCanada(any(), anyList());
        //ACT
        final Map<FundSeriesHolding, RMaturityAllocation> actual = m.queryBenchOfFundCanada(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryBenchOfOfEtfUs_verifyDoQuery() {
        //SETUP
        final MaturityAllocationFDSRepository m = mock(MaturityAllocationFDSRepository.class);
        final List<EtfHolding> holdings = List.of();
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryBenchOfOfEtfUs(any(), anyList());
        //ACT
        m.queryBenchOfOfEtfUs(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == MaturityAllocationEtfUsEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfOfEtfUs_checkResult() {
        //SETUP
        final MaturityAllocationFDSRepository m = mock(MaturityAllocationFDSRepository.class);
        final List<EtfHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);
        doCallRealMethod().when(m).queryBenchOfOfEtfUs(any(), anyList());
        //ACT
        final Map<EtfHolding, RMaturityAllocation> actual = m.queryBenchOfOfEtfUs(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryBenchOfEtfCanada_verifyDoQuery() {
        //SETUP
        final MaturityAllocationFDSRepository m = mock(MaturityAllocationFDSRepository.class);
        final List<EtfHolding> holdings = List.of();
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryBenchOfEtfCanada(any(), anyList());
        //ACT
        m.queryBenchOfEtfCanada(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == MaturityAllocationEtfCanadaEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfEtfCanada_checkResult() {
        //SETUP
        final MaturityAllocationFDSRepository m = mock(MaturityAllocationFDSRepository.class);
        final List<EtfHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryBenchOfEtfCanada(any(), anyList());
        //ACT
        final Map<EtfHolding, RMaturityAllocation> actual = m.queryBenchOfEtfCanada(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryUsMutualFunds_verifyDoQuery() {
        //SETUP
        final MaturityAllocationFDSRepository m = mock(MaturityAllocationFDSRepository.class);
        final List<UsMutualFundHolding> holdings = List.of(mock(UsMutualFundHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryUsMutualFunds(any(), anyList());
        //ACT
        m.queryUsMutualFunds(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == MaturityAllocationCanadaUsMutualFundEndpoint.class),
                eq(providers));
    }

    @Test
    void queryCanadaHedgeFunds_verifyDoQuery() {
        //SETUP
        final MaturityAllocationFDSRepository m = mock(MaturityAllocationFDSRepository.class);
        final List<CanadaHedgeFundHolding> holdings = List.of(mock(CanadaHedgeFundHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
        //ACT
        m.queryCanadaHedgeFunds(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == MaturityAllocationCanadaHedgeFundEndpoint.class),
                eq(providers));
    }

    @Test
    void queryCanadaHedgeFunds_checkResult() {
        //SETUP
        final MaturityAllocationFDSRepository m = mock(MaturityAllocationFDSRepository.class);
        final List<CanadaHedgeFundHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
        //ACT
        final Map<CanadaHedgeFundHolding, RMaturityAllocation> actual = m.queryCanadaHedgeFunds(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryCanadaPooledFunds_verifyDoQuery() {
        //SETUP
        final MaturityAllocationFDSRepository m = mock(MaturityAllocationFDSRepository.class);
        final List<CanadaPooledFundHolding> holdings = List.of(mock(CanadaPooledFundHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
        //ACT
        m.queryCanadaPooledFunds(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == MaturityAllocationPooledFundEndpoint.class),
                eq(providers));
    }

    @Test
    void queryCanadaPooledFunds_checkResult() {
        //SETUP
        final MaturityAllocationFDSRepository m = mock(MaturityAllocationFDSRepository.class);
        final List<CanadaPooledFundHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
        //ACT
        final Map<CanadaPooledFundHolding, RMaturityAllocation> actual = m.queryCanadaPooledFunds(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryFixedIncomes_verifyDoQuery() {
        //SETUP
        final MaturityAllocationFDSRepository m = mock(MaturityAllocationFDSRepository.class);
        final List<FixedIncomeHolding> holdings = List.of(mock(FixedIncomeHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.BROADRIDGE);

        doCallRealMethod().when(m).queryBenchOfFixedIncomes(any(), anyList());
        //ACT
        m.queryBenchOfFixedIncomes(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == MaturityAllocationFixedIncomeEndpoint.class),
                eq(providers));
    }

    @Test
    void queryFixedIncomes_checkResult() {
        //SETUP
        final MaturityAllocationFDSRepository m = mock(MaturityAllocationFDSRepository.class);
        final List<FixedIncomeHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.BROADRIDGE);

        doCallRealMethod().when(m).queryBenchOfFixedIncomes(any(), anyList());
        //ACT
        final Map<FixedIncomeHolding, RMaturityAllocation> actual = m.queryBenchOfFixedIncomes(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }
    
}
