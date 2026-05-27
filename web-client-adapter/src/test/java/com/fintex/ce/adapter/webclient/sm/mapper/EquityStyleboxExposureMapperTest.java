package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.exposure.EquityStyleboxExposure;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.rating.StyleBoxType;
import com.fintex.wm.commons.domain.rating.StyleBoxValue;
import com.fintex.wm.commons.domain.rating.StyleBoxes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class EquityStyleboxExposureMapperTest {

  private final EquityStyleboxExposureMapper mapper = new EquityStyleboxExposureMapper();

  @Test
  void shouldMapAllFieldsCorrectly_whenResponseHasMultipleBoxValuesAndProvider() {
    var styleBoxes = new StyleBoxes();
    styleBoxes.setBoxValues(List.of(
        new StyleBoxValue(StyleBoxType.LARGE_VALUE, new BigDecimal("18.5")),
        new StyleBoxValue(StyleBoxType.LARGE_CORE, new BigDecimal("42.3")),
        new StyleBoxValue(StyleBoxType.MID_GROWTH, new BigDecimal("3.8"))));
    styleBoxes.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    PortfolioHolding holding = createHolding("XIU.TO", FinancialInstrumentType.ETF_CANADA);

    EquityStyleboxExposure result = mapper.map(styleBoxes, holding);

    assertThat(result.getBoxValues()).hasSize(3);
    assertThat(result.getBoxValues().get(StyleBoxType.LARGE_VALUE)).isEqualByComparingTo("18.5");
    assertThat(result.getBoxValues().get(StyleBoxType.LARGE_CORE)).isEqualByComparingTo("42.3");
    assertThat(result.getBoxValues().get(StyleBoxType.MID_GROWTH)).isEqualByComparingTo("3.8");
    assertThat(result.getHoldingType()).isEqualTo(FinancialInstrumentType.ETF_CANADA);
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
  }

  @ParameterizedTest
  @MethodSource("nullAndEmptyResponses")
  void shouldReturnEmptyBoxValues_whenResponseIsNullOrHasNoBoxValues(StyleBoxes smsResponse) {
    PortfolioHolding holding = createHolding("TEST.ID", FinancialInstrumentType.FUND);

    EquityStyleboxExposure result = mapper.map(smsResponse, holding);

    assertThat(result.getBoxValues()).isEmpty();
    assertThat(result.getHoldingType()).isEqualTo(FinancialInstrumentType.FUND);
    assertThat(result.getProviders()).isEmpty();
  }

  static Stream<Arguments> nullAndEmptyResponses() {
    var nullBoxValuesResponse = new StyleBoxes();
    nullBoxValuesResponse.setBoxValues(null);

    var emptyBoxValuesResponse = new StyleBoxes();
    emptyBoxValuesResponse.setBoxValues(List.of());

    return Stream.of(
        Arguments.of((StyleBoxes) null),
        Arguments.of(nullBoxValuesResponse),
        Arguments.of(emptyBoxValuesResponse));
  }

  @Test
  void shouldFilterOutEntriesWithNullTypeOrValue() {
    var validEntry = new StyleBoxValue(StyleBoxType.SMALL_CORE, new BigDecimal("1.5"));
    var nullTypeEntry = new StyleBoxValue(null, BigDecimal.valueOf(5.0));
    var nullValueEntry = new StyleBoxValue(StyleBoxType.SMALL_GROWTH, null);

    var styleBoxes = new StyleBoxes();
    styleBoxes.setBoxValues(List.of(validEntry, nullTypeEntry, nullValueEntry));

    EquityStyleboxExposure result = mapper.map(styleBoxes, createHolding("TEST.ID", FinancialInstrumentType.ETF));

    assertThat(result.getBoxValues()).hasSize(1);
    assertThat(result.getBoxValues()).containsKey(StyleBoxType.SMALL_CORE);
  }

  @Test
  void shouldMapAllNineStyleboxTypes() {
    List<StyleBoxValue> entries = List.of(
        new StyleBoxValue(StyleBoxType.LARGE_VALUE, new BigDecimal("15.0")),
        new StyleBoxValue(StyleBoxType.LARGE_CORE, new BigDecimal("35.0")),
        new StyleBoxValue(StyleBoxType.LARGE_GROWTH, new BigDecimal("20.0")),
        new StyleBoxValue(StyleBoxType.MID_VALUE, new BigDecimal("5.0")),
        new StyleBoxValue(StyleBoxType.MID_CORE, new BigDecimal("10.0")),
        new StyleBoxValue(StyleBoxType.MID_GROWTH, new BigDecimal("8.0")),
        new StyleBoxValue(StyleBoxType.SMALL_VALUE, new BigDecimal("3.0")),
        new StyleBoxValue(StyleBoxType.SMALL_CORE, new BigDecimal("2.5")),
        new StyleBoxValue(StyleBoxType.SMALL_GROWTH, new BigDecimal("1.5")));

    var styleBoxes = new StyleBoxes();
    styleBoxes.setBoxValues(entries);

    EquityStyleboxExposure result = mapper.map(styleBoxes, createHolding("FULL.TEST", FinancialInstrumentType.ETF));

    assertThat(result.getBoxValues()).hasSize(9);
    assertThat(result.getBoxValues().get(StyleBoxType.LARGE_VALUE)).isEqualByComparingTo("15.0");
    assertThat(result.getBoxValues().get(StyleBoxType.LARGE_CORE)).isEqualByComparingTo("35.0");
    assertThat(result.getBoxValues().get(StyleBoxType.LARGE_GROWTH)).isEqualByComparingTo("20.0");
    assertThat(result.getBoxValues().get(StyleBoxType.MID_VALUE)).isEqualByComparingTo("5.0");
    assertThat(result.getBoxValues().get(StyleBoxType.MID_CORE)).isEqualByComparingTo("10.0");
    assertThat(result.getBoxValues().get(StyleBoxType.MID_GROWTH)).isEqualByComparingTo("8.0");
    assertThat(result.getBoxValues().get(StyleBoxType.SMALL_VALUE)).isEqualByComparingTo("3.0");
    assertThat(result.getBoxValues().get(StyleBoxType.SMALL_CORE)).isEqualByComparingTo("2.5");
    assertThat(result.getBoxValues().get(StyleBoxType.SMALL_GROWTH)).isEqualByComparingTo("1.5");
  }

  private PortfolioHolding createHolding(String securityId, FinancialInstrumentType holdingType) {
    var identifier = new SecurityIdentifier();
    identifier.setId(securityId);
    return new PortfolioHolding(null, holdingType, identifier);
  }
}
