package com.fintex.ce.adapter.webclient.mic.mapper;

import com.fintex.ce.model.domain.calculation.exposure.CountryExposure;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.CountryAllocation;
import com.fintex.wm.commons.domain.allocation.CountryAllocationValue;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CountryExposureMapperTest {

  private final CountryExposureMapper mapper = new CountryExposureMapper();

  @Test
  void shouldMapAllocationsAndProvider_whenResponseHasCountryAllocationValues() {
    var canada = createCountryAllocationValue(Country.CANADA, "0.65");
    var usa = createCountryAllocationValue(Country.USA, "0.35");

    var micResponse = new CountryAllocation();
    micResponse.setAllocations(List.of(canada, usa));
    micResponse.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    CountryExposure result = mapper.map(micResponse, createHolding("SEC-001"));

    assertThat(result.getHoldingType()).isEqualTo(FinancialInstrumentType.ETF);
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
    assertThat(result.getAllocations()).hasSize(2);
    assertThat(result.getAllocations()).containsEntry(Country.CANADA, BigDecimal.valueOf(0.65));
    assertThat(result.getAllocations()).containsEntry(Country.USA, BigDecimal.valueOf(0.35));
  }

  @Test
  void shouldReturnEmptyAllocations_whenResponseIsNull() {
    CountryExposure result = mapper.map(null, createHolding("SEC-002"));

    assertThat(result.getProviders()).isEmpty();
    assertThat(result.getAllocations()).isEmpty();
  }

  @Test
  void shouldReturnEmptyAllocations_whenAllocationListIsNull() {
    var micResponse = new CountryAllocation();
    micResponse.setAllocations(null);

    CountryExposure result = mapper.map(micResponse, createHolding("SEC-003"));

    assertThat(result.getAllocations()).isEmpty();
    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldNotSetProvider_whenDataProviderIsNull() {
    var micResponse = new CountryAllocation();
    micResponse.setAllocations(List.of());
    micResponse.setDataProviders(null);

    CountryExposure result = mapper.map(micResponse, createHolding("SEC-004"));

    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldFilterOutEntriesWithNullIsoCodeOrValue() {
    var valid = createCountryAllocationValue(Country.CANADA, "0.65");

    var nullIso = new CountryAllocationValue();
    nullIso.setType(null);
    nullIso.setValue(BigDecimal.valueOf(0.20));

    var nullValue = new CountryAllocationValue();
    nullValue.setType(Country.UNITED_KINGDOM);
    nullValue.setValue(null);

    var micResponse = new CountryAllocation();
    micResponse.setAllocations(List.of(valid, nullIso, nullValue));

    CountryExposure result = mapper.map(micResponse, createHolding("SEC-005"));

    assertThat(result.getAllocations()).hasSize(1);
    assertThat(result.getAllocations()).containsEntry(Country.CANADA, BigDecimal.valueOf(0.65));
  }

  @Test
  void shouldSumValues_whenDuplicateIsoCodesExist() {
    var can1 = createCountryAllocationValue(Country.CANADA, "0.40");
    var can2 = createCountryAllocationValue(Country.CANADA, "0.25");

    var micResponse = new CountryAllocation();
    micResponse.setAllocations(List.of(can1, can2));

    CountryExposure result = mapper.map(micResponse, createHolding("SEC-006"));

    assertThat(result.getAllocations()).hasSize(1);
    assertThat(result.getAllocations().get(Country.CANADA)).isEqualByComparingTo("0.65");
  }

  private CountryAllocationValue createCountryAllocationValue(Country country, String value) {
    var cv = new CountryAllocationValue();
    cv.setType(country);
    cv.setValue(new BigDecimal(value));
    return cv;
  }

  private PortfolioHolding createHolding(String securityId) {
    return new PortfolioHolding(null, FinancialInstrumentType.ETF, Country.CANADA,
        new SecurityIdentifier(securityId, null));
  }
}
