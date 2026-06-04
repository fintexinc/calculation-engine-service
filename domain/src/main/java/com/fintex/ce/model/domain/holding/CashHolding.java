package com.fintex.ce.model.domain.holding;

import com.fintex.ce.model.domain.enumeration.InterestFreq;
import com.fintex.wm.commons.domain.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@Jacksonized
@EqualsAndHashCode(callSuper = true)
public final class CashHolding extends PortfolioHolding implements MonthlyReturnGeneratableHolding {

  private final Currency currency;
  private final LocalDate investmentDate;
  private final BigDecimal clientIntRate;
  private final InterestFreq interestFreq;

  public InterestFreq getInterestFreq() {
    if (Objects.isNull(interestFreq)) {
      return InterestFreq.ANNUAL;
    }
    return interestFreq;
  }

  public LocalDate getInvestmentDate() {
    if (Objects.isNull(investmentDate)) {
      return GicHolding.DEFAULT_START_DATE;
    }
    return investmentDate;
  }

  public boolean hasClientIntRate() {
    return clientIntRate != null;
  }

}
