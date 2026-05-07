package com.fintex.ce.model.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Determines which holdings are weighted into the average fee, and when the result is null. Wire format (JSON)
 * preserves the original lowercase values for backwards compatibility.
 */
public enum FeeAggregationMode {

  /**
   * Fund (MER-bearing) holdings only. Weights normalised within the fund subset. Returns null when no fund holding is
   * present in the portfolio.
   */
  FUNDS_ONLY("scaled"),

  /**
   * All holdings. Weights normalised across the whole portfolio; non-fund holdings (stocks, cash, GIC, fixed income)
   * contribute 0%.
   */
  WHOLE_PORTFOLIO("absolute"),

  /**
   * Same holding set as {@link #FUNDS_ONLY}, but returns null if any included holding is missing its primary fee
   * datapoint, regardless of whether the secondary datapoint is available. Use when the caller wants a true reported
   * fee or nothing at all.
   */
  FUNDS_ONLY_STRICT("forceReportFee");

  private final String name;

  FeeAggregationMode(final String name) {
    this.name = name;
  }

  @JsonCreator
  public static FeeAggregationMode fromJson(String name) {
    for (FeeAggregationMode t : values()) {
      if (t.name().equalsIgnoreCase(name) || t.getName().equalsIgnoreCase(name)) {
        return t;
      }
    }
    throw new IllegalArgumentException("Unknown FeeAggregationMode: '" + name
        + "'. Expected one of: scaled, absolute, forceReportFee.");
  }

  @JsonValue
  public String getName() {
    return name;
  }

}
