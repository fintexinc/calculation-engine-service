package com.fintex.ce.domain.model.holding;

import com.fintex.ce.domain.model.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.model.enumeration.InterestFreq;
import com.fintex.sm.model.domain.enumeration.CurrencyType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import static com.fintex.ce.domain.util.BigDecimalUtils.bigDecimalEquals;
import static com.fintex.sm.model.domain.enumeration.CurrencyType.CAD;

@Getter
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@Jacksonized
public class GicHolding extends Holding implements MonthlyReturnGeneratableHolding {

  public static final LocalDate DEFAULT_START_DATE = LocalDate.of(1954, 1, 31);

  @Builder.Default
  private final UUID uuid = UUID.randomUUID();
  private final CurrencyType currency;
  private final LocalDate investmentDate;
  private final BigDecimal clientIntRate;
  private final InterestFreq interestFreq;
  private final BigDecimal term;
  private final String name;

  public CurrencyType getCurrency() {
    if (Objects.isNull(currency)) {
      return CAD;
    }
    return currency;
  }

  public LocalDate getInvestmentDate() {
    if (Objects.isNull(investmentDate)) {
      return DEFAULT_START_DATE;
    }
    return investmentDate;
  }

  public InterestFreq getInterestFreq() {
    if (Objects.isNull(interestFreq)) {
      return InterestFreq.ANNUAL;
    }
    return interestFreq;
  }

  public AssetAllocationRegion getAssetAllocation() {
    if (isLessThanOneYearOld()) {
      return AssetAllocationRegion.CASH;
    }
    return AssetAllocationRegion.FIXED_INCOME;
  }

  public boolean isLessThanOneYearOld() {
    return getTerm().intValue() < 365;
  }

  @Override
  public String generateUserIdentifier() {
    return getHoldingType() + Holding.DELIMITER + getCurrency() + Holding.DELIMITER + getValue();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    GicHolding that = (GicHolding) o;
    return Objects.equals(uuid, that.uuid)
        && Objects.equals(getCurrency(), that.getCurrency())
        && Objects.equals(getInvestmentDate(), that.getInvestmentDate())
        && bigDecimalEquals(clientIntRate, that.clientIntRate)
        && Objects.equals(getInterestFreq(), that.getInterestFreq())
        && bigDecimalEquals(term, that.term)
        && Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
