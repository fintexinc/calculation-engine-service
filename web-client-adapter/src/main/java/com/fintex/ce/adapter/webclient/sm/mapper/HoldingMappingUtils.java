package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class HoldingMappingUtils {

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
   * Checks if the instrument type should be skipped (not sent to SMS API). Backed by the shared
   * {@link FilterUtils#NOT_SENT_TO_SM_TYPES} so the application-side validators that must not flag these as "not found"
   * stay in lockstep with this filter.
   */
  public static boolean isSkipped(FinancialInstrumentType instrumentType) {
    return instrumentType == null || FilterUtils.NOT_SENT_TO_SM_TYPES.contains(instrumentType);
  }
}
