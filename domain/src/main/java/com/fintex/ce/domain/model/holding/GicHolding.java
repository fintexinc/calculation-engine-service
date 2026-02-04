package com.fintex.ce.domain.model.holding;

import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.enumeration.InterestFreq;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import static com.fintex.ce.domain.enumeration.Currency.CAD;
import static com.fintex.ce.domain.model.holding.Holding.DELIMITER;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class GicHolding extends Holding implements MonthlyReturnGeneratableHolding {

  public static final LocalDate DEFAULT_START_DATE = LocalDate.of(1954, 1, 31);

  public GicHolding() {
  }

  public GicHolding(final BigDecimal amount, final HoldingType type) {
    super(amount, type);
  }

  private UUID uuid = UUID.randomUUID();
  private Currency currency;
  private LocalDate investmentDate;
  private BigDecimal clientIntRate;
  private InterestFreq interestFreq;
  private BigDecimal term;
  private String name;

  public Currency getCurrency() {
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
    if (getTerm().intValue() < 365) {
      return true;
    }
    return false;
  }

  @Override
  public String generateUserIdentifier() {
    return super.getType() + DELIMITER + getCurrency() + DELIMITER + super.getValue();
  }
}
