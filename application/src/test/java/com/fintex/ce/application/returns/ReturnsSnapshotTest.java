package com.fintex.ce.application.returns;

import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.BasePceException;
import com.fintex.ce.model.error.exceptions.CalculationsFailedException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.error.Notification;
import com.fintex.wm.commons.error.Severity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holdingWithoutCountry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReturnsSnapshotTest {

  private static final PortfolioHolding HOLDING_USD = holdingWithoutCountry(
      new SecurityIdentifier("USD-A", FiIdentifierType.TICKER), null, null);
  private static final PortfolioHolding HOLDING_CAD = holdingWithoutCountry(
      new SecurityIdentifier("CAD-B", FiIdentifierType.TICKER), null, null);
  private static final PortfolioHolding HOLDING_BAD = holdingWithoutCountry(
      new SecurityIdentifier("BAD-C", FiIdentifierType.TICKER), null, null);

  @Test
  void shouldReturnEmptySnapshotWithImmutableMaps_whenEmpty() {
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = ReturnsSnapshot.empty();

    assertThat(snapshot.holdingCurrencyMap()).isEmpty();
    assertThat(snapshot.returnsMap()).isEmpty();
    assertThat(snapshot.errors()).isEmpty();
    assertThat(snapshot.performanceStartDate()).isNull();
    assertThat(snapshot.performanceEndDate()).isNull();
  }

  @Test
  void shouldBuildPopulatedSnapshot_whenForMonthlyReturns() {
    Map<PortfolioHolding, HoldingMonthlyReturns> sourceData = Map.of(
        HOLDING_USD, holdingMonthlyReturns(Currency.USD.name(),
            entry(LocalDate.parse("2020-01-31"), BigDecimal.valueOf(0.01)),
            entry(LocalDate.parse("2020-02-29"), BigDecimal.valueOf(0.02))),
        HOLDING_CAD, holdingMonthlyReturns(Currency.CAD.name(),
            entry(LocalDate.parse("2020-01-31"), BigDecimal.valueOf(0.03)),
            entry(LocalDate.parse("2020-02-29"), BigDecimal.valueOf(0.04))));

    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = ReturnsSnapshot.forMonthlyReturns(sourceData);

    assertThat(snapshot.holdingCurrencyMap()).containsEntry(HOLDING_USD, Currency.USD);
    assertThat(snapshot.holdingCurrencyMap()).containsEntry(HOLDING_CAD, Currency.CAD);
    assertThat(snapshot.returnsMap()).hasSize(2);
    assertThat(snapshot.performanceStartDate()).isEqualTo(LocalDate.parse("2020-01-31"));
    assertThat(snapshot.performanceEndDate()).isEqualTo(LocalDate.parse("2020-02-29"));
    assertThat(snapshot.errors()).isEmpty();
  }

  @Test
  void shouldRecordMissingCurrencyError_whenForMonthlyReturnsAndCurrencyInvalid() {
    Map<PortfolioHolding, HoldingMonthlyReturns> sourceData = Map.of(
        HOLDING_BAD, holdingMonthlyReturns("XYZ-INVALID",
            entry(LocalDate.parse("2020-01-31"), BigDecimal.valueOf(0.01))));

    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = ReturnsSnapshot.forMonthlyReturns(sourceData);

    assertThat(snapshot.errors())
        .extracting(BasePceException::getErrorCode)
        .containsExactly(ErrorCode.HOLDING_MISSING_CURRENCY_FROM_FDS);
    assertThat(snapshot.holdingCurrencyMap()).isEmpty();
  }

  @Test
  void shouldOnlyTranslateMonthlyReturnsErrors_whenValidateOnly() {
    HoldingMonthlyReturns ok = holdingMonthlyReturns(Currency.USD.name(),
        entry(LocalDate.parse("2020-01-31"), BigDecimal.valueOf(0.01)));
    HoldingMonthlyReturns broken = holdingMonthlyReturns(Currency.CAD.name(),
        entry(LocalDate.parse("2020-01-31"), BigDecimal.valueOf(0.02)));
    broken.addError(ErrorCode.MISSING_MONTHLY_RETURNS.toNotificationForHolding(HOLDING_CAD));

    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = ReturnsSnapshot.validateOnly(Map.of(
        HOLDING_USD, ok,
        HOLDING_CAD, broken));

    assertThat(snapshot.holdingCurrencyMap()).isEmpty();
    assertThat(snapshot.errors())
        .extracting(BasePceException::getErrorCode)
        .containsExactly(ErrorCode.MISSING_MONTHLY_RETURNS);
  }

  @Test
  void shouldThrow_whenForMonthlyReturnsContainsFatalBeforeStartDateError() {
    HoldingMonthlyReturns broken = holdingMonthlyReturns(Currency.USD.name(),
        entry(LocalDate.parse("2020-01-31"), BigDecimal.valueOf(0.01)));
    broken.addError(ErrorCode.CPED_BEFORE_PORTFOLIO_PSD.toNotification());

    assertThatThrownBy(() -> ReturnsSnapshot.forMonthlyReturns(Map.of(HOLDING_USD, broken)))
        .isInstanceOf(CalculationsFailedException.class);
  }

  @Test
  void shouldReplaceReturnsMap_whenWithReturnsMap() {
    ReturnsSnapshot<HoldingMonthlyReturns> base = baseSnapshot();
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> updated = Map.of(HOLDING_CAD,
        treeMap(entry(LocalDate.parse("2021-12-31"), BigDecimal.TEN)));

    ReturnsSnapshot<HoldingMonthlyReturns> next = base.withReturnsMap(updated);

    assertThat(next.returnsMap()).containsOnlyKeys(HOLDING_CAD);
    assertThat(next.holdingCurrencyMap()).isEqualTo(base.holdingCurrencyMap());
    assertThat(next.performanceStartDate()).isEqualTo(base.performanceStartDate());
    assertThat(next.performanceEndDate()).isEqualTo(base.performanceEndDate());
    assertThat(next.errors()).isEqualTo(base.errors());
  }

  @Test
  void shouldReplaceCurrencyMap_whenWithHoldingCurrencyMap() {
    ReturnsSnapshot<HoldingMonthlyReturns> base = baseSnapshot();
    Map<PortfolioHolding, Currency> updated = Map.of(HOLDING_USD, Currency.EUR);

    ReturnsSnapshot<HoldingMonthlyReturns> next = base.withHoldingCurrencyMap(updated);

    assertThat(next.holdingCurrencyMap()).containsExactlyEntriesOf(updated);
    assertThat(next.returnsMap()).isEqualTo(base.returnsMap());
  }

  @Test
  void shouldReplacePeriod_whenWithPeriod() {
    ReturnsSnapshot<HoldingMonthlyReturns> base = baseSnapshot();
    LocalDate newPsd = LocalDate.parse("2019-01-31");
    LocalDate newPed = LocalDate.parse("2024-01-31");

    ReturnsSnapshot<HoldingMonthlyReturns> next = base.withPeriod(newPsd, newPed);

    assertThat(next.performanceStartDate()).isEqualTo(newPsd);
    assertThat(next.performanceEndDate()).isEqualTo(newPed);
  }

  @Test
  void shouldTrimReturnsAndRecalculatePerformanceWindow_whenTrimToStart() {
    LocalDate january = LocalDate.parse("2020-01-31");
    LocalDate february = LocalDate.parse("2020-02-29");
    LocalDate march = LocalDate.parse("2020-03-31");
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = new ReturnsSnapshot<>(Map.of(),
        Map.of(HOLDING_USD, treeMap(entry(january, BigDecimal.ONE), entry(february, BigDecimal.TEN),
            entry(march, BigDecimal.valueOf(100)))),
        january, march, List.of());

    ReturnsSnapshot<HoldingMonthlyReturns> result = snapshot.trimToStart(february);

    assertThat(result.performanceStartDate()).isEqualTo(february);
    assertThat(result.performanceEndDate()).isEqualTo(march);
    assertThat(result.returnsMap().get(HOLDING_USD)).containsOnlyKeys(february, march);
  }

  @Test
  void shouldTrimPreWindowReturns_whenTrimToStartMatchesPerformanceStartDate() {
    LocalDate january = LocalDate.parse("2020-01-31");
    LocalDate february = LocalDate.parse("2020-02-29");
    LocalDate march = LocalDate.parse("2020-03-31");
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = new ReturnsSnapshot<>(Map.of(),
        Map.of(HOLDING_USD, treeMap(entry(january, BigDecimal.ONE), entry(february, BigDecimal.TEN),
            entry(march, BigDecimal.valueOf(100))),
            HOLDING_CAD, treeMap(entry(february, BigDecimal.valueOf(2)), entry(march, BigDecimal.valueOf(3)))),
        february, march, List.of());

    ReturnsSnapshot<HoldingMonthlyReturns> result = snapshot.trimToStart(february);

    assertThat(result.performanceStartDate()).isEqualTo(february);
    assertThat(result.performanceEndDate()).isEqualTo(march);
    assertThat(result.returnsMap().get(HOLDING_USD)).containsExactlyEntriesOf(treeMap(
        entry(february, BigDecimal.TEN), entry(march, BigDecimal.valueOf(100))));
    assertThat(result.returnsMap().get(HOLDING_CAD)).containsExactlyEntriesOf(treeMap(
        entry(february, BigDecimal.valueOf(2)), entry(march, BigDecimal.valueOf(3))));
  }

  @Test
  void shouldReturnSameInstance_whenWithAddedErrorsCalledWithEmptyList() {
    ReturnsSnapshot<HoldingMonthlyReturns> base = baseSnapshot();

    ReturnsSnapshot<HoldingMonthlyReturns> next = base.withAddedErrors(List.of());

    assertThat(next).isSameAs(base);
  }

  @Test
  void shouldAppendErrors_whenWithAddedErrorsCalledWithNonEmptyList() {
    ReturnsSnapshot<HoldingMonthlyReturns> base = baseSnapshot();
    BasePceException existing = ErrorCode.HOLDING_PSD_OUT_OF_RANGE.toExceptionForId("e0");
    base = base.withErrors(List.of(existing));
    BasePceException added = ErrorCode.MISSING_MONTHLY_RETURNS.toExceptionForId("e1");

    ReturnsSnapshot<HoldingMonthlyReturns> next = base.withAddedErrors(List.of(added));

    assertThat(next.errors()).containsExactly(existing, added);
  }

  @Test
  void shouldDeepCopyReturnsMap_whenSnapshotIsConstructed() {
    TreeMap<LocalDate, BigDecimal> originalSeries = treeMap(
        entry(LocalDate.parse("2020-01-31"), BigDecimal.ONE));
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> source = new HashMap<>();
    source.put(HOLDING_USD, originalSeries);
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = new ReturnsSnapshot<>(Map.of(), source, null, null, List.of());

    originalSeries.put(LocalDate.parse("2020-02-29"), BigDecimal.TEN);

    assertThat(snapshot.returnsMap().get(HOLDING_USD)).hasSize(1);
  }

  @Test
  void shouldTranslateEndDateAfterPerformanceToWarning_whenGetErrorsAsWarnings() {
    BasePceException error = ErrorCode.CPED_AFTER_PORTFOLIO_PED.toException();
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = ReturnsSnapshot.<HoldingMonthlyReturns>empty()
        .withErrors(List.of(error));

    List<Notification> warnings = snapshot.getErrorsAsWarnings();

    assertThat(warnings).hasSize(1);
    Notification warning = warnings.getFirst();
    assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.CPED_AFTER_PORTFOLIO_PED);
    assertThat(warning.getSeverity()).isEqualTo(Severity.WARNING);
  }

  @Test
  void shouldDropHoldingStartingAfterEarliestEnd_whenForMonthlyReturnsHasOutOfRangePsdHolding() {
    Map<PortfolioHolding, HoldingMonthlyReturns> sourceData = new LinkedHashMap<>();
    sourceData.put(HOLDING_USD, holdingMonthlyReturns(Currency.USD.name(),
        entry(LocalDate.parse("2024-12-31"), BigDecimal.ONE)));
    sourceData.put(HOLDING_CAD, holdingMonthlyReturns(Currency.CAD.name(),
        entry(LocalDate.parse("2020-01-31"), BigDecimal.ONE)));

    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = ReturnsSnapshot.forMonthlyReturns(sourceData);

    assertThat(snapshot.errors())
        .extracting(BasePceException::getErrorCode)
        .contains(ErrorCode.HOLDING_PSD_OUT_OF_RANGE);
    assertThat(snapshot.returnsMap()).containsOnlyKeys(HOLDING_CAD);
  }

  private ReturnsSnapshot<HoldingMonthlyReturns> baseSnapshot() {
    Map<PortfolioHolding, Currency> currencies = Map.of(HOLDING_USD, Currency.USD);
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = Map.of(HOLDING_USD,
        treeMap(entry(LocalDate.parse("2020-01-31"), BigDecimal.ONE)));
    return new ReturnsSnapshot<>(currencies, returns, LocalDate.parse("2020-01-31"),
        LocalDate.parse("2020-01-31"), List.of());
  }

  @SafeVarargs
  private static HoldingMonthlyReturns holdingMonthlyReturns(String currency,
      Map.Entry<LocalDate, BigDecimal>... entries) {
    HoldingMonthlyReturns data = new HoldingMonthlyReturns();
    data.setCurrency(currency);
    data.setReturns(treeMap(entries));
    return data;
  }

  @SafeVarargs
  private static TreeMap<LocalDate, BigDecimal> treeMap(Map.Entry<LocalDate, BigDecimal>... entries) {
    TreeMap<LocalDate, BigDecimal> map = new TreeMap<>();
    for (Map.Entry<LocalDate, BigDecimal> entry : entries) {
      map.put(entry.getKey(), entry.getValue());
    }
    return map;
  }

  private static Map.Entry<LocalDate, BigDecimal> entry(LocalDate date, BigDecimal value) {
    return Map.entry(date, value);
  }
}
