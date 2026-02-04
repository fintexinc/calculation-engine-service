package com.fintex.ce.adapter.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintex.ce.domain.enumeration.calculation.SalesCharge;
import com.fintex.ce.adapter.rest.dto.response.core.ErrorDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class SalesChargeResDtos extends ErrorDTO {

  private Map<SalesCharge, SalesChargeResDto> salesCharges = new EnumMap<>(SalesCharge.class);

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SalesChargeResDto {
    private BigDecimal allocation;
    private BigDecimal value;
    private Set<SalesChargeHoldingResDto> holdings;

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final SalesChargeResDto that = (SalesChargeResDto) o;

      if (allocation != null ? !(allocation.compareTo(that.allocation) == 0) : that.allocation != null) return false;
      if (value != null ? !(value.compareTo(that.value) == 0) : that.value != null) return false;
      return holdings != null ? holdings.equals(that.holdings) : that.holdings == null;
    }

    @Override
    public int hashCode() {
      int result = allocation != null ? allocation.stripTrailingZeros().hashCode() : 0;
      result = 31 * result + (value != null ? value.stripTrailingZeros().hashCode() : 0);
      result = 31 * result + (holdings != null ? holdings.hashCode() : 0);
      return result;
    }
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SalesChargeHoldingResDto {
    private String fundServCode;
    @JsonProperty("mfAllocation")
    private BigDecimal mutualFundAllocation;

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final SalesChargeHoldingResDto that = (SalesChargeHoldingResDto) o;

      if (fundServCode != null ? !fundServCode.equals(that.fundServCode) : that.fundServCode != null)
        return false;
      return mutualFundAllocation != null
          ? (mutualFundAllocation.compareTo(that.mutualFundAllocation) == 0)
          : that.mutualFundAllocation == null;
    }

    @Override
    public int hashCode() {
      int result = fundServCode != null ? fundServCode.hashCode() : 0;
      result = 31 * result + (mutualFundAllocation != null ? mutualFundAllocation.stripTrailingZeros().hashCode() : 0);
      return result;
    }
  }

}
