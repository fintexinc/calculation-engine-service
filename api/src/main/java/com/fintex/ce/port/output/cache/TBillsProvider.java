package com.fintex.ce.port.output.cache;

import com.fintex.ce.domain.enumeration.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.TreeMap;

public interface TBillsProvider {

  NavigableMap<LocalDate, BigDecimal> loadTBillsFor(Currency currency);

}
