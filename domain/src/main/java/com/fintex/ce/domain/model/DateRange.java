package com.fintex.ce.domain.model;

import com.google.common.collect.Range;

import java.time.LocalDate;

public record DateRange(LocalDate from, LocalDate to) {

  public Range<LocalDate> toRange() {
    return Range.closed(from, to);
  }
}