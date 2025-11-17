package com.fintex.ce.config.constant;

import com.fintex.ce.config.enumeration.HoldingType;

import java.util.EnumSet;
import java.util.Set;

public class HoldingTypeGroup {

    public static final Set<HoldingType> FUNDS = EnumSet.of(HoldingType.CANADA_MUTUAL_FUNDS, HoldingType.US_ETF, HoldingType.CANADA_ETF, HoldingType.SEGREGATED_FUND_CANADA, HoldingType.CANADA_HEDGE_FUNDS, HoldingType.US_MUTUAL_FUNDS);
    public static final Set<HoldingType> EQUITIES = EnumSet.of(HoldingType.CANADA_STOCKS, HoldingType.US_STOCKS);

    private HoldingTypeGroup() {
    }

}
