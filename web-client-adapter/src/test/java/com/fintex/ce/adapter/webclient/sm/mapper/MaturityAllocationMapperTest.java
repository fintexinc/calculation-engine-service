package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.MaturityAllocation;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.DataProvider;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.datapoint.Maturities;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import com.fintex.sm.model.domain.enumeration.TimeDuration;
import com.fintex.sm.model.domain.value.MaturityDurationValue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaturityAllocationMapperTest {

  private final MaturityAllocationMapper mapper = new MaturityAllocationMapper();

  @Test
  void shouldMapPeriodsAndProvider_whenResponseHasMaturityValues() {
    var smsResponse = new Maturities();
    smsResponse.setPeriods(List.of(
        createPeriod(TimeDuration.ONE_TO_SEVEN_DAYS, "10.5"),
        createPeriod(TimeDuration.THREE_TO_FIVE_YEARS, "35.0"),
        createPeriod(TimeDuration.SEVEN_TO_TEN_YEARS, "54.5")));
    smsResponse.setDataProvider(DataProvider.MORNINGSTAR);

    MaturityAllocation result = mapper.map(smsResponse, createHolding("SEC-001"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-001");
    assertThat(result.getHoldingType()).isEqualTo(FinancialInstrumentType.ETF_CANADA);
    assertThat(result.getProvider()).isEqualTo("MORNINGSTAR");
    assertThat(result.getMaturityDurationValues()).hasSize(3);
    assertThat(result.getMaturityDurationValues())
        .containsEntry("ONE_TO_SEVEN_DAYS", BigDecimal.valueOf(10.5));
    assertThat(result.getMaturityDurationValues())
        .containsEntry("THREE_TO_FIVE_YEARS", BigDecimal.valueOf(35.0));
    assertThat(result.getMaturityDurationValues())
        .containsEntry("SEVEN_TO_TEN_YEARS", BigDecimal.valueOf(54.5));
  }

  @Test
  void shouldReturnEmptyMap_whenResponseIsNull() {
    MaturityAllocation result = mapper.map(null, createHolding("SEC-002"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-002");
    assertThat(result.getProvider()).isNull();
    assertThat(result.getMaturityDurationValues()).isEmpty();
  }

  @Test
  void shouldReturnEmptyMap_whenPeriodsListIsNull() {
    var smsResponse = new Maturities();
    smsResponse.setPeriods(null);

    MaturityAllocation result = mapper.map(smsResponse, createHolding("SEC-003"));

    assertThat(result.getMaturityDurationValues()).isEmpty();
    assertThat(result.getProvider()).isNull();
  }

  @Test
  void shouldNotSetProvider_whenDataProviderIsNull() {
    var smsResponse = new Maturities();
    smsResponse.setPeriods(List.of());
    smsResponse.setDataProvider(null);

    MaturityAllocation result = mapper.map(smsResponse, createHolding("SEC-004"));

    assertThat(result.getProvider()).isNull();
  }

  @Test
  void shouldFilterOutEntriesWithNullDurationOrValue() {
    var valid = createPeriod(TimeDuration.FIVE_TO_SEVEN_YEARS, "25.0");

    var nullDuration = new MaturityDurationValue();
    nullDuration.setMaturityDuration(null);
    nullDuration.setValue(BigDecimal.valueOf(10.0));

    var nullValue = new MaturityDurationValue();
    nullValue.setMaturityDuration(TimeDuration.ONE_TO_SEVEN_DAYS);
    nullValue.setValue(null);

    var smsResponse = new Maturities();
    smsResponse.setPeriods(List.of(valid, nullDuration, nullValue));

    MaturityAllocation result = mapper.map(smsResponse, createHolding("SEC-005"));

    assertThat(result.getMaturityDurationValues()).hasSize(1);
    assertThat(result.getMaturityDurationValues())
        .containsEntry("FIVE_TO_SEVEN_YEARS", BigDecimal.valueOf(25.0));
  }

  @Test
  void shouldSumValues_whenDuplicateDurationsExist() {
    var smsResponse = new Maturities();
    smsResponse.setPeriods(List.of(
        createPeriod(TimeDuration.THREE_TO_FIVE_YEARS, "20.0"),
        createPeriod(TimeDuration.THREE_TO_FIVE_YEARS, "15.0")));

    MaturityAllocation result = mapper.map(smsResponse, createHolding("SEC-006"));

    assertThat(result.getMaturityDurationValues()).hasSize(1);
    assertThat(result.getMaturityDurationValues().get("THREE_TO_FIVE_YEARS"))
        .isEqualByComparingTo("35.0");
  }

  private MaturityDurationValue createPeriod(TimeDuration duration, String value) {
    var mdv = new MaturityDurationValue();
    mdv.setMaturityDuration(duration);
    mdv.setValue(new BigDecimal(value));
    return mdv;
  }

  private Holding createHolding(String securityId) {
    return new Holding(null, FinancialInstrumentType.ETF_CANADA, new SecurityIdentifier(securityId, null));
  }
}
