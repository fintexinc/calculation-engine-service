package com.fintex.ce.domain.model.holding;

import com.fintex.ce.domain.enumeration.Currency;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import static com.fintex.ce.domain.model.holding.Holding.DELIMITER;

@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PagHolding extends Holding {

  private String identifier;
  private Currency currency;

  @Override
  public String generateUserIdentifier() {
    return getType() + DELIMITER + identifier;
  }

}
