package com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation;

import com.fintex.ce.adapter.graphqlclient.endpoint.assetallocation.AssetAllocationFixedIncomeEndpoint;
import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.DataProvider;
import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssetAllocationFixedIncomeEndpointTest {

  @Test
  void requestMapper_verify() {
    // SETUP
    final AssetAllocationFixedIncomeEndpoint m = mock(AssetAllocationFixedIncomeEndpoint.class);

    final FixedIncomeQuery fixedIncomeQuery = mock(FixedIncomeQuery.class);
    when(fixedIncomeQuery.assetAllocation(any())).thenReturn(fixedIncomeQuery);
    when(fixedIncomeQuery.externalIdentifiers(any())).thenReturn(fixedIncomeQuery);

    doCallRealMethod().when(m).requestMapper(any());

    // ACT
    final FixedIncomeQuery result = m.requestMapper(fixedIncomeQuery);

    // VERIFY
    verify(result).assetAllocation(any());
    verify(result).externalIdentifiers(any());
  }

  @Test
  void responseMapper_verifyAssetAllocation() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final AssetAllocationFixedIncomeEndpoint m = mock(AssetAllocationFixedIncomeEndpoint.class);

      final FixedIncome fixedIncome = mock(FixedIncome.class);
      final AssetAllocation assetAllocation = mock(AssetAllocation.class);
      when(fixedIncome.getAssetAllocation()).thenReturn(assetAllocation);

      final FixedIncomeHolding fixedIncomeHolding = mock(FixedIncomeHolding.class);
      final HoldingType holdingType = HoldingType.FIXED_INCOME;
      when(fixedIncomeHolding.getType()).thenReturn(holdingType);

      doCallRealMethod().when(m).responseMapper(any(), any());

      // ACT
      m.responseMapper(fixedIncome, fixedIncomeHolding);

      // VERIFY
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.assetAllocation(assetAllocation, holdingType));
    }
  }

  @Test
  void responseMapper_checkResult() {
    // SETUP
    final AssetAllocationFixedIncomeEndpoint m = mock(AssetAllocationFixedIncomeEndpoint.class);

    final FixedIncome fixedIncome = mock(FixedIncome.class);
    final AssetAllocation assetAllocation = mock(AssetAllocation.class);
    when(fixedIncome.getAssetAllocation()).thenReturn(assetAllocation);

    when(assetAllocation.getDataProvider()).thenReturn(DataProvider.BROADRIDGE);

    final FixedIncomeHolding fixedIncomeHolding = mock(FixedIncomeHolding.class);
    final HoldingType holdingType = HoldingType.FIXED_INCOME;
    when(fixedIncomeHolding.getType()).thenReturn(holdingType);

    final com.fintex.ce.domain.model.AssetAllocation expected = new com.fintex.ce.domain.model.AssetAllocation(
        HoldingType.FIXED_INCOME, Map.of());
    expected.setProvider(DataProvider.BROADRIDGE.name());
    doCallRealMethod().when(m).responseMapper(any(), any());

    // ACT
    m.responseMapper(fixedIncome, fixedIncomeHolding);
    com.fintex.ce.domain.model.AssetAllocation actual = GraphQlMapperUtils.assetAllocation(assetAllocation,
        holdingType);

    // VERIFY
    assertEquals(expected, actual);
  }

}
