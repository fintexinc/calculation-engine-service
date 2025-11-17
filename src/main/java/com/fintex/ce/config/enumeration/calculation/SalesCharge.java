package com.fintex.ce.config.enumeration.calculation;

import com.fintex.ce.exception.SystemException;
import com.fintex.ce.exception.code.ErrorCode;
import com.fintex.smclient.graphql.SalesChargeType;
import lombok.Getter;

import java.util.Set;

import static com.fintex.smclient.graphql.SalesChargeType.*;

@Getter
public enum SalesCharge {

    NO_LOAD_INITIAL_SALES_CHARGE(Set.of(FRONT_END_CHARGE, VOLUME_SALES_CHARGE, FORMULA_ONE, NO_SALES_OR_REDEMPTION_CHARGE)),
    LOW_LOAD_SALES_CHARGE(Set.of(LOW_SALES_CHARGE)),
    DEFERRED_SALES_CHARGE(Set.of(DEFERRED_SALES_CHARGE_ON_MARKET_VALUE, DEFERRED_CHARGE_ON_ORIGINAL_AMOUNT, SalesChargeType.DEFERRED_SALES_CHARGE,
            GROUP_SALES_CHARGE, REDEMPTION_CHARGE));

    private final Set<SalesChargeType> types;

    SalesCharge(final Set<SalesChargeType> types) {
        this.types = types;
    }

    public static SalesCharge of(final String type) {
        for (final SalesCharge value : values()) {
            if (value.getTypes().contains(SalesChargeType.valueOf(type.toUpperCase()))) {
                return value;
            }
        }
        final String message = String.format("Could not find Sales Charge type for %s", type);
        throw new SystemException(message, ErrorCode.INTERNAL_SERVER_ERROR);
    }

}
