package com.fintex.ce.application.returns.processor;

import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.returns.FxContext;
import com.fintex.ce.application.returns.ProcessingContext;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.BasePceException;
import com.fintex.ce.model.error.exceptions.CalculationsFailedException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.error.Notification;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FxConversionProcessorTest {

  private static final PortfolioHolding HOLDING_USD = new PortfolioHolding(null, null,
      new SecurityIdentifier("USD-A", FiIdentifierType.TICKER));
  private static final PortfolioHolding HOLDING_EUR = new PortfolioHolding(null, null,
      new SecurityIdentifier("EUR-A", FiIdentifierType.TICKER));
  private static final LocalDate JAN = LocalDate.parse("2020-01-31");

  private final FxRateService fxRateService = mock(FxRateService.class);
  private final FxConversionProcessor processor = new FxConversionProcessor(fxRateService);

  @Test
  void shouldShortCircuit_whenTargetCurrencyIsNull() {
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = snapshot(Map.of(HOLDING_USD, Currency.USD));
    ProcessingContext context = ProcessingContext.of(null, null, FxContext.empty());

    ReturnsSnapshot<HoldingMonthlyReturns> result = processor.process(snapshot, context);

    assertThat(result).isSameAs(snapshot);
    verifyNoInteractions(fxRateService);
  }

  @Test
  void shouldThrow_whenSnapshotHasFatalError() {
    BasePceException fatal = ErrorCode.CPED_AFTER_PORTFOLIO_PED.toExceptionForId("fatal");
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = snapshot(Map.of(HOLDING_USD, Currency.USD))
        .withErrors(List.of(fatal));
    ProcessingContext context = ProcessingContext.of(null, null,
        new FxContext(Map.of(), Currency.CAD));

    assertThatThrownBy(() -> processor.process(snapshot, context))
        .isInstanceOf(CalculationsFailedException.class);
    verify(fxRateService, never()).convertReturns(any(), any(), any(), any(), any());
  }

  @Test
  void shouldReplaceReturnsAndUpdateCurrencyMap_whenConversionSucceeds() {
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = snapshot(Map.of(HOLDING_USD, Currency.USD));
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> converted = Map.of(HOLDING_USD,
        treeMap(Map.entry(JAN, BigDecimal.valueOf(1.5))));
    when(fxRateService.convertReturns(any(), any(), any(), any(), any())).thenReturn(converted);

    ProcessingContext context = ProcessingContext.of(null, null, new FxContext(Map.of(), Currency.CAD));

    ReturnsSnapshot<HoldingMonthlyReturns> result = processor.process(snapshot, context);

    assertThat(result.returnsMap()).isEqualTo(converted);
    assertThat(result.holdingCurrencyMap()).containsEntry(HOLDING_USD, Currency.CAD);
    assertThat(result.errors()).isEmpty();
    assertThat(result.warnings()).isEmpty();
  }

  @Test
  void shouldFoldWarningsAndReturnPartialConversion_whenSomeHoldingDatesUnavailable() {
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = snapshot(Map.of(
        HOLDING_USD, Currency.USD,
        HOLDING_EUR, Currency.EUR));
    when(fxRateService.convertReturns(any(), any(), any(), any(), any())).thenAnswer(invocation -> {
      List<Notification> warnings = invocation.getArgument(4);
      warnings.add(ErrorCode.FX_RATES_UNAVAILABLE.toNotificationForHolding(HOLDING_EUR,
          Currency.EUR, Currency.CAD));
      return Map.of(
          HOLDING_USD, treeMap(Map.entry(JAN, BigDecimal.valueOf(1.5))),
          HOLDING_EUR, new TreeMap<LocalDate, BigDecimal>());
    });

    ProcessingContext context = ProcessingContext.of(null, null,
        new FxContext(Map.of(new CurrencyExchangePair(Currency.USD, Currency.CAD), navigable()), Currency.CAD));

    ReturnsSnapshot<HoldingMonthlyReturns> result = processor.process(snapshot, context);

    assertThat(result.errors()).isEmpty();
    assertThat(result.warnings()).hasSize(1);
    assertThat(result.warnings().getFirst().getCode())
        .isEqualTo(ErrorCode.FX_RATES_UNAVAILABLE.getCode());
    assertThat(result.returnsMap().get(HOLDING_USD)).containsOnlyKeys(JAN);
    assertThat(result.returnsMap().get(HOLDING_EUR)).isEmpty();
  }

  @Test
  void shouldKeepHoldingsAlreadyInTargetCurrency_whenSomeHoldingsMatchTarget() {
    PortfolioHolding cadHolding = new PortfolioHolding(null, null,
        new SecurityIdentifier("CAD-A", FiIdentifierType.TICKER));
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = snapshot(Map.of(
        HOLDING_USD, Currency.USD,
        cadHolding, Currency.CAD));
    when(fxRateService.convertReturns(any(), any(), any(), any(), any())).thenReturn(Map.of(
        HOLDING_USD, treeMap(Map.entry(JAN, BigDecimal.valueOf(1.5))),
        cadHolding, treeMap(Map.entry(JAN, BigDecimal.ONE))));

    ProcessingContext context = ProcessingContext.of(null, null, new FxContext(Map.of(), Currency.CAD));

    ReturnsSnapshot<HoldingMonthlyReturns> result = processor.process(snapshot, context);

    assertThat(result.holdingCurrencyMap()).containsEntry(HOLDING_USD, Currency.CAD);
    assertThat(result.holdingCurrencyMap()).containsEntry(cadHolding, Currency.CAD);
  }

  private static ReturnsSnapshot<HoldingMonthlyReturns> snapshot(Map<PortfolioHolding, Currency> currencies) {
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = currencies.keySet().stream()
        .collect(java.util.stream.Collectors.toMap(h -> h,
            h -> treeMap(Map.entry(JAN, BigDecimal.ONE))));
    return new ReturnsSnapshot<>(currencies, returns, JAN, JAN, List.of());
  }

  private static TreeMap<LocalDate, BigDecimal> treeMap(Map.Entry<LocalDate, BigDecimal> entry) {
    TreeMap<LocalDate, BigDecimal> map = new TreeMap<>();
    map.put(entry.getKey(), entry.getValue());
    return map;
  }

  private static NavigableMap<LocalDate, BigDecimal> navigable() {
    TreeMap<LocalDate, BigDecimal> map = new TreeMap<>();
    map.put(JAN, BigDecimal.valueOf(1.3));
    return map;
  }
}