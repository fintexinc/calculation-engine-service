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
public class CanadaPooledFundHolding extends Holding {

    private String morningstarId;

    public CanadaPooledFundHolding() {
    }

    public CanadaPooledFundHolding(BigDecimal amount, String morningstarId) {
        super(amount, HoldingType.CANADA_POOLED_FUNDS);
        this.morningstarId = morningstarId;
    }

    @Override
    public String generateUserIdentifier() {
        return getType() + DELIMITER + morningstarId;
    }

}
