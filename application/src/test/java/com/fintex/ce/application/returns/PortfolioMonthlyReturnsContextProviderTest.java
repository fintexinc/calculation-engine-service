package com.fintex.ce.application.returns;

import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PortfolioMonthlyReturnsContextProviderTest {

  private static final PortfolioHolding ETF = new PortfolioHolding(null, FinancialInstrumentType.ETF_US,
      new SecurityIdentifier("SPY", FiIdentifierType.TICKER));

  private final MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);
  private final FxRateService fxRateService = mock(FxRateService.class);

  private final PortfolioMonthlyReturnsContextProvider provider = new PortfolioMonthlyReturnsContextProvider(
      monthlyReturnsService, fxRateService);

  @Test
  void shouldEmitPortfolioRole_whenGetIsCalled() {
    when(monthlyReturnsService.getMonthlyReturns(any())).thenReturn(ReturnsSnapshot.empty());

    MonthlyReturnsContext<HoldingMonthlyReturns> context = provider.get(List.of(ETF), null);

    assertThat(context.role()).isEqualTo(ReturnsRole.PORTFOLIO);
    assertThat(context.fxContext()).isEqualTo(FxContext.empty());
  }

  @Test
  void shouldQueryFxRatesWithLowerBoundExtendedToFirstOfMonthBeforePsd_whenTargetCurrencyIsSet() {
    // Regression: the FX query lower bound must reach back further than PSD - 1 month-end so that
    // floorEntry(PSD - 1 month) succeeds even when PSD - 1 month-end falls on a weekend or holiday.
    // FX_RATES_UNAVAILABLE is now a hard HTTP 400 error, so a spurious missing-rate on the first
    // month would abort the calculation instead of warning.
    LocalDate psd = LocalDate.parse("2020-01-31");
    LocalDate ped = LocalDate.parse("2020-03-31");
    TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>(Map.of(psd, BigDecimal.ONE, ped, BigDecimal.ONE));
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = new ReturnsSnapshot<>(Map.of(ETF, Currency.USD),
        Map.of(ETF, returns), psd, ped, List.of());
    when(monthlyReturnsService.getMonthlyReturns(any())).thenReturn(snapshot);
    when(fxRateService.rates(any(), any(), any())).thenReturn(Map.of());

    provider.get(List.of(ETF), Currency.CAD);

    ArgumentCaptor<DateRange> rangeCaptor = ArgumentCaptor.forClass(DateRange.class);
    verify(fxRateService).rates(any(), eq(Currency.CAD), rangeCaptor.capture());
    assertThat(rangeCaptor.getValue().start()).isEqualTo(LocalDate.parse("2019-12-01"));
    assertThat(rangeCaptor.getValue().end()).isEqualTo(ped);
  }
}
