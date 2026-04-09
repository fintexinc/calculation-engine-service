package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.HoldingEquityMarketCap;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.DataProvider;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.datapoint.EquityMarketCapitalization;
import com.fintex.sm.model.domain.enumeration.EquityMarketCapitalizationType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import com.fintex.sm.model.domain.value.EquityMarketCapitalizationTypeValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class EquityMarketCapitalizationMapperTest {

  private final EquityMarketCapitalizationMapper mapper = new EquityMarketCapitalizationMapper();

  @Test
  void shouldMapAllFieldsCorrectly_whenResponseHasAllCapTypesAndProvider() {
    var smsResponse = new EquityMarketCapitalization();
    smsResponse.setValues(List.of(
        createEntry(EquityMarketCapitalizationType.GIANT, "45.67"),
        createEntry(EquityMarketCapitalizationType.LARGE, "30.00"),
        createEntry(EquityMarketCapitalizationType.MEDIUM, "12.33"),
        createEntry(EquityMarketCapitalizationType.SMALL, "8.50"),
        createEntry(EquityMarketCapitalizationType.MICRO, "3.50")));
    smsResponse.setDataProvider(DataProvider.MORNINGSTAR);

    HoldingEquityMarketCap result = mapper.map(smsResponse, createHolding("VTI"));

    assertThat(result.getRatings()).hasSize(5);
    assertThat(result.getRatings().get(EquityMarketCapitalizationType.GIANT)).isEqualByComparingTo("45.67");
    assertThat(result.getRatings().get(EquityMarketCapitalizationType.LARGE)).isEqualByComparingTo("30.00");
    assertThat(result.getRatings().get(EquityMarketCapitalizationType.MEDIUM)).isEqualByComparingTo("12.33");
    assertThat(result.getRatings().get(EquityMarketCapitalizationType.SMALL)).isEqualByComparingTo("8.50");
    assertThat(result.getRatings().get(EquityMarketCapitalizationType.MICRO)).isEqualByComparingTo("3.50");
    assertThat(result.getHoldingId()).isEqualTo("VTI");
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
  }

  @ParameterizedTest
  @MethodSource("nullAndEmptyResponses")
  void shouldReturnEmptyRatings_whenResponseIsNullOrHasNoValues(
      EquityMarketCapitalization smsResponse) {
    HoldingEquityMarketCap result = mapper.map(smsResponse, createHolding("TEST.ID"));

    assertThat(result.getRatings()).isEmpty();
    assertThat(result.getHoldingId()).isEqualTo("TEST.ID");
    assertThat(result.getProviders()).isEmpty();
  }

  static Stream<Arguments> nullAndEmptyResponses() {
    var nullValuesResponse = new EquityMarketCapitalization();
    nullValuesResponse.setValues(null);

    var emptyValuesResponse = new EquityMarketCapitalization();
    emptyValuesResponse.setValues(List.of());

    return Stream.of(
        Arguments.of((EquityMarketCapitalization) null),
        Arguments.of(nullValuesResponse),
        Arguments.of(emptyValuesResponse));
  }

  @Test
  void shouldFilterOutEntriesWithNullTypeOrValue() {
    var validEntry = createEntry(EquityMarketCapitalizationType.GIANT, "50.0");

    var nullTypeEntry = new EquityMarketCapitalizationTypeValue();
    nullTypeEntry.setEquityMarketCapitalization(null);
    nullTypeEntry.setValue(BigDecimal.valueOf(10.0));

    var nullValueEntry = new EquityMarketCapitalizationTypeValue();
    nullValueEntry.setEquityMarketCapitalization(EquityMarketCapitalizationType.SMALL);
    nullValueEntry.setValue(null);

    var smsResponse = new EquityMarketCapitalization();
    smsResponse.setValues(List.of(validEntry, nullTypeEntry, nullValueEntry));

    HoldingEquityMarketCap result = mapper.map(smsResponse, createHolding("TEST.ID"));

    assertThat(result.getRatings()).hasSize(1);
    assertThat(result.getRatings()).containsKey(EquityMarketCapitalizationType.GIANT);
    assertThat(result.getRatings()).doesNotContainKey(EquityMarketCapitalizationType.SMALL);
  }

  @Test
  void shouldNotSetProvider_whenDataProviderIsNull() {
    var smsResponse = new EquityMarketCapitalization();
    smsResponse.setValues(List.of());
    smsResponse.setDataProvider(null);

    HoldingEquityMarketCap result = mapper.map(smsResponse, createHolding("SEC-001"));

    assertThat(result.getProviders()).isEmpty();
  }

  private EquityMarketCapitalizationTypeValue createEntry(
      EquityMarketCapitalizationType type, String value) {
    var entry = new EquityMarketCapitalizationTypeValue();
    entry.setEquityMarketCapitalization(type);
    entry.setValue(new BigDecimal(value));
    return entry;
  }

  private Holding createHolding(String securityId) {
    return new Holding(null, FinancialInstrumentType.ETF_CANADA, new SecurityIdentifier(securityId, null));
  }
}
