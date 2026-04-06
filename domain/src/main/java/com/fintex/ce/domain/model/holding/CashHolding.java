package com.fintex.ce.domain.model.holding;

import com.fintex.ce.domain.model.enumeration.InterestFreq;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import static com.fintex.ce.domain.util.BigDecimalUtils.bigDecimalEquals;

@Getter
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@Jacksonized
public class CashHolding extends Holding implements MonthlyReturnGeneratableHolding {

  private final CurrencyType currency;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    CashHolding that = (CashHolding) o;
    return Objects.equals(currency, that.currency)
        && Objects.equals(getInvestmentDate(), that.getInvestmentDate())
        && bigDecimalEquals(clientIntRate, that.clientIntRate)
        && Objects.equals(getInterestFreq(), that.getInterestFreq());
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
