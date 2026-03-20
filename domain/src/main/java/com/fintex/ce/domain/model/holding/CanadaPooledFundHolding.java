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
public class CanadaPooledFundHolding extends Holding {

  private String morningstarId;

  public CanadaPooledFundHolding() {
  }

  public CanadaPooledFundHolding(BigDecimal amount, String morningstarId) {
    super(amount, HoldingType.CANADA_POOLED_FUNDS);
    this.morningstarId = morningstarId;
    setSecurityIdentifier(new SecurityIdentifier(morningstarId, FiIdentifierType.MORNINGSTAR_ID));
  }

  @Override
  public String generateUserIdentifier() {
    return getType() + DELIMITER + morningstarId;
  }

}
