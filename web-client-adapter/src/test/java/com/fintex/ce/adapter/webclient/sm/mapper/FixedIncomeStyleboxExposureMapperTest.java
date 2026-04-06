package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.FixedIncomeStyleboxExposure;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.DataProvider;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import com.fintex.sm.model.domain.enumeration.FixedIncomeStyleBoxType;
import com.fintex.sm.model.domain.rating.FixedIncomeStyleBoxes;
import com.fintex.sm.model.domain.value.FixedIncomeStyleBoxValue;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

class FixedIncomeStyleboxExposureMapperTest {

  private final FixedIncomeStyleboxExposureMapper mapper = new FixedIncomeStyleboxExposureMapper();

  @Test
  void shouldMapAllFieldsCorrectly_whenResponseHasMultipleBoxValuesAndProvider() {
    var highLimited = createEntry(FixedIncomeStyleBoxType.HIGH_LIMITED, "15.50");
    var highModerate = createEntry(FixedIncomeStyleBoxType.HIGH_MODERATE, "22.30");
    var mediumExtensive = createEntry(FixedIncomeStyleBoxType.MEDIUM_EXTENSIVE, "18.75");

    var smsResponse = new FixedIncomeStyleBoxes();
    smsResponse.setBoxValues(List.of(highLimited, highModerate, mediumExtensive));
    smsResponse.setDataProvider(DataProvider.MORNINGSTAR);

    Holding holding = createHolding("AGG", FinancialInstrumentType.ETF);

    FixedIncomeStyleboxExposure result = mapper.map(smsResponse, holding);

    assertThat(result.getBoxValues()).hasSize(3);
    assertThat(result.getBoxValues().get(FixedIncomeStyleBoxType.HIGH_LIMITED)).isEqualByComparingTo("15.50");
    assertThat(result.getBoxValues().get(FixedIncomeStyleBoxType.HIGH_MODERATE)).isEqualByComparingTo("22.30");
    assertThat(result.getBoxValues().get(FixedIncomeStyleBoxType.MEDIUM_EXTENSIVE)).isEqualByComparingTo("18.75");
    assertThat(result.getHoldingId()).isEqualTo("AGG");
    assertThat(result.getHoldingType()).isEqualTo(FinancialInstrumentType.ETF);
    assertThat(result.getProvider()).isEqualTo(DataProvider.MORNINGSTAR.name());
  }

  @ParameterizedTest
  @MethodSource("nullAndEmptyResponses")
  void shouldReturnEmptyBoxValues_whenResponseIsNullOrHasNoBoxValues(
      FixedIncomeStyleBoxes smsResponse) {
    Holding holding = createHolding("TEST.ID", FinancialInstrumentType.FUND);

    FixedIncomeStyleboxExposure result = mapper.map(smsResponse, holding);

    assertThat(result.getBoxValues()).isEmpty();
    assertThat(result.getHoldingId()).isEqualTo("TEST.ID");
    assertThat(result.getHoldingType()).isEqualTo(FinancialInstrumentType.FUND);
    assertThat(result.getProvider()).isNull();
  }

  static Stream<Arguments> nullAndEmptyResponses() {
    var nullBoxValuesResponse = new FixedIncomeStyleBoxes();
    nullBoxValuesResponse.setBoxValues(null);

    var emptyBoxValuesResponse = new FixedIncomeStyleBoxes();
    emptyBoxValuesResponse.setBoxValues(List.of());

    return Stream.of(
        Arguments.of((FixedIncomeStyleBoxes) null),
        Arguments.of(nullBoxValuesResponse),
        Arguments.of(emptyBoxValuesResponse));
  }

  @Test
  void shouldFilterOutEntriesWithNullTypeOrValue() {
    var validEntry = createEntry(FixedIncomeStyleBoxType.LOW_LIMITED, "10.0");
    var nullTypeEntry = FixedIncomeStyleBoxValue.builder().styleBoxType(null).value(BigDecimal.valueOf(5.0)).build();
    var nullValueEntry = FixedIncomeStyleBoxValue.builder().styleBoxType(FixedIncomeStyleBoxType.LOW_MODERATE).value(null).build();

    var smsResponse = new FixedIncomeStyleBoxes();
    smsResponse.setBoxValues(List.of(validEntry, nullTypeEntry, nullValueEntry));

    FixedIncomeStyleboxExposure result = mapper.map(smsResponse, createHolding("TEST.ID", FinancialInstrumentType.ETF_CANADA));

    assertThat(result.getBoxValues()).hasSize(1);
    assertThat(result.getBoxValues()).containsKey(FixedIncomeStyleBoxType.LOW_LIMITED);
    assertThat(result.getBoxValues()).doesNotContainKey(null);
  }

  @Test
  void shouldMapAllStyleboxTypes() {
    List<FixedIncomeStyleBoxValue> entries = List.of(
        createEntry(FixedIncomeStyleBoxType.HIGH_LIMITED, "10.0"),
        createEntry(FixedIncomeStyleBoxType.HIGH_MODERATE, "12.0"),
        createEntry(FixedIncomeStyleBoxType.HIGH_EXTENSIVE, "8.0"),
        createEntry(FixedIncomeStyleBoxType.MEDIUM_LIMITED, "15.0"),
        createEntry(FixedIncomeStyleBoxType.MEDIUM_MODERATE, "11.0"),
        createEntry(FixedIncomeStyleBoxType.MEDIUM_EXTENSIVE, "9.0"),
        createEntry(FixedIncomeStyleBoxType.LOW_LIMITED, "13.0"),
        createEntry(FixedIncomeStyleBoxType.LOW_MODERATE, "14.0"),
        createEntry(FixedIncomeStyleBoxType.LOW_EXTENSIVE, "8.0"));

    var smsResponse = new FixedIncomeStyleBoxes();
    smsResponse.setBoxValues(entries);

    FixedIncomeStyleboxExposure result = mapper.map(smsResponse, createHolding("FULL.TEST", FinancialInstrumentType.ETF));

    assertThat(result.getBoxValues()).hasSize(9);
    assertThat(result.getBoxValues().get(FixedIncomeStyleBoxType.HIGH_LIMITED)).isEqualByComparingTo("10.0");
    assertThat(result.getBoxValues().get(FixedIncomeStyleBoxType.HIGH_MODERATE)).isEqualByComparingTo("12.0");
    assertThat(result.getBoxValues().get(FixedIncomeStyleBoxType.HIGH_EXTENSIVE)).isEqualByComparingTo("8.0");
    assertThat(result.getBoxValues().get(FixedIncomeStyleBoxType.MEDIUM_LIMITED)).isEqualByComparingTo("15.0");
    assertThat(result.getBoxValues().get(FixedIncomeStyleBoxType.MEDIUM_MODERATE)).isEqualByComparingTo("11.0");
    assertThat(result.getBoxValues().get(FixedIncomeStyleBoxType.MEDIUM_EXTENSIVE)).isEqualByComparingTo("9.0");
    assertThat(result.getBoxValues().get(FixedIncomeStyleBoxType.LOW_LIMITED)).isEqualByComparingTo("13.0");
    assertThat(result.getBoxValues().get(FixedIncomeStyleBoxType.LOW_MODERATE)).isEqualByComparingTo("14.0");
    assertThat(result.getBoxValues().get(FixedIncomeStyleBoxType.LOW_EXTENSIVE)).isEqualByComparingTo("8.0");
  }

  private FixedIncomeStyleBoxValue createEntry(FixedIncomeStyleBoxType type, String value) {
    return FixedIncomeStyleBoxValue.builder()
        .styleBoxType(type)
        .value(new BigDecimal(value))
        .build();
  }

  private Holding createHolding(String securityId, FinancialInstrumentType holdingType) {
    var identifier = new SecurityIdentifier();
    identifier.setId(securityId);
    return new Holding(null, holdingType, identifier);
  }
}