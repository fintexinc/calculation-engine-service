package com.fintex.ce.port.webclient.sm;

import com.fintex.wm.commons.domain.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;

public interface TBillsFetcher {

  Map<Currency, NavigableMap<LocalDate, BigDecimal>> fetch();

}