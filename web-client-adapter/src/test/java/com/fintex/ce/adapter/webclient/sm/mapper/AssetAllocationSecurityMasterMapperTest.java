package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.AssetAllocation;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.value.NameValue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssetAllocationSecurityMasterMapperTest {

  private final AssetAllocationSecurityMasterMapper mapper = new AssetAllocationSecurityMasterMapper();

  @Test
  void shouldMapAllocationsAndProvider_whenResponseHasValues() {
    var smsResponse = new AssetAllocation();
    smsResponse.setAllocation(List.of(
        new NameValue("EQUITY", new BigDecimal("60.5")),
        new NameValue("FIXED_INCOME", new BigDecimal("30.0")),
        new NameValue("CASH", new BigDecimal("9.5"))));
    smsResponse.setDataProvider(DataProvider.MORNINGSTAR);

    HoldingAssetAllocation result = mapper.map(smsResponse, createHolding("SEC-001"));

    assertThat(result.getHoldingType()).isEqualTo(FinancialInstrumentType.ETF_CANADA);
    assertThat(result.getAllocations()).hasSize(3);
    assertThat(result.getAllocations()).containsEntry("EQUITY", new BigDecimal("60.5"));
    assertThat(result.getAllocations()).containsEntry("FIXED_INCOME", new BigDecimal("30.0"));
    assertThat(result.getAllocations()).containsEntry("CASH", new BigDecimal("9.5"));
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
  }

  @Test
  void shouldReturnEmptyAllocationsAndProviders_whenResponseIsNull() {
    HoldingAssetAllocation result = mapper.map(null, createHolding("SEC-002"));

    assertThat(result.getAllocations()).isEmpty();
    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldKeepProvidersEmpty_whenDataProviderIsNull() {
    var smsResponse = new AssetAllocation();
    smsResponse.setAllocation(List.of(new NameValue("EQUITY", BigDecimal.ONE)));
    smsResponse.setDataProvider(null);

    HoldingAssetAllocation result = mapper.map(smsResponse, createHolding("SEC-003"));

    assertThat(result.getAllocations()).containsEntry("EQUITY", BigDecimal.ONE);
    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldThrowException_whenResponseContainsDuplicateAllocationKeys() {
    var smsResponse = new AssetAllocation();
    smsResponse.setAllocation(List.of(
        new NameValue("EQUITY", new BigDecimal("10.0")),
        new NameValue("EQUITY", new BigDecimal("20.0"))));

    assertThatThrownBy(() -> mapper.map(smsResponse, createHolding("SEC-004")))
        .isInstanceOf(IllegalStateException.class);
  }

  private PortfolioHolding createHolding(String securityId) {
    return new PortfolioHolding(BigDecimal.ONE, FinancialInstrumentType.ETF_CANADA,
        new SecurityIdentifier(securityId, FiIdentifierType.TICKER));
  }
}
