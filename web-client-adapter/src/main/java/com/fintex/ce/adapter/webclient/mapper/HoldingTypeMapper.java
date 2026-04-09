package com.fintex.ce.adapter.webclient.mapper;

import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;

import java.util.Set;

public final class HoldingTypeMapper {

  // TODO: Ask business requirements for PAG_GUIDED_PORTFOLIO - it is not present in FinancialInstrumentType enum
  private static final Set<FinancialInstrumentType> SKIPPED_INSTRUMENT_TYPES = Set.of(
      FinancialInstrumentType.CASH,
      FinancialInstrumentType.GIC);

  private HoldingTypeMapper() {
  }

  /**
   * Converts FinancialInstrumentType to its string representation for API requests. Uses the enum name directly as it
   * already matches the SMS API format.
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
