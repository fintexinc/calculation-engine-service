package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RIncomeForecast;
import com.fintex.ce.repository.graphql.query.endpoint.incomeforecast.IncomeForecastCanadaHedgeFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.incomeforecast.IncomeForecastCanadaUsMutualFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.incomeforecast.IncomeForecastEtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.incomeforecast.IncomeForecastEtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.incomeforecast.IncomeForecastFixedIncomeEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.incomeforecast.IncomeForecastFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.incomeforecast.IncomeForecastPooledFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.incomeforecast.IncomeForecastStockEndpoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class IncomeForecastFDSRepositoryTest {

    @Test
    void queryBenchOfFundCanada_verifyDoQuery() {
        //SETUP
        final var graphqlTransport = mock(GraphqlTransportComponent.class);
        final var incomeForecastFDSRepository = mock(IncomeForecastFDSRepository.class,
                withSettings().useConstructor(graphqlTransport));

        final var holdings = mock(List.class);
        final var provider = mock(List.class);

        doCallRealMethod().when(incomeForecastFDSRepository).queryBenchOfFundCanada(any(), any());

        //ACT
        final Map map = incomeForecastFDSRepository.queryBenchOfFundCanada(holdings, provider);

        //VERIFY
        verify(incomeForecastFDSRepository).doQuery(same(holdings),
                argThat(arg -> arg.getClass() == IncomeForecastFundCanadaEndpoint.class), same(provider));
    }

    @Test
    void queryBenchOfEtfCanada() {
        //SETUP
        final var graphqlTransport = mock(GraphqlTransportComponent.class);
        final var incomeForecastFDSRepository = mock(IncomeForecastFDSRepository.class,
                withSettings().useConstructor(graphqlTransport));

        final var holdings = mock(List.class);
        final var provider = mock(List.class);

        doCallRealMethod().when(incomeForecastFDSRepository).queryBenchOfEtfCanada(any(), any());

        //ACT
        final Map map = incomeForecastFDSRepository.queryBenchOfEtfCanada(holdings, provider);

        //VERIFY
        verify(incomeForecastFDSRepository).doQuery(same(holdings),
                argThat(arg -> arg.getClass() == IncomeForecastEtfCanadaEndpoint.class), same(provider));
    }

    @Test
    void queryBenchOfOfEtfUs() {
        //SETUP
        final var graphqlTransport = mock(GraphqlTransportComponent.class);
        final var incomeForecastFDSRepository = mock(IncomeForecastFDSRepository.class,
                withSettings().useConstructor(graphqlTransport));

        final var holdings = mock(List.class);
        final var provider = mock(List.class);

        doCallRealMethod().when(incomeForecastFDSRepository).queryBenchOfOfEtfUs(any(), any());

        //ACT
        final Map map = incomeForecastFDSRepository.queryBenchOfOfEtfUs(holdings, provider);

        //VERIFY
        verify(incomeForecastFDSRepository).doQuery(same(holdings),
                argThat(arg -> arg.getClass() == IncomeForecastEtfUsEndpoint.class), same(provider));
    }

    @Test
    void queryBenchOfFixedIncomes_verifyDoQuery() {
        //SETUP
        final IncomeForecastFDSRepository m = mock(IncomeForecastFDSRepository.class);
        final List<FixedIncomeHolding> holdings = List.of();
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryBenchOfFixedIncomes(any(), anyList());
        //ACT
        m.queryBenchOfFixedIncomes(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == IncomeForecastFixedIncomeEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfFixedIncomes_checkResult() {
        //SETUP
        final IncomeForecastFDSRepository m = mock(IncomeForecastFDSRepository.class);
        final List<FixedIncomeHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryBenchOfFixedIncomes(any(), anyList());
        //ACT
        final Map<FixedIncomeHolding, RIncomeForecast> actual = m.queryBenchOfFixedIncomes(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryBenchOfStock_verifyDoQuery() {
        //SETUP
        final IncomeForecastFDSRepository m = mock(IncomeForecastFDSRepository.class);
        final List<StockHolding> holdings = List.of(mock(StockHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryBenchOfStock(any(), anyList());
        //ACT
        m.queryBenchOfStock(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == IncomeForecastStockEndpoint.class),
                eq(providers));
    }

    @Test
    void queryBenchOfStock_checkResult() {
        //SETUP
        final IncomeForecastFDSRepository m = mock(IncomeForecastFDSRepository.class);
        final List<StockHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryBenchOfStock(any(), anyList());
        //ACT
        final Map<StockHolding, RIncomeForecast> actual = m.queryBenchOfStock(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryCanadaHedgeFunds_verifyDoQuery() {
        //SETUP
        final IncomeForecastFDSRepository m = mock(IncomeForecastFDSRepository.class);
        final List<CanadaHedgeFundHolding> holdings = List.of(mock(CanadaHedgeFundHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
        //ACT
        m.queryCanadaHedgeFunds(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == IncomeForecastCanadaHedgeFundEndpoint.class),
                eq(providers));
    }

    @Test
    void queryCanadaHedgeFunds_checkResult() {
        //SETUP
        final IncomeForecastFDSRepository m = mock(IncomeForecastFDSRepository.class);
        final List<CanadaHedgeFundHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
        //ACT
        final Map<CanadaHedgeFundHolding, RIncomeForecast> actual = m.queryCanadaHedgeFunds(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryCanadaPooledFunds_verifyDoQuery() {
        //SETUP
        final IncomeForecastFDSRepository m = mock(IncomeForecastFDSRepository.class);
        final List<CanadaPooledFundHolding> holdings = List.of(mock(CanadaPooledFundHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
        //ACT
        m.queryCanadaPooledFunds(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == IncomeForecastPooledFundEndpoint.class),
                eq(providers));
    }

    @Test
    void queryCanadaPooledFunds_checkResult() {
        //SETUP
        final IncomeForecastFDSRepository m = mock(IncomeForecastFDSRepository.class);
        final List<CanadaPooledFundHolding> holdings = List.of();

        final HashMap<Object, Object> expected = new HashMap<>();
        when(m.doQuery(any(), any(), any())).thenReturn(expected);

        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryCanadaPooledFunds(any(), anyList());
        //ACT
        final Map<CanadaPooledFundHolding, RIncomeForecast> actual = m.queryCanadaPooledFunds(holdings, providers);

        //VERIFY
        Assertions.assertSame(expected, actual);
    }

    @Test
    void queryUsMutualFunds_verifyDoQuery() {
        //SETUP
        final IncomeForecastFDSRepository m = mock(IncomeForecastFDSRepository.class);
        final List<UsMutualFundHolding> holdings = List.of(mock(UsMutualFundHolding.class));
        final List<DataProvider> providers = List.of(DataProvider.EAGLE);

        doCallRealMethod().when(m).queryUsMutualFunds(any(), anyList());
        //ACT
        m.queryUsMutualFunds(holdings, providers);

        //VERIFY
        verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == IncomeForecastCanadaUsMutualFundEndpoint.class),
                eq(providers));
    }

}
