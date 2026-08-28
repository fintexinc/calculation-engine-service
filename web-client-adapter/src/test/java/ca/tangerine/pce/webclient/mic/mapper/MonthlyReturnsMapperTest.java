package ca.tangerine.pce.webclient.mic.mapper;

import ca.tangerine.pce.model.domain.calculation.returns.HoldingMonthlyReturns;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.ErrorParams;
import ca.tangerine.pce.model.error.exceptions.CalculationException;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;
import ca.tangerine.wm.commons.domain.performance.MonthlyReturns;
import ca.tangerine.wm.commons.domain.value.DateBigDecimalValue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holding;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonthlyReturnsMapperTest {

  private final MonthlyReturnsMapper mapper = new MonthlyReturnsMapper();

  @Test
  void shouldMapReturnsAndProvider_whenResponseHasDateValues() {
    var micResponse = new MonthlyReturns();
    micResponse.setReturns(List.of(
        createDateValue("2025-01-31", "0.0125"),
        createDateValue("2025-02-28", "-0.0080")));
    micResponse.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    HoldingMonthlyReturns result = mapper.map(micResponse, holding(new SecurityIdentifier("SEC-001", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

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
    HoldingMonthlyReturns result = mapper.map(null, holding(new SecurityIdentifier("SEC-002", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getProviders()).isEmpty();
    assertThat(result.getReturns()).isEmpty();
  }

  @Test
  void shouldReturnEmptyReturns_whenReturnsListIsNull() {
    var micResponse = new MonthlyReturns();
    micResponse.setReturns(null);

    HoldingMonthlyReturns result = mapper.map(micResponse, holding(new SecurityIdentifier("SEC-003", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getReturns()).isEmpty();
    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldNotSetProvider_whenDataProviderIsNull() {
    var micResponse = new MonthlyReturns();
    micResponse.setReturns(List.of());
    micResponse.setDataProviders(null);

    HoldingMonthlyReturns result = mapper.map(micResponse, holding(new SecurityIdentifier("SEC-004", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldFilterOutEntry_whenDateIsNull() {
    var valid = createDateValue("2025-03-31", "0.0200");

    var nullDate = new DateBigDecimalValue();
    nullDate.setDate(null);
    nullDate.setValue(BigDecimal.valueOf(0.01));

    var micResponse = new MonthlyReturns();
    micResponse.setReturns(List.of(valid, nullDate));

    HoldingMonthlyReturns result = mapper.map(micResponse, holding(new SecurityIdentifier("SEC-005", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getReturns())
        .containsOnlyKeys(LocalDate.of(2025, 3, 31))
        .containsEntry(LocalDate.of(2025, 3, 31), new BigDecimal("0.0200"));
  }

  @Test
  void shouldThrowMissingMonthlyReturnForDate_whenMultipleEntryValuesAreNull() {
    var micResponse = new MonthlyReturns();
    micResponse.setReturns(List.of(
        createDateValueWithNullValue("2025-12-01"),
        createDateValueWithNullValue("2025-04-01"),
        createDateValueWithNullValue("2025-01-01")));
    PortfolioHolding holding = holding(new SecurityIdentifier("SEC-006", null), FinancialInstrumentType.ETF,
        Country.CANADA, (BigDecimal) null);

    assertThatThrownBy(() -> mapper.map(micResponse, holding))
        .isInstanceOfSatisfying(CalculationException.class, exception -> {
          assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSING_MONTHLY_RETURN_FOR_DATE);
          assertThat(exception).hasMessage(
              "The holding ETF-SEC-006 is missing monthly return values for date 2025-01-31, 2025-04-30, 2025-12-31");
          assertThat(exception.getMetadata())
              .containsOnlyKeys(ErrorParams.HOLDING_ID, "param-1", "param-2")
              .containsEntry(ErrorParams.HOLDING_ID, ErrorParams.holdingId(holding))
              .containsEntry("param-1", ErrorParams.holdingId(holding))
              .containsEntry("param-2", "2025-01-31, 2025-04-30, 2025-12-31");
        });
  }

  @Test
  void shouldKeepFirstValue_whenDuplicateDatesExist() {
    var micResponse = new MonthlyReturns();
    micResponse.setReturns(List.of(
        createDateValue("2025-01-31", "0.0100"),
        createDateValue("2025-01-31", "0.0200")));

    HoldingMonthlyReturns result = mapper.map(micResponse, holding(new SecurityIdentifier("SEC-007", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getReturns()).hasSize(1);
    assertThat(result.getReturns().get(LocalDate.of(2025, 1, 31)))
        .isEqualByComparingTo("0.0100");
  }

  @Test
  void shouldReturnSortedTreeMap() {
    var micResponse = new MonthlyReturns();
    micResponse.setReturns(List.of(
        createDateValue("2025-03-31", "0.03"),
        createDateValue("2025-01-31", "0.01"),
        createDateValue("2025-02-28", "0.02")));

    HoldingMonthlyReturns result = mapper.map(micResponse, holding(new SecurityIdentifier("SEC-008", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getReturns().firstKey()).isEqualTo(LocalDate.of(2025, 1, 31));
    assertThat(result.getReturns().lastKey()).isEqualTo(LocalDate.of(2025, 3, 31));
  }

  @Test
  void shouldNormalizeFirstOfMonthDatesToLastOfMonth() {
    var micResponse = new MonthlyReturns();
    micResponse.setReturns(List.of(
        createDateValue("2024-12-01", "-5.28348"),
        createDateValue("2025-01-01", "3.60137"),
        createDateValue("2025-02-01", "-2.02298")));

    HoldingMonthlyReturns result = mapper.map(micResponse, holding(new SecurityIdentifier("SEC-009", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getReturns()).containsOnlyKeys(
        LocalDate.of(2024, 12, 31),
        LocalDate.of(2025, 1, 31),
        LocalDate.of(2025, 2, 28));
  }

  @Test
  void shouldKeepFirstValue_whenMidMonthEntriesCollideAfterNormalization() {
    var micResponse = new MonthlyReturns();
    micResponse.setReturns(List.of(
        createDateValue("2025-01-01", "0.0100"),
        createDateValue("2025-01-15", "0.0200")));

    HoldingMonthlyReturns result = mapper.map(micResponse, holding(new SecurityIdentifier("SEC-010", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getReturns()).hasSize(1);
    assertThat(result.getReturns().get(LocalDate.of(2025, 1, 31)))
        .isEqualByComparingTo("0.0100");
  }

  @Test
  void shouldThrowCountryNotSupported_whenCountryHasNoCurrencyMapping() {
    var micResponse = new MonthlyReturns();
    micResponse.setReturns(List.of());
    PortfolioHolding ukHolding = holding(new SecurityIdentifier("SEC-UK", null),
        FinancialInstrumentType.ETF, Country.UNITED_KINGDOM, (BigDecimal) null);

    assertThatThrownBy(() -> mapper.map(micResponse, ukHolding))
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

}
