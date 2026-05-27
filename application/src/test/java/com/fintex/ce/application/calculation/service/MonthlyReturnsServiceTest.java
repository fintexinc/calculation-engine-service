package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.MonthlyReturnsGenerator;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.BasePceException;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MonthlyReturnsServiceTest {

  private static final PortfolioHolding ETF = new PortfolioHolding(null, FinancialInstrumentType.ETF_US,
      new SecurityIdentifier("ETF-A", FiIdentifierType.TICKER));
  private static final PortfolioHolding STOCK = new PortfolioHolding(null, FinancialInstrumentType.STOCK_US,
      new SecurityIdentifier("STK-B", FiIdentifierType.TICKER));
  private static final PortfolioHolding CASH = new PortfolioHolding(null, FinancialInstrumentType.CASH,
      new SecurityIdentifier("CASH", FiIdentifierType.TICKER));

  @SuppressWarnings("unchecked")
  private final SecurityDataFetcher<HoldingMonthlyReturns> fetcher = mock(SecurityDataFetcher.class);
  private final MonthlyReturnsGenerator generator = mock(MonthlyReturnsGenerator.class);

  private final MonthlyReturnsService service = new MonthlyReturnsService(fetcher, generator);

  @Test
  void shouldThrowSmsNoDataForHolding_whenHoldingMissingFromSecurityMasterResponse() {
    when(fetcher.fetch(anyList(), anyList())).thenReturn(Map.of());
    when(generator.generateGicMonthlyReturns(anyList())).thenReturn(Map.of());

    assertThatThrownBy(() -> service.getMonthlyReturns(List.of(ETF)))
        .isInstanceOf(BasePceException.class)
        .satisfies(thrown -> assertThat(((BasePceException) thrown).getErrorCode())
            .isEqualTo(ErrorCode.NO_SECURITY_DATA_FOR_HOLDING));
  }

  @Test
  void shouldThrowMissingMonthlyReturns_whenHoldingPresentButReturnsAreEmpty() {
    HoldingMonthlyReturns empty = new HoldingMonthlyReturns();
    empty.setCurrency(Currency.USD.name());
    empty.setReturns(new TreeMap<>());
    when(fetcher.fetch(anyList(), anyList())).thenReturn(Map.of(ETF, empty));
    when(generator.generateGicMonthlyReturns(anyList())).thenReturn(Map.of());

    assertThatThrownBy(() -> service.getMonthlyReturns(List.of(ETF)))
        .isInstanceOf(BasePceException.class)
        .satisfies(thrown -> assertThat(((BasePceException) thrown).getErrorCode())
            .isEqualTo(ErrorCode.MISSING_MONTHLY_RETURNS));
  }

  @Test
  void shouldSkipCashAndGicTypes_whenValidatingMonthlyReturnsPresence() {
    when(fetcher.fetch(anyList(), anyList())).thenReturn(Map.of());
    when(generator.generateGicMonthlyReturns(anyList())).thenReturn(Map.of());

    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = service.getMonthlyReturns(List.of(CASH));

    assertThat(snapshot.returnsMap()).isEmpty();
    assertThat(snapshot.errors()).isEmpty();
  }

  @Test
  void shouldMergeFetcherAndGeneratorOutputs_whenGetMonthlyReturns() {
    HoldingMonthlyReturns etfReturns = holdingMonthlyReturns(Currency.USD,
        Map.entry(LocalDate.parse("2020-01-31"), BigDecimal.valueOf(0.01)));
    HoldingMonthlyReturns stockReturns = holdingMonthlyReturns(Currency.USD,
        Map.entry(LocalDate.parse("2020-01-31"), BigDecimal.valueOf(0.02)));
    when(fetcher.fetch(anyList(), anyList())).thenReturn(Map.of(ETF, etfReturns));
    when(generator.generateGicMonthlyReturns(anyList())).thenReturn(Map.of(STOCK, stockReturns));

    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = service.getMonthlyReturns(List.of(ETF, STOCK));

    assertThat(snapshot.returnsMap()).containsOnlyKeys(ETF, STOCK);
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
