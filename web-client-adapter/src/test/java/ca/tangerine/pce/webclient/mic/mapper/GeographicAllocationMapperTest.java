package ca.tangerine.pce.webclient.mic.mapper;

import ca.tangerine.pce.model.domain.calculation.allocation.HoldingGeographicAllocation;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.allocation.GeographicAllocation;
import ca.tangerine.wm.commons.domain.allocation.GeographicAllocationValue;
import ca.tangerine.wm.commons.domain.allocation.GeographicAllocationWithCurrency;
import ca.tangerine.wm.commons.domain.allocation.GeographicRegionType;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.currency.CurrencyDatapoint;
import ca.tangerine.wm.commons.domain.enumeration.Country;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.TreeSet;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.etf;
import static org.assertj.core.api.Assertions.assertThat;

class GeographicAllocationMapperTest {

  private final GeographicAllocationMapper mapper = new GeographicAllocationMapper();

  @Test
  void shouldMapAllocationsCurrencyAndProvider_whenResponseHasValues() {
    GeographicAllocationWithCurrency micResponse = withCurrency(List.of(
        new GeographicAllocationValue(GeographicRegionType.US, new BigDecimal("60.5"), new TreeSet<>()),
        new GeographicAllocationValue(GeographicRegionType.EUROPE, new BigDecimal("30.0"), new TreeSet<>()),
        new GeographicAllocationValue(GeographicRegionType.ASIA, new BigDecimal("9.5"), new TreeSet<>())),
        Currency.USD, DataProvider.MORNINGSTAR);

    HoldingGeographicAllocation result = mapper.map(micResponse, etf("SEC-001", Country.CANADA, 1));

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
    GeographicAllocationWithCurrency micResponse = withCurrency(
        List.of(new GeographicAllocationValue(GeographicRegionType.US, BigDecimal.ONE, new TreeSet<>())),
        null, null);

    HoldingGeographicAllocation result = mapper.map(micResponse, etf("SEC-003", Country.CANADA, 1));

    assertThat(result.getAllocations()).containsEntry(GeographicRegionType.US, BigDecimal.ONE);
    assertThat(result.getProviders()).isEmpty();
    assertThat(result.getCurrency()).isNull();
  }

  @Test
  void shouldSumDuplicateAllocationKeys() {
    GeographicAllocationWithCurrency micResponse = withCurrency(List.of(
        new GeographicAllocationValue(GeographicRegionType.US, new BigDecimal("10.0"), new TreeSet<>()),
        new GeographicAllocationValue(GeographicRegionType.US, new BigDecimal("20.0"), new TreeSet<>())),
        Currency.CAD, null);

    HoldingGeographicAllocation result = mapper.map(micResponse, etf("SEC-004", Country.CANADA, 1));

    assertThat(result.getAllocations()).containsEntry(GeographicRegionType.US, new BigDecimal("30.0"));
    assertThat(result.getCurrency()).isEqualTo(Currency.CAD);
  }

  @Test
  void shouldSkipNullValuesAndTypes() {
    GeographicAllocationWithCurrency micResponse = withCurrency(List.of(
        new GeographicAllocationValue(null, BigDecimal.ONE, new TreeSet<>()),
        new GeographicAllocationValue(GeographicRegionType.ASIA, null, new TreeSet<>()),
        new GeographicAllocationValue(GeographicRegionType.EUROPE, BigDecimal.TEN, new TreeSet<>())),
        null, null);

    HoldingGeographicAllocation result = mapper.map(micResponse, etf("SEC-005", Country.CANADA, 1));

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
