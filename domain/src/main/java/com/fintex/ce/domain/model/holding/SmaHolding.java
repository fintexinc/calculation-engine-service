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
public class SmaHolding extends Holding {

  private String identifier;
  private String currency;

  public SmaHolding setIdentifier(String identifier) {
    this.identifier = identifier;
    setSecurityIdentifier(new SecurityIdentifier(identifier, FiIdentifierType.MORNINGSTAR_ID));
    return this;
  }

  @Override
  public String generateUserIdentifier() {
    return getType() + DELIMITER + identifier;
  }

}
