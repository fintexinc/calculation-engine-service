package ca.tangerine.pce.webclient.mic.mapper;

import lombok.experimental.UtilityClass;

import ca.tangerine.pce.util.FilterUtils;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;

@UtilityClass
public final class HoldingMappingUtils {

  /**
   * Converts FinancialInstrumentType to its string representation for API requests. Uses the enum name directly as it
   * already matches the MIC API format.
   */
  public static String toApiType(FinancialInstrumentType instrumentType) {
    if (instrumentType == null) {
      return null;
    }
    return instrumentType.name();
  }

  /**
   * Checks if the instrument type should be skipped (not sent to MIC API). Backed by the shared
   * {@link FilterUtils#LOCALLY_SOURCED_TYPES} so the application-side validators that must not flag these as "not
   * found" stay in lockstep with this filter.
   */
  public static boolean isSkipped(FinancialInstrumentType instrumentType) {
    return instrumentType == null || FilterUtils.LOCALLY_SOURCED_TYPES.contains(instrumentType);
  }
}
