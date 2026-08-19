package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.allocation.HoldingGeographicAllocation;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.GeographicAllocation;
import com.fintex.wm.commons.domain.allocation.GeographicAllocationValue;
import com.fintex.wm.commons.domain.allocation.GeographicAllocationWithCurrency;
import com.fintex.wm.commons.domain.allocation.GeographicRegionType;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;
import com.fintex.wm.commons.domain.enumeration.Country;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.TreeSet;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.etf;
import static org.assertj.core.api.Assertions.assertThat;

class GeographicAllocationMapperTest {

  private final GeographicAllocationMapper mapper = new GeographicAllocationMapper();

  @Test
  void shouldMapAllocationsCurrencyAndProvider_whenResponseHasValues() {
    GeographicAllocationWithCurrency smsResponse = withCurrency(List.of(
        new GeographicAllocationValue(GeographicRegionType.US, new BigDecimal("60.5"), new TreeSet<>()),
        new GeographicAllocationValue(GeographicRegionType.EUROPE, new BigDecimal("30.0"), new TreeSet<>()),
        new GeographicAllocationValue(GeographicRegionType.ASIA, new BigDecimal("9.5"), new TreeSet<>())),
        Currency.USD, DataProvider.MORNINGSTAR);

    HoldingGeographicAllocation result = mapper.map(smsResponse, etf("SEC-001", Country.CANADA, 1));

    assertThat(result.getAllocations()).hasSize(3);
    assertThat(result.getAllocations()).containsEntry(GeographicRegionType.US, new BigDecimal("60.5"));
    assertThat(result.getAllocations()).containsEntry(GeographicRegionType.EUROPE, new BigDecimal("30.0"));
    assertThat(result.getAllocations()).containsEntry(GeographicRegionType.ASIA, new BigDecimal("9.5"));
    assertThat(result.getCurrency()).isEqualTo(Currency.USD);
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
  }

  @Test
  void shouldReturnEmptyAllocationsAndProvidersAndNullCurrency_whenResponseIsNull() {
    HoldingGeographicAllocation result = mapper.map(null, etf("SEC-002", Country.CANADA, 1));

    assertThat(result.getAllocations()).isEmpty();
    assertThat(result.getProviders()).isEmpty();
    assertThat(result.getCurrency()).isNull();
  }

  @Test
  void shouldKeepProvidersEmpty_whenDataProviderIsNull() {
    GeographicAllocationWithCurrency smsResponse = withCurrency(
        List.of(new GeographicAllocationValue(GeographicRegionType.US, BigDecimal.ONE, new TreeSet<>())),
        null, null);

    HoldingGeographicAllocation result = mapper.map(smsResponse, etf("SEC-003", Country.CANADA, 1));

    assertThat(result.getAllocations()).containsEntry(GeographicRegionType.US, BigDecimal.ONE);
    assertThat(result.getProviders()).isEmpty();
    assertThat(result.getCurrency()).isNull();
  }

  @Test
  void shouldSumDuplicateAllocationKeys() {
    GeographicAllocationWithCurrency smsResponse = withCurrency(List.of(
        new GeographicAllocationValue(GeographicRegionType.US, new BigDecimal("10.0"), new TreeSet<>()),
        new GeographicAllocationValue(GeographicRegionType.US, new BigDecimal("20.0"), new TreeSet<>())),
        Currency.CAD, null);

    HoldingGeographicAllocation result = mapper.map(smsResponse, etf("SEC-004", Country.CANADA, 1));

    assertThat(result.getAllocations()).containsEntry(GeographicRegionType.US, new BigDecimal("30.0"));
    assertThat(result.getCurrency()).isEqualTo(Currency.CAD);
  }

  @Test
  void shouldSkipNullValuesAndTypes() {
    GeographicAllocationWithCurrency smsResponse = withCurrency(List.of(
        new GeographicAllocationValue(null, BigDecimal.ONE, new TreeSet<>()),
        new GeographicAllocationValue(GeographicRegionType.ASIA, null, new TreeSet<>()),
        new GeographicAllocationValue(GeographicRegionType.EUROPE, BigDecimal.TEN, new TreeSet<>())),
        null, null);

    HoldingGeographicAllocation result = mapper.map(smsResponse, etf("SEC-005", Country.CANADA, 1));

    assertThat(result.getAllocations()).containsOnly(
        org.assertj.core.api.Assertions.entry(GeographicRegionType.EUROPE, BigDecimal.TEN));
  }

  private static GeographicAllocationWithCurrency withCurrency(List<GeographicAllocationValue> values,
      Currency currency, DataProvider provider) {
    GeographicAllocation allocation = new GeographicAllocation();
    allocation.setAllocations(values);
    allocation.setDataProviders(provider == null ? List.of() : List.of(provider));

    GeographicAllocationWithCurrency wrapper = new GeographicAllocationWithCurrency();
    wrapper.setGeographicAllocation(allocation);
    if (currency != null) {
      CurrencyDatapoint dp = new CurrencyDatapoint();
      dp.setValue(currency);
      wrapper.setCurrency(dp);
    }
    return wrapper;
  }

}
