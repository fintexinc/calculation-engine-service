package com.fintex.ce.domain.model.holding;

import com.fintex.ce.domain.model.enumeration.HoldingType;
import com.fintex.sm.model.domain.EquitySecurityIdentifier;
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
public class StockHolding extends Holding {

  private String exchangeCode;
  private String ticker;

  public StockHolding() {
  }

  public StockHolding(final BigDecimal amount, final HoldingType type, final String exchangeCode, final String ticker) {
    super(amount, type);
    this.exchangeCode = exchangeCode;
    this.ticker = ticker;
    EquitySecurityIdentifier eqId = new EquitySecurityIdentifier();
    eqId.setId(ticker);
    eqId.setIdType(FiIdentifierType.TICKER_MIC);
    eqId.setExchangeId(exchangeCode);
    setSecurityIdentifier(eqId);
  }

  @Override
  public String generateUserIdentifier() {
    return getType() + DELIMITER + ticker + DELIMITER + exchangeCode;
  }

}
