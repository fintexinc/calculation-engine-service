package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.allocation.EquityCountryAllocation;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.CountryAllocation;
import com.fintex.wm.commons.domain.allocation.CountryAllocationValue;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holding;
import static org.assertj.core.api.Assertions.assertThat;

class EquityCountryAllocationMapperTest {

  private final EquityCountryAllocationMapper mapper = new EquityCountryAllocationMapper();

  @Test
  void shouldMapAllocationsAndProvider_whenResponseHasCountryAllocationValues() {
    var canada = new CountryAllocationValue();
    canada.setType(Country.CANADA);
    canada.setValue(BigDecimal.valueOf(0.65));

    var usa = new CountryAllocationValue();
    usa.setType(Country.USA);
    usa.setValue(BigDecimal.valueOf(0.35));

    var smsResponse = new CountryAllocation();
    smsResponse.setAllocations(List.of(canada, usa));
    smsResponse.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    EquityCountryAllocation result = mapper.map(smsResponse, holding(new SecurityIdentifier("SEC-001", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
    assertThat(result.getAllocations()).hasSize(2);
    assertThat(result.getAllocations()).containsEntry(Country.CANADA, BigDecimal.valueOf(0.65));
    assertThat(result.getAllocations()).containsEntry(Country.USA, BigDecimal.valueOf(0.35));
  }

  @Test
  void shouldReturnEmptyAllocations_whenResponseIsNull() {
    EquityCountryAllocation result = mapper.map(null, holding(new SecurityIdentifier("SEC-002", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getProviders()).isEmpty();
    assertThat(result.getAllocations()).isEmpty();
  }

  @Test
  void shouldReturnEmptyAllocations_whenAllocationListIsNull() {
    var smsResponse = new CountryAllocation();
    smsResponse.setAllocations(null);

    EquityCountryAllocation result = mapper.map(smsResponse, holding(new SecurityIdentifier("SEC-003", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getAllocations()).isEmpty();
    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldNotSetProvider_whenDataProviderIsNull() {
    var smsResponse = new CountryAllocation();
    smsResponse.setAllocations(List.of());
    smsResponse.setDataProviders(null);

    EquityCountryAllocation result = mapper.map(smsResponse, holding(new SecurityIdentifier("SEC-004", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getProviders()).isEmpty();
  }

}