package com.fintex.ce.model.domain.result.returns;

import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response for trailing-total-return metric. Contains trailing total returns per time interval period.")
public class TrailingTotalReturnsResult extends PeriodResult {

  @Schema(description = "Trailing total returns per time interval period")
  private Map<String, BigDecimal> trailingTotalReturn;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Schema(description = "Portfolio-versus-benchmark return comparison per time interval period")
  private List<ReturnComparison<TimePeriod>> comparison;

  public TrailingTotalReturnsResult(Map<String, BigDecimal> trailingTotalReturn) {
    setTrailingTotalReturn(trailingTotalReturn);
  }

  /**
   * Keeps the returns keyed by period name and ordered by that key, so the response order is deterministic. Values may
   * be {@code null} for periods without enough data, so entries are collected manually rather than via toMap.
   */
  public void setTrailingTotalReturn(Map<String, BigDecimal> trailingTotalReturn) {
    this.trailingTotalReturn = trailingTotalReturn == null
        ? null
        : trailingTotalReturn.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), Map::putAll);
  }
}
