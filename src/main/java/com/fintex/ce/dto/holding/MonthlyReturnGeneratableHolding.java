package com.fintex.ce.dto.holding;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.config.enumeration.InterestFreq;

import java.math.BigDecimal;
import java.time.LocalDate;


public interface MonthlyReturnGeneratableHolding {

    InterestFreq getInterestFreq();

    LocalDate getInvestmentDate();

    BigDecimal getClientIntRate();

    Currency getCurrency();

}
