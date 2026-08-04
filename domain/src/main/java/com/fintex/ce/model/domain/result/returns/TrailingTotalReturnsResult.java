package com.fintex.ce.model.domain.result.returns;

import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
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
  private List<TimeIntervalResult> trailingTotalReturn;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Schema(description = "Portfolio-versus-benchmark return comparison per time interval period")
  private List<ReturnComparison<TimePeriod>> comparison;

  public TrailingTotalReturnsResult(Collection<TimeIntervalResult> trailingTotalReturn) {
    setTrailingTotalReturn(trailingTotalReturn);
  }

  public void setTrailingTotalReturn(Collection<TimeIntervalResult> trailingTotalReturn) {
    this.trailingTotalReturn = trailingTotalReturn.stream()
        .sorted(Comparator.comparing(TimeIntervalResult::period))
        .toList();
  }
}
