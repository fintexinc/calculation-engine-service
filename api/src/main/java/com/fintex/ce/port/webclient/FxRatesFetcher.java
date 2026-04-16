package com.fintex.ce.port.webclient;

import com.fintex.ce.domain.model.CurrencyExchangePair;
import com.fintex.ce.domain.model.DateRange;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;

public interface FxRatesFetcher {

  NavigableMap<LocalDate, BigDecimal> fetch(CurrencyExchangePair currencyPair, DateRange dateRange);

}
