package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.allocation.EquityCountryAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.CountryAllocation;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.value.CountryValue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EquityCountryAllocationMapperTest {

  private final EquityCountryAllocationMapper mapper = new EquityCountryAllocationMapper();

  @Test
  void shouldMapAllocationsAndProvider_whenResponseHasCountryValues() {
    var canada = new CountryValue();
    canada.setIsoCode("CAN");
    canada.setValue(BigDecimal.valueOf(0.65));

    var usa = new CountryValue();
    usa.setIsoCode("USA");
    usa.setValue(BigDecimal.valueOf(0.35));

    var smsResponse = new CountryAllocation();
    smsResponse.setAllocation(List.of(canada, usa));
    smsResponse.setDataProvider(DataProvider.MORNINGSTAR);

    EquityCountryAllocation result = mapper.map(smsResponse, createHolding("SEC-001"));

    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
    assertThat(result.getAllocations()).hasSize(2);
    assertThat(result.getAllocations()).containsEntry("CAN", BigDecimal.valueOf(0.65));
    assertThat(result.getAllocations()).containsEntry("USA", BigDecimal.valueOf(0.35));
  }

  @Test
  void shouldReturnEmptyAllocations_whenResponseIsNull() {
    EquityCountryAllocation result = mapper.map(null, createHolding("SEC-002"));

    assertThat(result.getProviders()).isEmpty();
    assertThat(result.getAllocations()).isEmpty();
  }

  @Test
  void shouldReturnEmptyAllocations_whenAllocationListIsNull() {
    var smsResponse = new CountryAllocation();
    smsResponse.setAllocation(null);

    EquityCountryAllocation result = mapper.map(smsResponse, createHolding("SEC-003"));

    assertThat(result.getAllocations()).isEmpty();
    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldNotSetProvider_whenDataProviderIsNull() {
    var smsResponse = new CountryAllocation();
    smsResponse.setAllocation(List.of());
    smsResponse.setDataProvider(null);

    EquityCountryAllocation result = mapper.map(smsResponse, createHolding("SEC-004"));

    assertThat(result.getProviders()).isEmpty();
  }

  private PortfolioHolding createHolding(String securityId) {
    var identifier = new SecurityIdentifier();
    identifier.setId(securityId);
    return new PortfolioHolding(null, FinancialInstrumentType.ETF_CANADA, identifier);
  }
}