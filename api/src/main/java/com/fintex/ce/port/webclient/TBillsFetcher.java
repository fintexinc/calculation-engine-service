package com.fintex.ce.port.webclient;

import com.fintex.sm.model.domain.enumeration.CurrencyType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;

public interface TBillsFetcher {

  NavigableMap<LocalDate, BigDecimal> fetch(CurrencyType currency);

}
