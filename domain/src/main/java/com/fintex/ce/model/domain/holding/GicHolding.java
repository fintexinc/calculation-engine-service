package com.fintex.ce.model.domain.holding;

import com.fintex.ce.model.domain.enumeration.InterestFreq;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.domain.currency.Currency;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import static com.fintex.wm.commons.domain.currency.Currency.CAD;

@Getter
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@Jacksonized
@EqualsAndHashCode(callSuper = true)
public final class GicHolding extends PortfolioHolding implements MonthlyReturnGeneratableHolding {

  public static final LocalDate DEFAULT_START_DATE = LocalDate.of(1954, 1, 31);

  @Builder.Default
  private final UUID uuid = UUID.randomUUID();
  private final Currency currency;
  private final LocalDate investmentDate;
  private final BigDecimal clientIntRate;
  private final InterestFreq interestFreq;
  private final BigDecimal term;
  private final String name;

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

  /**
   * Derived from {@link #getTerm()}, so it is deliberately kept off the wire: a holding is an inbound request object,
   * and both this and {@link #isLessThanOneYearOld()} throw when {@code term} is absent. Serializing them would turn a
   * request that validation is meant to reject with a 400 ({@code NotEmptyGicTermReqValidator}) into a
   * {@code NullPointerException} raised inside Jackson, and neither value is part of the request contract.
   */
  @JsonIgnore
  public AssetAllocationRegionType getAssetAllocationRegionType() {
    if (isLessThanOneYearOld()) {
      return AssetAllocationRegionType.CASH;
    }
    return AssetAllocationRegionType.FIXED_INCOME;
  }

  /**
   * Left to throw on an absent {@code term} rather than defaulting: a GIC with no term is rejected at the request
   * boundary, so reaching here without one is a wiring mistake — a metric that buckets GICs missing from
   * {@code NotEmptyGicTermReqValidator.supportedMetrics()} — and a silent default would hide it behind a plausible
   * bucket. See {@link #getAssetAllocationRegionType()} for why it is not serialized.
   */
  @JsonIgnore
  public boolean isLessThanOneYearOld() {
    return getTerm().intValue() < 365;
  }

  @Override
  public String getIdsString() {
    return getHoldingType() + PortfolioHolding.DELIMITER + getCurrency() + PortfolioHolding.DELIMITER + getValue();
  }

}
