package com.fintex.ce.model.domain.calculation.holding;

import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class HoldingAggregator {

  private final String name;
  private final String companyName;

  // This field is using only for identifying GIC holdings, as it is possible that all parameters of GIC are equals but
  // them aren't same
  private UUID uuid;

  public String getNameOrCompanyName() {
    return (name == null || name.isEmpty()) ? companyName : name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    HoldingAggregator that = (HoldingAggregator) o;
    return this.getNameOrCompanyName().equals(that.getNameOrCompanyName()) && Objects.equals(uuid, that.uuid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getNameOrCompanyName());
  }
}
