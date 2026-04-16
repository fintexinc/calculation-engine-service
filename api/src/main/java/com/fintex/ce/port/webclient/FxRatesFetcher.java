package com.fintex.ce.port.webclient;

import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.calculation.DateRange;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;

public interface FxRatesFetcher {

  NavigableMap<LocalDate, BigDecimal> fetch(CurrencyExchangePair currencyPair, DateRange dateRange);

}
