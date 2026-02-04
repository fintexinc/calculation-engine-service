package com.fintex.ce.domain.model.holding;

import com.fintex.ce.domain.enumeration.HoldingType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

import static com.fintex.ce.domain.model.holding.Holding.DELIMITER;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class CanadaHedgeFundHolding extends Holding {

  private String morningstarId;

  public CanadaHedgeFundHolding() {
  }

  public CanadaHedgeFundHolding(BigDecimal amount, String morningstarId) {
    super(amount, HoldingType.CANADA_HEDGE_FUNDS);
    this.morningstarId = morningstarId;
  }

  @Override
  public String generateUserIdentifier() {
    return getType() + DELIMITER + morningstarId;
  }

}
