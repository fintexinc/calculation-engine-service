package com.fintex.ce.domain.model.holding;

import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FundSeriesHolding extends Holding {

  private String fundServCode;

  public FundSeriesHolding() {
  }

  public FundSeriesHolding(final BigDecimal amount, final String fundServCode) {
    super(amount, HoldingType.CANADA_MUTUAL_FUNDS);
    this.fundServCode = fundServCode;
    setSecurityIdentifier(new SecurityIdentifier(fundServCode, FiIdentifierType.FUNDSERV));
  }

  @Override
  public String generateUserIdentifier() {
    return fundServCode;
  }

}
