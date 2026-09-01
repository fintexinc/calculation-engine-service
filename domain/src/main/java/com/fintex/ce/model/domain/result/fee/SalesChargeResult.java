package com.fintex.ce.model.domain.result.fee;

import com.fintex.ce.model.domain.enumeration.SalesChargeCategory;
import com.fintex.ce.model.domain.result.BaseCalculationResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import static com.fintex.ce.model.util.BigDecimalUtils.bigDecimalEquals;
import static com.fintex.ce.model.util.BigDecimalUtils.bigDecimalHashCode;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "Response for sales-charge metric. Contains sales charge breakdown by category.")
public class SalesChargeResult extends BaseCalculationResult {

  @Schema(description = "Sales charge breakdown by category")
  @Builder.Default
  private Map<SalesChargeCategory, SalesChargeEntry> salesCharges = new EnumMap<>(SalesChargeCategory.class);

  public record SalesChargeEntry(BigDecimal allocation, BigDecimal value, Map<String, BigDecimal> holdings) {

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
}
