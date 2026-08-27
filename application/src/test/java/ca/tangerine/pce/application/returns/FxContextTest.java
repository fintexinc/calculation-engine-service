package ca.tangerine.pce.application.returns;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.model.domain.CurrencyExchangePair;
import ca.tangerine.wm.commons.domain.currency.Currency;

class FxContextTest {

  @Test
  void shouldExposeEmptyRatesAndNullTarget_whenEmpty() {
    FxContext fxContext = FxContext.empty();

    assertThat(fxContext.rates()).isEmpty();
    assertThat(fxContext.targetCurrency()).isNull();
    assertThat(fxContext.conversionRequired()).isFalse();
  }

  @Test
  void shouldRequireConversion_whenTargetCurrencyIsSet() {
    FxContext fxContext = new FxContext(Map.of(), Currency.CAD);

    assertThat(fxContext.conversionRequired()).isTrue();
    assertThat(fxContext.targetCurrency()).isEqualTo(Currency.CAD);
  }

  @Test
  void shouldDefensivelyCopyRates_whenConstructed() {
    Map<CurrencyExchangePair, NavigableMap<LocalDate, BigDecimal>> mutable = new HashMap<>();
    mutable.put(new CurrencyExchangePair(Currency.USD, Currency.CAD), new TreeMap<>());

    FxContext fxContext = new FxContext(mutable, Currency.CAD);
    mutable.clear();

    assertThat(fxContext.rates()).hasSize(1);
  }

  @Test
  void shouldNormalizeNullRates_whenConstructedWithNull() {
    FxContext fxContext = new FxContext(null, null);

    assertThat(fxContext.rates()).isEmpty();
    assertThat(fxContext.conversionRequired()).isFalse();
  }
}
