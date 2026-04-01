package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.CountryExposure;
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

class CountryExposureMapperTest {

  private final CountryExposureMapper mapper = new CountryExposureMapper();

  @Test
  void shouldMapAllocationsAndProvider_whenResponseHasCountryValues() {
    var canada = createCountryValue("CAN", "0.65");
    var usa = createCountryValue("USA", "0.35");

    var smsResponse = new CountryAllocation();
    smsResponse.setAllocation(List.of(canada, usa));
    smsResponse.setDataProvider(DataProvider.MORNINGSTAR);

    CountryExposure result = mapper.map(smsResponse, createHolding("SEC-001"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-001");
    assertThat(result.getHoldingType()).isEqualTo(FinancialInstrumentType.ETF_CANADA);
    assertThat(result.getProvider()).isEqualTo("MORNINGSTAR");
    assertThat(result.getAllocations()).hasSize(2);
    assertThat(result.getAllocations()).containsEntry("CAN", BigDecimal.valueOf(0.65));
    assertThat(result.getAllocations()).containsEntry("USA", BigDecimal.valueOf(0.35));
  }

  @Test
  void shouldReturnEmptyAllocations_whenResponseIsNull() {
    CountryExposure result = mapper.map(null, createHolding("SEC-002"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-002");
    assertThat(result.getProvider()).isNull();
    assertThat(result.getAllocations()).isEmpty();
  }

  @Test
  void shouldReturnEmptyAllocations_whenAllocationListIsNull() {
    var smsResponse = new CountryAllocation();
    smsResponse.setAllocation(null);

    CountryExposure result = mapper.map(smsResponse, createHolding("SEC-003"));

    assertThat(result.getAllocations()).isEmpty();
    assertThat(result.getProvider()).isNull();
  }

  @Test
  void shouldNotSetProvider_whenDataProviderIsNull() {
    var smsResponse = new CountryAllocation();
    smsResponse.setAllocation(List.of());
    smsResponse.setDataProvider(null);

    CountryExposure result = mapper.map(smsResponse, createHolding("SEC-004"));

    assertThat(result.getProvider()).isNull();
  }

  @Test
  void shouldFilterOutEntriesWithNullIsoCodeOrValue() {
    var valid = createCountryValue("CAN", "0.65");

    var nullIso = new CountryValue();
    nullIso.setIsoCode(null);
    nullIso.setValue(BigDecimal.valueOf(0.20));

    var nullValue = new CountryValue();
    nullValue.setIsoCode("GBR");
    nullValue.setValue(null);

    var smsResponse = new CountryAllocation();
    smsResponse.setAllocation(List.of(valid, nullIso, nullValue));

    CountryExposure result = mapper.map(smsResponse, createHolding("SEC-005"));

    assertThat(result.getAllocations()).hasSize(1);
    assertThat(result.getAllocations()).containsEntry("CAN", BigDecimal.valueOf(0.65));
  }

  @Test
  void shouldSumValues_whenDuplicateIsoCodesExist() {
    var can1 = createCountryValue("CAN", "0.40");
    var can2 = createCountryValue("CAN", "0.25");

    var smsResponse = new CountryAllocation();
    smsResponse.setAllocation(List.of(can1, can2));

    CountryExposure result = mapper.map(smsResponse, createHolding("SEC-006"));

    assertThat(result.getAllocations()).hasSize(1);
    assertThat(result.getAllocations().get("CAN")).isEqualByComparingTo("0.65");
  }

  private CountryValue createCountryValue(String isoCode, String value) {
    var cv = new CountryValue();
    cv.setIsoCode(isoCode);
    cv.setValue(new BigDecimal(value));
    return cv;
  }

  private Holding createHolding(String securityId) {
    return new Holding()
        .setHoldingType(FinancialInstrumentType.ETF_CANADA)
        .setSecurityIdentifier(new SecurityIdentifier(securityId, null));
  }
}
