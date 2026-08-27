package ca.tangerine.pce.model.domain;

import ca.tangerine.wm.commons.domain.currency.Currency;

public record CurrencyExchangePair(Currency from, Currency to) {

  public CurrencyExchangePair inverse() {
    return new CurrencyExchangePair(to, from);
  }
}
