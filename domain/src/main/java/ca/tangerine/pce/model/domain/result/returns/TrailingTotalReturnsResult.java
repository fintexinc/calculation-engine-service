package ca.tangerine.pce.model.domain.result.returns;

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

import ca.tangerine.pce.model.domain.result.PeriodResult;
import ca.tangerine.pce.model.domain.result.TimeIntervalResult;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;
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
