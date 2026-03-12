package com.fintex.ce.adapter.webclient.mapper;

import com.fintex.ce.domain.enumeration.HoldingType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public final class HoldingTypeMapper {

  private static final Map<HoldingType, String> MAPPING = new EnumMap<>(HoldingType.class);
  private static final Set<HoldingType> SKIPPED = Set.of(
      HoldingType.CASH,
      HoldingType.GIC,
      HoldingType.PAG_GUIDED_PORTFOLIO);

  static {
    MAPPING.put(HoldingType.CANADA_MUTUAL_FUNDS, "MUTUAL_FUND_CANADA");
    MAPPING.put(HoldingType.SEGREGATED_FUND_CANADA, "SEGREGATED_FUND_CANADA");
    MAPPING.put(HoldingType.CANADA_ETF, "ETF_CANADA");
    MAPPING.put(HoldingType.US_ETF, "ETF_US");
    MAPPING.put(HoldingType.CANADA_STOCKS, "STOCK_CANADA");
    MAPPING.put(HoldingType.US_STOCKS, "STOCK_US");
    MAPPING.put(HoldingType.BENCHMARK_INDEX, "BENCHMARK_INDEX");
    MAPPING.put(HoldingType.US_MUTUAL_FUNDS, "MUTUAL_FUND_US");
    MAPPING.put(HoldingType.CANADA_HEDGE_FUNDS, "HEDGE_FUND_CANADA");
    MAPPING.put(HoldingType.CANADA_POOLED_FUNDS, "POOLED_FUND_CANADA");
    MAPPING.put(HoldingType.FIXED_INCOME, "FIXED_INCOME");
    MAPPING.put(HoldingType.SEPARATELY_MANAGED_ACCOUNT, "SEPARATELY_MANAGED_ACCOUNT");
  }

  private HoldingTypeMapper() {
  }

  public static String toFinancialInstrumentType(HoldingType holdingType) {
    return MAPPING.get(holdingType);
  }

  public static boolean isSkipped(HoldingType holdingType) {
    return SKIPPED.contains(holdingType);
  }
}
