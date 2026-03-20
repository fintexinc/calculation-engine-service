package com.fintex.ce.domain.model.holding;

import com.fintex.ce.domain.model.enumeration.HoldingType;
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
public class UsMutualFundHolding extends Holding {

  private String ticker;

  public UsMutualFundHolding() {
  }

  public UsMutualFundHolding(BigDecimal amount, String ticker) {
    super(amount, HoldingType.US_MUTUAL_FUNDS);
    this.ticker = ticker;
    setSecurityIdentifier(new SecurityIdentifier(ticker, FiIdentifierType.TICKER));
  }

  @Override
  public String generateUserIdentifier() {
    return getType() + DELIMITER + ticker;
  }

}
