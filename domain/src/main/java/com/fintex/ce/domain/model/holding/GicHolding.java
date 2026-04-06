package com.fintex.ce.domain.model.holding;

import com.fintex.ce.domain.model.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.model.enumeration.InterestFreq;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import static com.fintex.sm.model.domain.enumeration.CurrencyType.CAD;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class GicHolding extends Holding implements MonthlyReturnGeneratableHolding {

  public static final LocalDate DEFAULT_START_DATE = LocalDate.of(1954, 1, 31);

  public GicHolding() {
  }

  public GicHolding(final BigDecimal amount, final FinancialInstrumentType holdingType) {
    super(amount, holdingType);
  }

  private UUID uuid = UUID.randomUUID();
  private CurrencyType currency;
  private LocalDate investmentDate;
  private BigDecimal clientIntRate;
  private InterestFreq interestFreq;
  private BigDecimal term;
  private String name;

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
}
