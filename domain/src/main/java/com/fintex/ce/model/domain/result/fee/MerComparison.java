package com.fintex.ce.model.domain.result.fee;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single view's comparison of the portfolio's MER against the benchmark's MER. Numeric fields are {@code null} when
 * either side's MER is unavailable for the view.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Portfolio-vs-benchmark MER comparison for one aggregation view")
public class MerComparison {

  @Schema(description = "The portfolio's weighted-average MER for this view, as a ratio")
  private BigDecimal portfolioMer;

  @Schema(description = "The benchmark's weighted-average MER, as a ratio")
  private BigDecimal benchmarkMer;

  @Schema(description = "Percent difference of the portfolio MER relative to the benchmark MER: "
      + "(portfolioMer - benchmarkMer) / benchmarkMer * 100. Positive means the portfolio costs more.")
  private BigDecimal percentDifference;

  @Schema(description = "True only when the portfolio MER is identical to the benchmark MER")
  private boolean equal;

  @Schema(description = "Annual dollar impact = (benchmarkMer - portfolioMer) * this view's asset base "
      + "(funds-only market value for the funds-only view, whole-portfolio market value for the whole-portfolio "
      + "view), in the request's targetCurrency — or the service's configured reporting currency (CAD) when the "
      + "request omits it. Holding values are FX-converted into that currency before weighting. "
      + "Positive means the portfolio costs less than the benchmark; negative means it costs more.")
  private BigDecimal annualDollarImpact;
}
