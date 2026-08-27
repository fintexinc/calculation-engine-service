package ca.tangerine.pce.application.returns;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;

import ca.tangerine.pce.model.domain.CurrencyExchangePair;
import ca.tangerine.wm.commons.domain.currency.Currency;

/**
 * Immutable bundle of FX inputs needed by {@code FxConversionProcessor}.
 *
 * <p>
 * {@code rates} maps each currency pair (source → target) to the time series of end-of-month rates fetched for the
 * relevant date range; {@code targetCurrency} is the currency every holding's returns should be converted into. A
 * {@code null} {@code targetCurrency} signals that no FX conversion is required and the FX processor will short-circuit
 * to a no-op.
 * </p>
 */
public record FxContext(
    Map<CurrencyExchangePair, NavigableMap<LocalDate, BigDecimal>> rates,
    Currency targetCurrency) {

  public FxContext {
    rates = rates == null ? Map.of() : Map.copyOf(rates);
  }

  public static FxContext empty() {
    return new FxContext(Map.of(), null);
  }

  public boolean conversionRequired() {
    return targetCurrency != null;
  }
}
