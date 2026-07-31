package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.ErrorParams;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.performance.MonthlyReturns;
import com.fintex.wm.commons.domain.value.DateBigDecimalValue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonthlyReturnsMapperTest {

  private final MonthlyReturnsMapper mapper = new MonthlyReturnsMapper();

  @Test
  void shouldMapReturnsAndProvider_whenResponseHasDateValues() {
    var smsResponse = new MonthlyReturns();
    smsResponse.setReturns(List.of(
        createDateValue("2025-01-31", "0.0125"),
        createDateValue("2025-02-28", "-0.0080")));
    smsResponse.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    HoldingMonthlyReturns result = mapper.map(smsResponse, createHolding("SEC-001"));

    assertThat(result.getHoldingType()).isEqualTo(FinancialInstrumentType.ETF);
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
    assertThat(result.getReturns()).hasSize(2);
    assertThat(result.getReturns().get(LocalDate.of(2025, 1, 31)))
        .isEqualByComparingTo("0.0125");
    assertThat(result.getReturns().get(LocalDate.of(2025, 2, 28)))
        .isEqualByComparingTo("-0.0080");
  }

  @Test
  void shouldReturnEmptyReturns_whenResponseIsNull() {
    HoldingMonthlyReturns result = mapper.map(null, createHolding("SEC-002"));

    assertThat(result.getProviders()).isEmpty();
    assertThat(result.getReturns()).isEmpty();
  }

  @Test
  void shouldReturnEmptyReturns_whenReturnsListIsNull() {
    var smsResponse = new MonthlyReturns();
    smsResponse.setReturns(null);

    HoldingMonthlyReturns result = mapper.map(smsResponse, createHolding("SEC-003"));

    assertThat(result.getReturns()).isEmpty();
    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldNotSetProvider_whenDataProviderIsNull() {
    var smsResponse = new MonthlyReturns();
    smsResponse.setReturns(List.of());
    smsResponse.setDataProviders(null);

    HoldingMonthlyReturns result = mapper.map(smsResponse, createHolding("SEC-004"));

    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldFilterOutEntry_whenDateIsNull() {
    var valid = createDateValue("2025-03-31", "0.0200");

    var nullDate = new DateBigDecimalValue();
    nullDate.setDate(null);
    nullDate.setValue(BigDecimal.valueOf(0.01));

    var smsResponse = new MonthlyReturns();
    smsResponse.setReturns(List.of(valid, nullDate));

    HoldingMonthlyReturns result = mapper.map(smsResponse, createHolding("SEC-005"));

    assertThat(result.getReturns())
        .containsOnlyKeys(LocalDate.of(2025, 3, 31))
        .containsEntry(LocalDate.of(2025, 3, 31), new BigDecimal("0.0200"));
  }

  @Test
  void shouldThrowMissingMonthlyReturnForDate_whenMultipleEntryValuesAreNull() {
    var smsResponse = new MonthlyReturns();
    smsResponse.setReturns(List.of(
        createDateValueWithNullValue("2025-12-01"),
        createDateValueWithNullValue("2025-04-01"),
        createDateValueWithNullValue("2025-01-01")));
    PortfolioHolding holding = createHolding("SEC-006");

    assertThatThrownBy(() -> mapper.map(smsResponse, holding))
        .isInstanceOfSatisfying(CalculationException.class, exception -> {
          assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSING_MONTHLY_RETURN_FOR_DATE);
          assertThat(exception).hasMessage(
              "The holding is missing monthly return values for date 2025-01-31, 2025-04-30, 2025-12-31");
          assertThat(exception.getMetadata())
              .containsOnlyKeys(ErrorParams.HOLDING_ID, "param-1")
              .containsEntry(ErrorParams.HOLDING_ID, ErrorParams.holdingId(holding))
              .containsEntry("param-1", "2025-01-31, 2025-04-30, 2025-12-31");
        });
  }

  @Test
  void shouldKeepFirstValue_whenDuplicateDatesExist() {
    var smsResponse = new MonthlyReturns();
    smsResponse.setReturns(List.of(
        createDateValue("2025-01-31", "0.0100"),
        createDateValue("2025-01-31", "0.0200")));

    HoldingMonthlyReturns result = mapper.map(smsResponse, createHolding("SEC-007"));

    assertThat(result.getReturns()).hasSize(1);
    assertThat(result.getReturns().get(LocalDate.of(2025, 1, 31)))
        .isEqualByComparingTo("0.0100");
  }

  @Test
  void shouldReturnSortedTreeMap() {
    var smsResponse = new MonthlyReturns();
    smsResponse.setReturns(List.of(
        createDateValue("2025-03-31", "0.03"),
        createDateValue("2025-01-31", "0.01"),
        createDateValue("2025-02-28", "0.02")));

    HoldingMonthlyReturns result = mapper.map(smsResponse, createHolding("SEC-008"));

    assertThat(result.getReturns().firstKey()).isEqualTo(LocalDate.of(2025, 1, 31));
    assertThat(result.getReturns().lastKey()).isEqualTo(LocalDate.of(2025, 3, 31));
  }

  @Test
  void shouldNormalizeFirstOfMonthDatesToLastOfMonth() {
    var smsResponse = new MonthlyReturns();
    smsResponse.setReturns(List.of(
        createDateValue("2024-12-01", "-5.28348"),
        createDateValue("2025-01-01", "3.60137"),
        createDateValue("2025-02-01", "-2.02298")));

    HoldingMonthlyReturns result = mapper.map(smsResponse, createHolding("SEC-009"));

    assertThat(result.getReturns()).containsOnlyKeys(
        LocalDate.of(2024, 12, 31),
        LocalDate.of(2025, 1, 31),
        LocalDate.of(2025, 2, 28));
  }

  @Test
  void shouldKeepFirstValue_whenMidMonthEntriesCollideAfterNormalization() {
    var smsResponse = new MonthlyReturns();
    smsResponse.setReturns(List.of(
        createDateValue("2025-01-01", "0.0100"),
        createDateValue("2025-01-15", "0.0200")));

    HoldingMonthlyReturns result = mapper.map(smsResponse, createHolding("SEC-010"));

    assertThat(result.getReturns()).hasSize(1);
    assertThat(result.getReturns().get(LocalDate.of(2025, 1, 31)))
        .isEqualByComparingTo("0.0100");
  }

  @Test
  void shouldThrowCountryNotSupported_whenCountryHasNoCurrencyMapping() {
    var smsResponse = new MonthlyReturns();
    smsResponse.setReturns(List.of());
    PortfolioHolding ukHolding = new PortfolioHolding(null, FinancialInstrumentType.ETF, Country.UNITED_KINGDOM,
        new SecurityIdentifier("SEC-UK", null));

    assertThatThrownBy(() -> mapper.map(smsResponse, ukHolding))
        .isInstanceOf(CalculationException.class)
        .extracting(ex -> ((CalculationException) ex).getErrorCode())
        .isEqualTo(ErrorCode.COUNTRY_NOT_SUPPORTED);
  }

  private DateBigDecimalValue createDateValue(String date, String value) {
    var dv = new DateBigDecimalValue();
    dv.setDate(date);
    dv.setValue(new BigDecimal(value));
    return dv;
  }

  private DateBigDecimalValue createDateValueWithNullValue(String date) {
    var value = new DateBigDecimalValue();
    value.setDate(date);
    value.setValue(null);
    return value;
  }

  private PortfolioHolding createHolding(String securityId) {
    return new PortfolioHolding(null, FinancialInstrumentType.ETF, Country.CANADA,
        new SecurityIdentifier(securityId, null));
  }
}
