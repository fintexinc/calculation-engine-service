package com.fintex.ce.application.returns;

import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.ce.port.webclient.mic.TreasuryBillsFetcher;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.cash;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CashMonthlyReturnsGeneratorTest {

  private static final CashHolding CAD_CASH = cash(Currency.CAD, "100");
  private static final CashHolding USD_CASH = cash(Currency.USD, "300");

  private final TreasuryBillsFetcher treasuryBillsFetcher = mock(TreasuryBillsFetcher.class);
  private final CashMonthlyReturnsGenerator generator = new CashMonthlyReturnsGenerator(treasuryBillsFetcher);

  @Test
  void shouldGenerateMonthlyReturnsFromCurrencySpecificTBills_whenCashHoldingsPresent() {
    NavigableMap<LocalDate, BigDecimal> cadReturns = returns("2024-01-31", "0.40", "2024-02-29", "0.50");
    NavigableMap<LocalDate, BigDecimal> usdReturns = returns("2024-01-31", "0.30", "2024-02-29", "0.60");
    when(treasuryBillsFetcher.fetch(Currency.CAD)).thenReturn(cadReturns);
    when(treasuryBillsFetcher.fetch(Currency.USD)).thenReturn(usdReturns);

    Map<PortfolioHolding, HoldingMonthlyReturns> monthlyReturns = generator.generateCashMonthlyReturns(
        List.of(CAD_CASH, USD_CASH));

    assertThat(monthlyReturns).hasSize(2).containsOnlyKeys(CAD_CASH, USD_CASH);
    assertCashReturns(monthlyReturns.get(CAD_CASH), Currency.CAD, cadReturns);
    assertCashReturns(monthlyReturns.get(USD_CASH), Currency.USD, usdReturns);
    verify(treasuryBillsFetcher).fetch(Currency.CAD);
    verify(treasuryBillsFetcher).fetch(Currency.USD);
  }

  @Test
  void shouldNotFetchTBills_whenPortfolioContainsNoCashHoldings() {
    Map<PortfolioHolding, HoldingMonthlyReturns> monthlyReturns = generator.generateCashMonthlyReturns(List.of());

    assertThat(monthlyReturns).isEmpty();
    verifyNoInteractions(treasuryBillsFetcher);
  }

  @Test
  void shouldThrowTBillSeriesNotAvailable_whenCurrencyHasNoTBillReturns() {
    when(treasuryBillsFetcher.fetch(Currency.CAD)).thenReturn(new TreeMap<>());

    assertThatThrownBy(() -> generator.generateCashMonthlyReturns(List.of(CAD_CASH)))
        .isInstanceOfSatisfying(CalculationException.class, exception -> {
          assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TBILL_SERIES_NOT_AVAILABLE_FOR_CURRENCY);
          assertThat(exception).hasMessage("T-Bill rates are not available for currency CAD");
          assertThat(exception.getMetadata())
              .containsOnlyKeys("param-1")
              .containsEntry("param-1", Currency.CAD);
        });
    verify(treasuryBillsFetcher).fetch(Currency.CAD);
  }

  private static NavigableMap<LocalDate, BigDecimal> returns(String... dateValuePairs) {
    return new TreeMap<>(Map.of(
        LocalDate.parse(dateValuePairs[0]), new BigDecimal(dateValuePairs[1]),
        LocalDate.parse(dateValuePairs[2]), new BigDecimal(dateValuePairs[3])));
  }

  private static void assertCashReturns(HoldingMonthlyReturns actual, Currency currency,
      NavigableMap<LocalDate, BigDecimal> expectedReturns) {
    assertThat(actual.getHoldingType()).isEqualTo(FinancialInstrumentType.CASH);
    assertThat(actual.getCurrency()).isEqualTo(currency.name());
    assertThat(actual.getReturns()).containsExactlyEntriesOf(expectedReturns);
  }
}
