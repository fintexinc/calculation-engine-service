package ca.tangerine.pce.application.returns;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import static ca.tangerine.pce.util.FilterUtils.CASH_PREDICATE;
import static ca.tangerine.pce.util.FilterUtils.filterHoldings;

import ca.tangerine.pce.application.util.TBillsValidator;
import ca.tangerine.pce.model.domain.calculation.returns.HoldingMonthlyReturns;
import ca.tangerine.pce.model.domain.holding.CashHolding;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.port.webclient.mic.TreasuryBillsFetcher;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;

/**
 * Sources the monthly-return series of CASH holdings from the Treasury Bill series of their currency.
 *
 * <p>
 * A cash balance earns nothing while it sits in the account, but it still has to carry a series through the
 * weighted-average pipeline: {@link WeightedAverageComponent#calculateEndingPortfolioWeight} derives the portfolio
 * weights from the holdings present in the returns snapshot, so a cash holding without returns is dropped from the
 * weight denominator and the remaining holdings are over-weighted — a 10k cash sleeve would silently disappear from
 * growth-of-10k instead of diluting it. Modelling the balance as short-term government paper is the standard proxy and
 * keeps cash consistent with the risk metrics (Sharpe, Sortino, Alpha), which already treat the T-Bill series as the
 * risk-free rate.
 *
 * <p>
 * The series is currency-specific, so it is fetched once per distinct cash currency (see {@link TreasuryBillsFetcher})
 * and shared by the holdings of that currency; a currency the provider has no T-Bill data for fails fast through
 * {@link TBillsValidator}. This is the CASH counterpart of the GIC returns synthesized by
 * {@link MonthlyReturnsGenerator}, and both are merged into the snapshot by
 * {@code MonthlyReturnsService#getMonthlyReturns}.
 */
@Component
@RequiredArgsConstructor
public class CashMonthlyReturnsGenerator {

  private final TreasuryBillsFetcher treasuryBillsFetcher;

  public Map<PortfolioHolding, HoldingMonthlyReturns> generateCashMonthlyReturns(List<PortfolioHolding> holdings) {
    List<CashHolding> cashHoldings = filterHoldings(holdings, CASH_PREDICATE);
    Map<Currency, NavigableMap<LocalDate, BigDecimal>> returnsByCurrency = cashHoldings.stream()
        .map(CashHolding::getCurrency)
        .distinct()
        .collect(Collectors.toMap(Function.identity(),
            currency -> TBillsValidator.requireNonEmpty(treasuryBillsFetcher.fetch(currency), currency)));

    return cashHoldings.stream()
        .collect(Collectors.toMap(Function.identity(),
            holding -> createMonthlyReturns(holding.getCurrency(), returnsByCurrency.get(holding.getCurrency()))));
  }

  private HoldingMonthlyReturns createMonthlyReturns(Currency currency, NavigableMap<LocalDate, BigDecimal> returns) {
    HoldingMonthlyReturns monthlyReturns = new HoldingMonthlyReturns();
    monthlyReturns.setCurrency(currency.name());
    monthlyReturns.setHoldingType(FinancialInstrumentType.CASH);
    monthlyReturns.setReturns(new TreeMap<>(returns));
    return monthlyReturns;
  }
}
