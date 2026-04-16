package com.fintex.ce.domain.model;

import com.fintex.sm.model.domain.enumeration.CurrencyType;

public record CurrencyExchangePair(CurrencyType from, CurrencyType to) {

  public CurrencyExchangePair inverse() {
    return new CurrencyExchangePair(to, from);
  }
}