package ca.tangerine.pce.model.domain.holding;

import java.math.BigDecimal;
import java.time.LocalDate;

import ca.tangerine.pce.model.domain.enumeration.InterestFreq;
import ca.tangerine.wm.commons.domain.currency.Currency;

public interface MonthlyReturnGeneratableHolding {

  InterestFreq getInterestFreq();

  LocalDate getInvestmentDate();

  BigDecimal getClientIntRate();

  Currency getCurrency();

}
