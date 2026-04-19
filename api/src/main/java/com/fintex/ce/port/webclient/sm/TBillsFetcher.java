package com.fintex.ce.port.webclient.sm;

import com.fintex.wm.commons.domain.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;

public interface TBillsFetcher {

  NavigableMap<LocalDate, BigDecimal> fetch(Currency currency);

}
