package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.ce.adapter.graphqlclient.endpoint.managementfee.ManagementFeeCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.managementfee.ManagementFeeEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.managementfee.ManagementFeeEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.managementfee.ManagementFeeFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.managementfee.ManagementFeeUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.ManagementFeeSMRepository;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.ManagementFee;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.smclient.service.GraphqlTransportComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class ManagementFeeSMRepositoryTest {

  @Test
  void queryBenchOfFundCanada_verifyDoQuery() {
    // SETUP
    final var graphqlTransport = mock(GraphqlTransportComponent.class);
    final var managementFeeFDSRepository = mock(ManagementFeeSMRepository.class,
        withSettings().useConstructor(graphqlTransport));

    final var holdings = mock(List.class);
    final var provider = mock(List.class);

    doCallRealMethod().when(managementFeeFDSRepository).queryBenchOfFundCanada(any(), any());

    // ACT
    final Map map = managementFeeFDSRepository.queryBenchOfFundCanada(holdings, provider);

    // VERIFY
    verify(managementFeeFDSRepository).doQuery(same(holdings),
        argThat(arg -> arg.getClass() == ManagementFeeFundCanadaEndpoint.class), same(provider));
  }

  @Test
  void queryBenchOfEtfCanada() {
    // SETUP
    final var graphqlTransport = mock(GraphqlTransportComponent.class);
    final var managementFeeFDSRepository = mock(ManagementFeeSMRepository.class,
        withSettings().useConstructor(graphqlTransport));

    final var holdings = mock(List.class);
    final var provider = mock(List.class);

    doCallRealMethod().when(managementFeeFDSRepository).queryBenchOfEtfCanada(any(), any());

    // ACT
    final Map map = managementFeeFDSRepository.queryBenchOfEtfCanada(holdings, provider);

    // VERIFY
    verify(managementFeeFDSRepository).doQuery(same(holdings),
        argThat(arg -> arg.getClass() == ManagementFeeEtfCanadaEndpoint.class), same(provider));
  }

  @Test
  void queryBenchOfOfEtfUs() {
    // SETUP
    final var graphqlTransport = mock(GraphqlTransportComponent.class);
    final var managementFeeFDSRepository = mock(ManagementFeeSMRepository.class,
        withSettings().useConstructor(graphqlTransport));

    final var holdings = mock(List.class);
    final var provider = mock(List.class);

    doCallRealMethod().when(managementFeeFDSRepository).queryBenchOfOfEtfUs(any(), any());

    // ACT
    final Map map = managementFeeFDSRepository.queryBenchOfOfEtfUs(holdings, provider);

    // VERIFY
    verify(managementFeeFDSRepository).doQuery(same(holdings),
        argThat(arg -> arg.getClass() == ManagementFeeEtfUsEndpoint.class), same(provider));
  }

  @Test
  void queryUsMutualFunds_verifyDoQuery() {
    // SETUP
    final ManagementFeeSMRepository m = mock(ManagementFeeSMRepository.class);
    final List<UsMutualFundHolding> holdings = List.of(mock(UsMutualFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryUsMutualFunds(any(), anyList());
    // ACT
    m.queryUsMutualFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument.getClass() == ManagementFeeUsMutualFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaHedgeFunds_verifyDoQuery() {
    // SETUP
    final ManagementFeeSMRepository m = mock(ManagementFeeSMRepository.class);
    final List<CanadaHedgeFundHolding> holdings = List.of(mock(CanadaHedgeFundHolding.class));
    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
    // ACT
    m.queryCanadaHedgeFunds(holdings, providers);

    // VERIFY
    verify(m).doQuery(eq(holdings), argThat(argument -> argument
        .getClass() == ManagementFeeCanadaHedgeFundEndpoint.class),
        eq(providers));
  }

  @Test
  void queryCanadaHedgeFunds_checkResult() {
    // SETUP
    final ManagementFeeSMRepository m = mock(ManagementFeeSMRepository.class);
    final List<CanadaHedgeFundHolding> holdings = List.of();

    final HashMap<Object, Object> expected = new HashMap<>();
    when(m.doQuery(any(), any(), any())).thenReturn(expected);

    final List<DataProvider> providers = List.of(DataProvider.EAGLE);

    doCallRealMethod().when(m).queryCanadaHedgeFunds(any(), anyList());
    // ACT
    final Map<CanadaHedgeFundHolding, ManagementFee> actual = m.queryCanadaHedgeFunds(holdings, providers);

    // VERIFY
    Assertions.assertSame(expected, actual);
  }

}