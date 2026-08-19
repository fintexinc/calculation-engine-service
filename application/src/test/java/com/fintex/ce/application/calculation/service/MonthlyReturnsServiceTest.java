package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.CashMonthlyReturnsGenerator;
import com.fintex.ce.application.returns.MonthlyReturnsGenerator;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.ErrorParams;
import com.fintex.ce.model.error.exceptions.BasePceException;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.cash;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holding;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MonthlyReturnsServiceTest {

  private static final LocalDate JANUARY_END = LocalDate.parse("2024-01-31");
  private static final LocalDate FEBRUARY_END = LocalDate.parse("2024-02-29");
  private static final LocalDate MARCH_END = LocalDate.parse("2024-03-31");

  private static final PortfolioHolding ETF = holding(
      new SecurityIdentifier("ETF-A", FiIdentifierType.TICKER), FinancialInstrumentType.ETF, Country.USA,
      (BigDecimal) null);
  private static final PortfolioHolding STOCK = holding(
      new SecurityIdentifier("STK-B", FiIdentifierType.TICKER), FinancialInstrumentType.STOCK, Country.USA,
      (BigDecimal) null);
  private static final CashHolding CASH = cash(Currency.CAD, (BigDecimal) null);

  private final MonthlyReturnsGenerator generator = mock(MonthlyReturnsGenerator.class);
  private final CashMonthlyReturnsGenerator cashMonthlyReturnsGenerator = mock(CashMonthlyReturnsGenerator.class);

  @BeforeEach
  void setUp() {
    when(cashMonthlyReturnsGenerator.generateCashMonthlyReturns(anyList())).thenReturn(Map.of());
  }

  @Test
  void shouldThrowNoSecurityDataForHolding_whenHoldingMissingFromMarketInvestmentCatalogueResponse() {
    when(generator.generateGicMonthlyReturns(anyList())).thenReturn(Map.of());
    MonthlyReturnsService service = service();

    assertThatThrownBy(() -> service.getMonthlyReturns(List.of(ETF), Map.of()))
        .isInstanceOf(BasePceException.class)
        .satisfies(thrown -> assertThat(((BasePceException) thrown).getErrorCode())
            .isEqualTo(ErrorCode.NO_SECURITY_DATA_FOR_HOLDING));
  }

  @Test
  void shouldThrowMissingMonthlyReturns_whenHoldingPresentButReturnsAreEmpty() {
    HoldingMonthlyReturns empty = new HoldingMonthlyReturns();
    empty.setCurrency(Currency.USD.name());
    empty.setReturns(new TreeMap<>());
    when(generator.generateGicMonthlyReturns(anyList())).thenReturn(Map.of());
    MonthlyReturnsService service = service();

    assertThatThrownBy(() -> service.getMonthlyReturns(List.of(ETF), Map.of(ETF, empty)))
        .isInstanceOf(BasePceException.class)
        .satisfies(thrown -> assertThat(((BasePceException) thrown).getErrorCode())
            .isEqualTo(ErrorCode.MISSING_MONTHLY_RETURNS));
  }

  @Test
  void shouldThrowMissingMonthlyReturnForDate_whenHoldingHistoryContainsCalendarGap() {
    HoldingMonthlyReturns returns = holdingMonthlyReturns(Currency.USD,
        Map.entry(JANUARY_END, BigDecimal.valueOf(0.01)),
        Map.entry(MARCH_END, BigDecimal.valueOf(0.02)));
    when(generator.generateGicMonthlyReturns(anyList())).thenReturn(Map.of());
    MonthlyReturnsService service = service();

    assertThatThrownBy(() -> service.getMonthlyReturns(List.of(ETF), Map.of(ETF, returns)))
        .isInstanceOfSatisfying(CalculationException.class, exception -> {
          assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSING_MONTHLY_RETURN_FOR_DATE);
          assertThat(exception)
              .hasMessage("The holding ETF-ETF-A is missing monthly return values for date 2024-02-29");
          assertThat(exception.getMetadata())
              .containsOnlyKeys(ErrorParams.HOLDING_ID, "param-1", "param-2")
              .containsEntry(ErrorParams.HOLDING_ID, ErrorParams.holdingId(ETF))
              .containsEntry("param-1", ErrorParams.holdingId(ETF))
              .containsEntry("param-2", FEBRUARY_END.toString());
        });
  }

  @Test
  void shouldMergeCashReturns_whenCashIsOmittedFromMarketInvestmentCatalogueResponse() {
    HoldingMonthlyReturns cashReturns = holdingMonthlyReturns(Currency.CAD,
        Map.entry(JANUARY_END, BigDecimal.valueOf(0.4)),
        Map.entry(FEBRUARY_END, BigDecimal.valueOf(0.5)));
    when(generator.generateGicMonthlyReturns(anyList())).thenReturn(Map.of());
    when(cashMonthlyReturnsGenerator.generateCashMonthlyReturns(anyList())).thenReturn(Map.of(CASH, cashReturns));
    MonthlyReturnsService service = service();

    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = service.getMonthlyReturns(List.of(CASH), Map.of());

    assertThat(snapshot.returnsMap()).containsOnlyKeys(CASH);
    assertThat(snapshot.returnsMap().get(CASH)).containsExactly(
        Map.entry(JANUARY_END, BigDecimal.valueOf(0.4)),
        Map.entry(FEBRUARY_END, BigDecimal.valueOf(0.5)));
    assertThat(snapshot.holdingCurrencyMap()).containsOnly(Map.entry(CASH, Currency.CAD));
    assertThat(snapshot.performanceStartDate()).isEqualTo(JANUARY_END);
    assertThat(snapshot.performanceEndDate()).isEqualTo(FEBRUARY_END);
    assertThat(snapshot.errors()).isEmpty();
    assertThat(snapshot.warnings()).isEmpty();
  }

  @Test
  void shouldMergeFetcherAndGeneratorOutputs_whenGetMonthlyReturns() {
    HoldingMonthlyReturns etfReturns = holdingMonthlyReturns(Currency.USD,
        Map.entry(JANUARY_END, BigDecimal.valueOf(0.01)));
    HoldingMonthlyReturns stockReturns = holdingMonthlyReturns(Currency.USD,
        Map.entry(JANUARY_END, BigDecimal.valueOf(0.02)));
    when(generator.generateGicMonthlyReturns(anyList())).thenReturn(Map.of(STOCK, stockReturns));
    MonthlyReturnsService service = service();

    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = service.getMonthlyReturns(List.of(ETF, STOCK),
        Map.of(ETF, etfReturns));

    assertThat(snapshot.returnsMap()).containsOnlyKeys(ETF, STOCK);
  }

  private MonthlyReturnsService service() {
    return new MonthlyReturnsService(generator, cashMonthlyReturnsGenerator);
  }

  @SafeVarargs
  private static HoldingMonthlyReturns holdingMonthlyReturns(Currency currency,
      Map.Entry<LocalDate, BigDecimal>... entries) {
    HoldingMonthlyReturns data = new HoldingMonthlyReturns();
    data.setCurrency(currency.name());
    TreeMap<LocalDate, BigDecimal> map = new TreeMap<>();
    for (Map.Entry<LocalDate, BigDecimal> entry : entries) {
      map.put(entry.getKey(), entry.getValue());
    }
    data.setReturns(map);
    return data;
  }
}
