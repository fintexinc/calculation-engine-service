package com.fintex.ce.dto.holding;

import com.fintex.ce.config.enumeration.HoldingType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

import static com.fintex.ce.config.constant.GeneralConstants.DELIMITER;

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
    }

    @Override
    public String generateUserIdentifier() {
        return getType() + DELIMITER + ticker + DELIMITER + exchangeCode;
    }

}
