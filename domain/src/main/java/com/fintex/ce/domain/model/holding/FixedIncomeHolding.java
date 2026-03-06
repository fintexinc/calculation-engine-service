package com.fintex.ce.domain.model.holding;

import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import static com.fintex.ce.domain.model.holding.Holding.DELIMITER;

@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FixedIncomeHolding extends Holding {

  private String identifier;

  public FixedIncomeHolding setIdentifier(String identifier) {
    this.identifier = identifier;
    setSecurityIdentifier(new SecurityIdentifier(identifier, FiIdentifierType.CUSIP));
    return this;
  }

  @Override
  public String generateUserIdentifier() {
    return getType() + DELIMITER + identifier;
  }

}
