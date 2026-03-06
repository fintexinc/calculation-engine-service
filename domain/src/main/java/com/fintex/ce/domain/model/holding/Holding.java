package com.fintex.ce.domain.model.holding;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fintex.ce.domain.enumeration.HoldingIdentifierType;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.sm.model.domain.EquitySecurityIdentifier;
import com.fintex.sm.model.domain.SecurityIdentifier;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true, defaultImpl = Holding.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = FundSeriesHolding.class, name = "CANADA_MUTUAL_FUNDS"),
    @JsonSubTypes.Type(value = FundSeriesHolding.class, name = "SEGREGATED_FUND_CANADA"),
    @JsonSubTypes.Type(value = EtfHolding.class, name = "US_ETF"),
    @JsonSubTypes.Type(value = EtfHolding.class, name = "CANADA_ETF"),
    @JsonSubTypes.Type(value = StockHolding.class, name = "CANADA_STOCKS"),
    @JsonSubTypes.Type(value = StockHolding.class, name = "US_STOCKS"),
    @JsonSubTypes.Type(value = CashHolding.class, name = "CASH"),
    @JsonSubTypes.Type(value = BenchmarkIndexHolding.class, name = "BENCHMARK_INDEX"),
    @JsonSubTypes.Type(value = GicHolding.class, name = "GIC"),
    @JsonSubTypes.Type(value = CanadaPooledFundHolding.class, name = "CANADA_POOLED_FUNDS"),
    @JsonSubTypes.Type(value = CanadaHedgeFundHolding.class, name = "CANADA_HEDGE_FUNDS"),
    @JsonSubTypes.Type(value = UsMutualFundHolding.class, name = "US_MUTUAL_FUNDS"),
    @JsonSubTypes.Type(value = FixedIncomeHolding.class, name = "FIXED_INCOME"),
    @JsonSubTypes.Type(value = SmaHolding.class, name = "SEPARATELY_MANAGED_ACCOUNT"),
    @JsonSubTypes.Type(value = PagHolding.class, name = "PAG_GUIDED_PORTFOLIO")
})
@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class Holding {

  public static final String DELIMITER = "-";

  @EqualsAndHashCode.Exclude
  private BigDecimal value;

  private HoldingType type;

  @Deprecated
  private HoldingIdentifierType holdingIdentifier;

  private SecurityIdentifier securityIdentifier;

  public Holding() {
  }

  public Holding(BigDecimal value, HoldingType type) {
    this.value = value;
    this.type = type;
  }

  /**
   * Unique id for the end user
   *
   * @return id of the entity
   */
  public String generateUserIdentifier() {
    if (securityIdentifier == null) {
      return type + DELIMITER + value;
    }
    if (securityIdentifier instanceof EquitySecurityIdentifier eq) {
      return type + DELIMITER + securityIdentifier.getId() + DELIMITER + eq.getExchangeId();
    }
    return type + DELIMITER + securityIdentifier.getId();
  }

}
