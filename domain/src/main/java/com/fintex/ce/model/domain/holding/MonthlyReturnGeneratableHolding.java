package com.fintex.ce.model.domain.holding;

import com.fintex.ce.model.domain.enumeration.InterestFreq;
import com.fintex.wm.commons.domain.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MonthlyReturnGeneratableHolding {

  InterestFreq getInterestFreq();

  LocalDate getInvestmentDate();

  BigDecimal getClientIntRate();

  Currency getCurrency();

}
