package com.fintex.ce.domain.model.holding;

import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.enumeration.InterestFreq;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MonthlyReturnGeneratableHolding {

  InterestFreq getInterestFreq();

  LocalDate getInvestmentDate();

  BigDecimal getClientIntRate();

  Currency getCurrency();

}
