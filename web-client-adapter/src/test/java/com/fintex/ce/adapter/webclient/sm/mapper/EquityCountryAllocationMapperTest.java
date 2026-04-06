package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.DataProvider;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.allocation.CountryAllocation;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import com.fintex.sm.model.domain.value.CountryValue;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EquityCountryAllocationMapperTest {

  private final EquityCountryAllocationMapper sut = new EquityCountryAllocationMapper();

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

    EquityCountryAllocation result = sut.map(smsResponse, createHolding("SEC-001"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-001");
    assertThat(result.getProvider()).isEqualTo("MORNINGSTAR");
    assertThat(result.getAllocations()).hasSize(2);
    assertThat(result.getAllocations()).containsEntry("CAN", BigDecimal.valueOf(0.65));
    assertThat(result.getAllocations()).containsEntry("USA", BigDecimal.valueOf(0.35));
  }

  @Test
  void shouldReturnEmptyAllocations_whenResponseIsNull() {
    EquityCountryAllocation result = sut.map(null, createHolding("SEC-002"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-002");
    assertThat(result.getProvider()).isNull();
    assertThat(result.getAllocations()).isEmpty();
  }

  @Test
  void shouldReturnEmptyAllocations_whenAllocationListIsNull() {
    var smsResponse = new CountryAllocation();
    smsResponse.setAllocation(null);

    EquityCountryAllocation result = sut.map(smsResponse, createHolding("SEC-003"));

    assertThat(result.getAllocations()).isEmpty();
    assertThat(result.getProvider()).isNull();
  }

  @Test
  void shouldNotSetProvider_whenDataProviderIsNull() {
    var smsResponse = new CountryAllocation();
    smsResponse.setAllocation(List.of());
    smsResponse.setDataProvider(null);

    EquityCountryAllocation result = sut.map(smsResponse, createHolding("SEC-004"));

    assertThat(result.getProvider()).isNull();
  }

  private Holding createHolding(String securityId) {
    var identifier = new SecurityIdentifier();
    identifier.setId(securityId);
    return new Holding(null, FinancialInstrumentType.ETF_CANADA, identifier);
  }
}