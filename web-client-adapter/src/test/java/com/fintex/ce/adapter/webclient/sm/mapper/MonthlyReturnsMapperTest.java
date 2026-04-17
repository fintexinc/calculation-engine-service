package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.performance.MonthlyReturns;
import com.fintex.wm.commons.domain.value.DateBigDecimalValue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyReturnsMapperTest {

  private final MonthlyReturnsMapper mapper = new MonthlyReturnsMapper();

  @Test
  void shouldMapReturnsAndProvider_whenResponseHasDateValues() {
    var smsResponse = new MonthlyReturns();
    smsResponse.setReturns(List.of(
        createDateValue("2025-01-31", "0.0125"),
        createDateValue("2025-02-28", "-0.0080")));
    smsResponse.setDataProvider(DataProvider.MORNINGSTAR);

    HoldingMonthlyReturns result = mapper.map(smsResponse, createHolding("SEC-001"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-001");
    assertThat(result.getHoldingType()).isEqualTo(FinancialInstrumentType.ETF_CANADA);
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

    assertThat(result.getHoldingId()).isEqualTo("SEC-002");
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
    smsResponse.setDataProvider(null);

    HoldingMonthlyReturns result = mapper.map(smsResponse, createHolding("SEC-004"));

    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldFilterOutEntriesWithNullDateOrValue() {
    var valid = createDateValue("2025-03-31", "0.0200");

    var nullDate = new DateBigDecimalValue();
    nullDate.setDate(null);
    nullDate.setValue(BigDecimal.valueOf(0.01));

    var nullValue = new DateBigDecimalValue();
    nullValue.setDate("2025-04-30");
    nullValue.setValue(null);

    var smsResponse = new MonthlyReturns();
    smsResponse.setReturns(List.of(valid, nullDate, nullValue));

    HoldingMonthlyReturns result = mapper.map(smsResponse, createHolding("SEC-005"));

    assertThat(result.getReturns()).hasSize(1);
    assertThat(result.getReturns()).containsKey(LocalDate.of(2025, 3, 31));
  }

  @Test
  void shouldKeepFirstValue_whenDuplicateDatesExist() {
    var smsResponse = new MonthlyReturns();
    smsResponse.setReturns(List.of(
        createDateValue("2025-01-31", "0.0100"),
        createDateValue("2025-01-31", "0.0200")));

    HoldingMonthlyReturns result = mapper.map(smsResponse, createHolding("SEC-006"));

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

    HoldingMonthlyReturns result = mapper.map(smsResponse, createHolding("SEC-007"));

    assertThat(result.getReturns().firstKey()).isEqualTo(LocalDate.of(2025, 1, 31));
    assertThat(result.getReturns().lastKey()).isEqualTo(LocalDate.of(2025, 3, 31));
  }

  private DateBigDecimalValue createDateValue(String date, String value) {
    var dv = new DateBigDecimalValue();
    dv.setDate(date);
    dv.setValue(new BigDecimal(value));
    return dv;
  }

  private PortfolioHolding createHolding(String securityId) {
    return new PortfolioHolding(null, FinancialInstrumentType.ETF_CANADA, new SecurityIdentifier(securityId, null));
  }
}
