package com.fintex.ce.adapter.webclient.mapper;

import com.fintex.ce.domain.model.enumeration.HoldingType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public final class HoldingTypeMapper {

  private static final Map<HoldingType, String> MAPPING = new EnumMap<>(HoldingType.class);
  private static final Set<HoldingType> SKIPPED = Set.of(
      HoldingType.CASH,
      HoldingType.GIC,
      HoldingType.PAG_GUIDED_PORTFOLIO);

  private static final Set<FinancialInstrumentType> SKIPPED_INSTRUMENT_TYPES = Set.of(
      FinancialInstrumentType.CASH,
      FinancialInstrumentType.GIC);

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

  @Deprecated(forRemoval = true)
  public static String toFinancialInstrumentType(HoldingType holdingType) {
    return MAPPING.get(holdingType);
  }

  @Deprecated(forRemoval = true)
  public static boolean isSkipped(HoldingType holdingType) {
    return SKIPPED.contains(holdingType);
  }

  /**
   * Converts FinancialInstrumentType to its string representation for API requests.
   * Uses the enum name directly as it already matches the SMS API format.
   */
  public static String toApiType(FinancialInstrumentType instrumentType) {
    if (instrumentType == null) {
      return null;
    }
    return instrumentType.name();
  }

  /**
   * Checks if the instrument type should be skipped (not sent to SMS API).
   */
  public static boolean isSkipped(FinancialInstrumentType instrumentType) {
    return instrumentType == null || SKIPPED_INSTRUMENT_TYPES.contains(instrumentType);
  }
}
