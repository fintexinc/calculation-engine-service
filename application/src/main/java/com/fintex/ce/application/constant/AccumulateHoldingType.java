package com.fintex.ce.application.constant;

/**
 * MIC holding-type codes eligible to be accumulated as Top-N candidates. The enum {@code name()} is the exact MIC code
 * compared against {@code CommonHolding.type} during aggregation, so YAML and request payloads can bind by name.
 */
public enum AccumulateHoldingType {

  /** Equity — common stock. */
  E,
  /** Equity right — subscription/warrant right attached to an equity. */
  ER,
  /** Bond — generic fixed-income bond instrument. */
  B,
  /** Bond convertible — bond convertible into the issuer's equity. */
  BC,
  /** Bond discount — zero-coupon / discount-issued bond. */
  BD,
  /** Bond treasury — government treasury bond. */
  BT;

  public String code() {
    return name();
  }
}
