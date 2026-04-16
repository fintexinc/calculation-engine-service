package com.fintex.ce.model.domain;

import com.fintex.wm.commons.domain.currency.Currency;

public record CurrencyExchangePair(Currency from, Currency to) {

  public CurrencyExchangePair inverse() {
    return new CurrencyExchangePair(to, from);
  }
}
