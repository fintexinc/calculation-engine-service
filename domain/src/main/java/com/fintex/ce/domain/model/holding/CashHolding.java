package com.fintex.ce.domain.model.holding;

import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.enumeration.InterestFreq;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class CashHolding extends Holding implements MonthlyReturnGeneratableHolding {

  public CashHolding() {
  }

  private Currency currency;

  private LocalDate investmentDate;
  private BigDecimal clientIntRate;
  private InterestFreq interestFreq;

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

  public CashHolding(final BigDecimal amount, final FinancialInstrumentType holdingType) {
    super(amount, holdingType);
  }

  public boolean hasClientIntRate() {
    return clientIntRate != null;
  }

}
