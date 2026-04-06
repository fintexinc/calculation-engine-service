package com.fintex.ce.domain.model.result;

import com.fintex.ce.domain.model.calculation.SalesChargeCategory;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import static com.fintex.ce.domain.util.BigDecimalUtils.bigDecimalEquals;
import static com.fintex.ce.domain.util.BigDecimalUtils.bigDecimalHashCode;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class SalesChargeResult extends ErrorResult {

  private Map<SalesChargeCategory, SalesChargeEntry> salesCharges = new EnumMap<>(SalesChargeCategory.class);

  public record SalesChargeEntry(BigDecimal allocation, BigDecimal value, Set<SalesChargeHoldingEntry> holdings) {

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final SalesChargeEntry that = (SalesChargeEntry) o;

      if (!bigDecimalEquals(allocation, that.allocation)) return false;
      if (!bigDecimalEquals(value, that.value)) return false;
      return Objects.equals(holdings, that.holdings);
    }

    @Override
    public int hashCode() {
      int result = bigDecimalHashCode(allocation);
      result = 31 * result + bigDecimalHashCode(value);
      result = 31 * result + (holdings != null ? holdings.hashCode() : 0);
      return result;
    }
  }

  public record SalesChargeHoldingEntry(String fundServCode, BigDecimal mutualFundAllocation) {

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final SalesChargeHoldingEntry that = (SalesChargeHoldingEntry) o;

      if (!Objects.equals(fundServCode, that.fundServCode)) return false;
      return bigDecimalEquals(mutualFundAllocation, that.mutualFundAllocation);
    }

    @Override
    public int hashCode() {
      int result = fundServCode != null ? fundServCode.hashCode() : 0;
      result = 31 * result + bigDecimalHashCode(mutualFundAllocation);
      return result;
    }
  }
}
