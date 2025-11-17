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
public class UsMutualFundHolding extends Holding {

    private String ticker;

    public UsMutualFundHolding() {
    }

    public UsMutualFundHolding(BigDecimal amount, String ticker) {
        super(amount, HoldingType.US_MUTUAL_FUNDS);
        this.ticker = ticker;
    }

    @Override
    public String generateUserIdentifier() {
        return getType() + DELIMITER + ticker;
    }

}
