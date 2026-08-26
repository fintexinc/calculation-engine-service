package com.fintex.ce.adapter.webclient.mic.mapper;

import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.AssetAllocation;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.domain.allocation.AssetAllocationValue;
import com.fintex.wm.commons.domain.allocation.AssetAllocationWithCurrency;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.TreeSet;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.etfCa;
import static org.assertj.core.api.Assertions.assertThat;

class AssetAllocationMarketInvestmentCatalogueMapperTest {

  private final AssetAllocationMarketInvestmentCatalogueMapper mapper = new AssetAllocationMarketInvestmentCatalogueMapper();

  @Test
  void shouldMapAllocationsCurrencyAndProvider_whenResponseHasValues() {
    AssetAllocationWithCurrency micResponse = withCurrency(List.of(
        new AssetAllocationValue(AssetAllocationRegionType.US_EQUITIES, new BigDecimal("60.5"), new TreeSet<>()),
        new AssetAllocationValue(AssetAllocationRegionType.FIXED_INCOME, new BigDecimal("30.0"), new TreeSet<>()),
        new AssetAllocationValue(AssetAllocationRegionType.CASH, new BigDecimal("9.5"), new TreeSet<>())),
        Currency.USD, DataProvider.MORNINGSTAR);

    HoldingAssetAllocation result = mapper.map(micResponse, etfCa("SEC-001"));

    assertThat(result.getAllocations()).hasSize(3);
    assertThat(result.getAllocations()).containsEntry(AssetAllocationRegionType.US_EQUITIES, new BigDecimal("60.5"));
    assertThat(result.getAllocations()).containsEntry(AssetAllocationRegionType.FIXED_INCOME, new BigDecimal("30.0"));
    assertThat(result.getAllocations()).containsEntry(AssetAllocationRegionType.CASH, new BigDecimal("9.5"));
    assertThat(result.getCurrency()).isEqualTo(Currency.USD);
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
  }

  @Test
  void shouldReturnEmptyAllocationsAndProvidersAndNullCurrency_whenResponseIsNull() {
    HoldingAssetAllocation result = mapper.map(null, etfCa("SEC-002"));

    assertThat(result.getAllocations()).isEmpty();
    assertThat(result.getProviders()).isEmpty();
    assertThat(result.getCurrency()).isNull();
  }

  @Test
  void shouldKeepProvidersEmpty_whenDataProviderIsNull() {
    AssetAllocationWithCurrency micResponse = withCurrency(
        List.of(new AssetAllocationValue(AssetAllocationRegionType.US_EQUITIES, BigDecimal.ONE, new TreeSet<>())),
        null, null);

    HoldingAssetAllocation result = mapper.map(micResponse, etfCa("SEC-003"));

    assertThat(result.getAllocations()).containsEntry(AssetAllocationRegionType.US_EQUITIES, BigDecimal.ONE);
    assertThat(result.getProviders()).isEmpty();
    assertThat(result.getCurrency()).isNull();
  }

  @Test
  void shouldSumDuplicateAllocationKeys() {
    AssetAllocationWithCurrency micResponse = withCurrency(List.of(
        new AssetAllocationValue(AssetAllocationRegionType.US_EQUITIES, new BigDecimal("10.0"), new TreeSet<>()),
        new AssetAllocationValue(AssetAllocationRegionType.US_EQUITIES, new BigDecimal("20.0"), new TreeSet<>())),
        Currency.CAD, null);

    HoldingAssetAllocation result = mapper.map(micResponse, etfCa("SEC-004"));

    assertThat(result.getAllocations()).containsEntry(AssetAllocationRegionType.US_EQUITIES, new BigDecimal("30.0"));
    assertThat(result.getCurrency()).isEqualTo(Currency.CAD);
  }

  @Test
  void shouldSkipNullValuesAndTypes() {
    AssetAllocationWithCurrency micResponse = withCurrency(List.of(
        new AssetAllocationValue(null, BigDecimal.ONE, new TreeSet<>()),
        new AssetAllocationValue(AssetAllocationRegionType.CASH, null, new TreeSet<>()),
        new AssetAllocationValue(AssetAllocationRegionType.FIXED_INCOME, BigDecimal.TEN, new TreeSet<>())),
        null, null);

    HoldingAssetAllocation result = mapper.map(micResponse, etfCa("SEC-005"));

    assertThat(result.getAllocations()).containsOnly(
        org.assertj.core.api.Assertions.entry(AssetAllocationRegionType.FIXED_INCOME, BigDecimal.TEN));
  }

  private static AssetAllocationWithCurrency withCurrency(List<AssetAllocationValue> values, Currency currency,
      DataProvider provider) {
    AssetAllocation allocation = new AssetAllocation();
    allocation.setAllocations(values);
    allocation.setDataProviders(provider == null ? null : List.of(provider));

    AssetAllocationWithCurrency wrapper = new AssetAllocationWithCurrency();
    wrapper.setAssetAllocation(allocation);
    if (currency != null) {
      CurrencyDatapoint dp = new CurrencyDatapoint();
      dp.setValue(currency);
      wrapper.setCurrency(dp);
    }
    return wrapper;
  }
}
