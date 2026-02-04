package com.fintex.ce.domain.model.holding;

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

  @Override
  public String generateUserIdentifier() {
    return getType() + DELIMITER + identifier;
  }

}
