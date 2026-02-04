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
public class EtfHolding extends Holding {

  private String exchangeCode;
  private String ticker;

  public EtfHolding() {
  }

  public EtfHolding(final BigDecimal amount, final HoldingType type, final String exchangeCode, final String ticker) {
    super(amount, type);
    this.exchangeCode = exchangeCode;
    this.ticker = ticker;
  }

  @Override
  public String generateUserIdentifier() {
    if (exchangeCode == null || exchangeCode.trim().isEmpty()) {
      return getType() + DELIMITER + getTicker();
    }
    return getType() + DELIMITER + getTicker() + DELIMITER + getExchangeCode();
  }

}
