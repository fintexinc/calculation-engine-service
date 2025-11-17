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
import com.fintex.ce.model.redis.RCountryExposure;
import com.fintex.ce.repository.graphql.query.endpoint.countryexposure.CountryExposureBenchmarkEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.countryexposure.CountryExposureCanadaHedgeFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.countryexposure.CountryExposureCanadaPooledFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.countryexposure.CountryExposureEtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.countryexposure.CountryExposureEtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.countryexposure.CountryExposureFixedIncomeEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.countryexposure.CountryExposureFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.countryexposure.CountryExposureUsMutualFundEndpoint;
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

class CountryExposureFDSRepositoryTest {

    @Test
    void queryBenchOfFundCanada_verifyDoQuery() {
        //SETUP
        final var graphqlTransport = mock(GraphqlTransportComponent.class);
        final CountryExposureFDSRepository m = mock(CountryExposureFDSRepository.class,
                withSettings().useConstructor(graphqlTransport));
        final List<FundSeriesHolding> holdings = List.of();
        final List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);

        doCallRealMethod().when(m).queryBenchOfFundCanada(any(), anyList());
        //ACT
        m.queryBenchOfFundCanada(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == CountryExposureFundCanadaEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfFundCanada_checkResult() {
        //SETUP
        final CountryExposureFDSRepository m = mock(CountryExposureFDSRepository.class);
        final List<FundSeriesHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);
        doCallRealMethod().when(m).queryBenchOfFundCanada(any(), anyList());
        //ACT
        final Map<FundSeriesHolding, RCountryExposure> actual = m.queryBenchOfFundCanada(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryBenchOfOfEtfUs_verifyDoQuery() {
        //SETUP
        final CountryExposureFDSRepository m = mock(CountryExposureFDSRepository.class);
        final List<EtfHolding> holdings = List.of();
        final List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);

        doCallRealMethod().when(m).queryBenchOfOfEtfUs(any(), anyList());
        //ACT
        m.queryBenchOfOfEtfUs(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == CountryExposureEtfUsEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfOfEtfUs_checkResult() {
        //SETUP
        final CountryExposureFDSRepository m = mock(CountryExposureFDSRepository.class);
        final List<EtfHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);
        doCallRealMethod().when(m).queryBenchOfOfEtfUs(any(), anyList());
        //ACT
        final Map<EtfHolding, RCountryExposure> actual = m.queryBenchOfOfEtfUs(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryBenchOfEtfCanada_verifyDoQuery() {
        //SETUP
        final CountryExposureFDSRepository m = mock(CountryExposureFDSRepository.class);
        final List<EtfHolding> holdings = List.of();
        final List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);

        doCallRealMethod().when(m).queryBenchOfEtfCanada(any(), anyList());
        //ACT
        m.queryBenchOfEtfCanada(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == CountryExposureEtfCanadaEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfEtfCanada_checkResult() {
        //SETUP
        final CountryExposureFDSRepository m = mock(CountryExposureFDSRepository.class);
        final List<EtfHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);

        doCallRealMethod().when(m).queryBenchOfEtfCanada(any(), anyList());
        //ACT
        final Map<EtfHolding, RCountryExposure> actual = m.queryBenchOfEtfCanada(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryBenchOfBenchmarks_verifyDoQuery() {
        //SETUP
        final CountryExposureFDSRepository m = mock(CountryExposureFDSRepository.class);
        final List<BenchmarkIndexHolding> holdings = List.of();
        final List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);

        doCallRealMethod().when(m).queryBenchOfBenchmarks(any(), anyList());
        //ACT
        m.queryBenchOfBenchmarks(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == CountryExposureBenchmarkEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfBenchmarks_checkResult() {
        //SETUP
        final CountryExposureFDSRepository m = mock(CountryExposureFDSRepository.class);
        final List<BenchmarkIndexHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.MORNINGSTAR);

        doCallRealMethod().when(m).queryBenchOfBenchmarks(any(), anyList());
        //ACT
        final Map<BenchmarkIndexHolding, RCountryExposure> actual = m.queryBenchOfBenchmarks(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryUsMutualFunds_verifyDoQuery() {
        //SETUP
        final CountryExposureFDSRepository m = mock(CountryExposureFDSRepository.class);
        final List<UsMutualFundHolding> holdings = List.of(mock(UsMutualFundHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryUsMutualFunds(any(), anyList());
        //ACT
        m.queryUsMutualFunds(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == CountryExposureUsMutualFundEndpoint.class),
                eq(providers));
    }

    @Test
    void queryCanadaHedgeFunds_verifyDoQuery() {
        //SETUP
        final CountryExposureFDSRepository m = mock(CountryExposureFDSRepository.class);
        final List<CanadaHedgeFundHolding> holdings = List.of(mock(CanadaHedgeFundHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
        //ACT
        m.queryCanadaHedgeFunds(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == CountryExposureCanadaHedgeFundEndpoint.class),
                eq(providers));
    }

    @Test
    void queryCanadaHedgeFunds_checkResult() {
        //SETUP
        final CountryExposureFDSRepository m = mock(CountryExposureFDSRepository.class);
        final List<CanadaHedgeFundHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
        //ACT
        final Map<CanadaHedgeFundHolding, RCountryExposure> actual = m.queryCanadaHedgeFunds(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryCanadaPooledFunds_verifyDoQuery() {
        //SETUP
        final CountryExposureFDSRepository m = mock(CountryExposureFDSRepository.class);
        final List<CanadaPooledFundHolding> holdings = List.of(mock(CanadaPooledFundHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
        //ACT
        m.queryCanadaPooledFunds(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == CountryExposureCanadaPooledFundEndpoint.class),
                eq(providers));
    }

    @Test
    void queryCanadaPooledFunds_checkResult() {
        //SETUP
        final CountryExposureFDSRepository m = mock(CountryExposureFDSRepository.class);
        final List<CanadaPooledFundHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
        //ACT
        final Map<CanadaPooledFundHolding, RCountryExposure> actual = m.queryCanadaPooledFunds(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryFixedIncome_verifyDoQuery() {
        //SETUP
        final CountryExposureFDSRepository m = mock(CountryExposureFDSRepository.class);
        final List<FixedIncomeHolding> holdings = List.of(mock(FixedIncomeHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.BROADRIDGE);
        doCallRealMethod().when(m).queryBenchOfFixedIncomes(any(), anyList());

        //ACT
        m.queryBenchOfFixedIncomes(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == CountryExposureFixedIncomeEndpoint.class),
                eq(providers));
    }

    @Test
    void queryFixedIncome_checkResult() {
        //SETUP
        final CountryExposureFDSRepository m = mock(CountryExposureFDSRepository.class);
        final List<FixedIncomeHolding> holdings = List.of();
        final HashMap<Object, Object> expected = new HashMap<>();
        final List<DataProvider> providers = List.of(DataProvider.BROADRIDGE);
        when(m.doQuery(any(), any(), any())).thenReturn(expected);
        doCallRealMethod().when(m).queryBenchOfFixedIncomes(any(), anyList());

        //ACT
        final Map<FixedIncomeHolding, RCountryExposure> actual = m.queryBenchOfFixedIncomes(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }
}