package com.fintex.ce.port.output;

import com.fintex.ce.domain.enumeration.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;

public interface TBillsPort {

  NavigableMap<LocalDate, BigDecimal> loadTBillsFor(Currency currency);

}
