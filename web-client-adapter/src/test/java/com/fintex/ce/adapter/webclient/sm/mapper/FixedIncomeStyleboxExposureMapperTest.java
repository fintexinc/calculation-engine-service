package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.exposure.FixedIncomeStyleboxExposure;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.rating.FixedIncomeStyleBoxType;
import com.fintex.wm.commons.domain.rating.FixedIncomeStyleBoxValue;
import com.fintex.wm.commons.domain.rating.FixedIncomeStyleBoxes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holdingWithoutCountry;
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
    smsResponse.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    PortfolioHolding holding = holdingWithoutCountry(new SecurityIdentifier("AGG", null), FinancialInstrumentType.ETF,
        (BigDecimal) null);

    FixedIncomeStyleboxExposure result = mapper.map(smsResponse, holding);

    assertThat(result.getBoxValues()).hasSize(3);
    assertThat(result.getBoxValues().get(FixedIncomeStyleBoxType.HIGH_LIMITED)).isEqualByComparingTo("15.50");
    assertThat(result.getBoxValues().get(FixedIncomeStyleBoxType.HIGH_MODERATE)).isEqualByComparingTo("22.30");
    assertThat(result.getBoxValues().get(FixedIncomeStyleBoxType.MEDIUM_EXTENSIVE)).isEqualByComparingTo("18.75");
    assertThat(result.getHoldingType()).isEqualTo(FinancialInstrumentType.ETF);
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
  }

  @ParameterizedTest
  @MethodSource("nullAndEmptyResponses")
  void shouldReturnEmptyBoxValues_whenResponseIsNullOrHasNoBoxValues(
      FixedIncomeStyleBoxes smsResponse) {
    PortfolioHolding holding = holdingWithoutCountry(new SecurityIdentifier("TEST.ID", null),
        FinancialInstrumentType.FUND, (BigDecimal) null);

    FixedIncomeStyleboxExposure result = mapper.map(smsResponse, holding);

    assertThat(result.getBoxValues()).isEmpty();
    assertThat(result.getHoldingType()).isEqualTo(FinancialInstrumentType.FUND);
    assertThat(result.getProviders()).isEmpty();
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
    var nullValueEntry = FixedIncomeStyleBoxValue.builder().styleBoxType(FixedIncomeStyleBoxType.LOW_MODERATE).value(
        null).build();

    var smsResponse = new FixedIncomeStyleBoxes();
    smsResponse.setBoxValues(List.of(validEntry, nullTypeEntry, nullValueEntry));

    FixedIncomeStyleboxExposure result = mapper.map(smsResponse, holdingWithoutCountry(new SecurityIdentifier("TEST.ID",
        null), FinancialInstrumentType.ETF,
        (BigDecimal) null));

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

    FixedIncomeStyleboxExposure result = mapper.map(smsResponse, holdingWithoutCountry(new SecurityIdentifier(
        "FULL.TEST", null), FinancialInstrumentType.ETF,
        (BigDecimal) null));

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

}