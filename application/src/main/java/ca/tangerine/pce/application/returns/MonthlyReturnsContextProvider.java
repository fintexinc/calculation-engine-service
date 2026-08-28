package ca.tangerine.pce.application.returns;

import ca.tangerine.pce.application.calculation.service.FxRateService;
import ca.tangerine.pce.application.calculation.service.MonthlyReturnsService;
import ca.tangerine.pce.model.domain.CurrencyExchangePair;
import ca.tangerine.pce.model.domain.calculation.DateRange;
import ca.tangerine.pce.model.domain.calculation.returns.HoldingMonthlyReturns;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.wm.commons.domain.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Builds a role-tagged {@link MonthlyReturnsContext} from a list of holdings + a target currency: pulls the monthly
 * returns snapshot via {@link MonthlyReturnsService}, fetches the FX rates needed for currency conversion, and wraps
 * the pair under a {@link ReturnsRole}. Subclasses pin the role they emit; callers inject the concrete subclass that
 * matches the side they want.
 */
@Slf4j
public abstract class MonthlyReturnsContextProvider {

  protected final MonthlyReturnsService monthlyReturnsService;
  private final FxRateService fxRateService;

  protected MonthlyReturnsContextProvider(MonthlyReturnsService monthlyReturnsService, FxRateService fxRateService) {
    this.monthlyReturnsService = monthlyReturnsService;
    this.fxRateService = fxRateService;
  }

  protected abstract ReturnsRole role();

  public final MonthlyReturnsContext<HoldingMonthlyReturns> get(List<PortfolioHolding> holdings, Currency currency,
      Map<PortfolioHolding, HoldingMonthlyReturns> monthlyReturns) {
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = monthlyReturnsService.getMonthlyReturns(holdings,
        monthlyReturns);
    FxContext fxContext = buildFxContext(snapshot, currency);
    return new MonthlyReturnsContext<>(snapshot, fxContext, role());
  }

  private FxContext buildFxContext(ReturnsSnapshot<HoldingMonthlyReturns> snapshot, Currency targetCurrency) {
    if (targetCurrency == null) {
      return FxContext.empty();
    }
    Map<CurrencyExchangePair, NavigableMap<LocalDate, BigDecimal>> rates = fetchFxRates(
        snapshot.holdingCurrencyMap(), targetCurrency,
        new DateRange(snapshot.performanceStartDate(), snapshot.performanceEndDate()));
    return new FxContext(rates, targetCurrency);
  }

  private Map<CurrencyExchangePair, NavigableMap<LocalDate, BigDecimal>> fetchFxRates(
      Map<PortfolioHolding, Currency> holdingCurrencies, Currency toCurrency, DateRange range) {
    log.debug("PortfolioHolding currencies: {}, target: {}", holdingCurrencies.values(), toCurrency);
    // Extend the lower bound to the first day of the month before PSD: the per-month conversion formula
    // in FxRateService looks up floorEntry(date.minusMonths(1)) for every monthly return, so the very
    // first month needs a rate at PSD - 1 month. Shifting only to the last-day of that month is unsafe
    // because that day may be a weekend or holiday with no published rate; widening to the first of the
    // month guarantees the floor lookup hits at least the last business day of the prior month.
    LocalDate extendedFrom = range.start() == null ? null : range.start().minusMonths(1).withDayOfMonth(1);
    return fxRateService.rates(holdingCurrencies, toCurrency, new DateRange(extendedFrom, range.end()));
  }
}
