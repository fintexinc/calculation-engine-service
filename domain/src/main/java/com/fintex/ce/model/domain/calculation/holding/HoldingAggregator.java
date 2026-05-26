package com.fintex.ce.model.domain.calculation.holding;

import java.util.Objects;

/**
 * Aggregation key for grouping leaves that represent the same underlying entity. Equality is decided by the "display"
 * identity returned by {@link #nameOrCompanyName()} so equity leaves with the same companyName and non-equity leaves
 * with the same name group together.
 */
public record HoldingAggregator(String name, String companyName) {

  public String nameOrCompanyName() {
    return (name == null || name.isEmpty()) ? companyName : name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof HoldingAggregator that)) return false;
    return Objects.equals(this.nameOrCompanyName(), that.nameOrCompanyName());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(nameOrCompanyName());
  }
}
