package com.fintex.ce.port;

import com.fintex.ce.domain.model.enumeration.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;

public interface TBillsFetcher {

  NavigableMap<LocalDate, BigDecimal> fetch(Currency currency);

}
