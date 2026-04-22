package com.fintex.ce.model.domain.result.correlation;

import com.fintex.ce.model.domain.result.PeriodResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Response for correlation metric. Contains correlation matrix between portfolio holdings and benchmark.")
public class CorrelationResult extends PeriodResult {

  @Schema(description = "Legend mapping holdings to correlation matrix keys")
  private List<HoldingsKeyResult> holdingsKey;
  @Schema(description = "Correlation values per time interval period")
  private List<CorrelationPeriodResult> correlationPeriods;
}
