package com.fintex.ce.model.domain.enumeration;

import lombok.Getter;

@Getter
public enum Frequency {
  ANNUAL(12),
  SEMI_ANNUAL(6),
  MONTHLY(1);

  private final int frequency;

  Frequency(int frequency) {
    this.frequency = frequency;
  }

  public static Frequency fromValue(int frequency) {
    for (Frequency value : values()) {
      if (value.getFrequency() == frequency) {
        return value;
      }
    }
    return null;
  }
}
