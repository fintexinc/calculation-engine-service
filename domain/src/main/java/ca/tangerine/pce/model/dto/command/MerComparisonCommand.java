package ca.tangerine.pce.model.dto.command;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.dto.command.contract.BenchmarkHoldingsProvider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Command for the {@code mer-benchmark-comparison} metric. Carries the portfolio holdings and fee-aggregation modes
 * (inherited from {@link AverageMerCommand}) plus the benchmark to compare against. The benchmark is a list so a
 * benchmark portfolio can be sent, but the common case — the single fund the portfolio's risk rating maps to — is just
 * a one-element list. It is exposed through {@link BenchmarkHoldingsProvider} so the orchestrator fetches its fee data
 * into the benchmark section of {@code SecurityData}.
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Command for the mer-benchmark-comparison metric: portfolio holdings compared against a benchmark")
public class MerComparisonCommand extends AverageMerCommand implements BenchmarkHoldingsProvider {

  @Schema(description = "The benchmark to compare against — one fund for a single-fund benchmark, "
      + "several for a benchmark portfolio. Holding values are the weights of the benchmark's average MER; "
      + "when none are supplied the holdings are weighted equally.")
  private List<PortfolioHolding> benchmarkHoldings;
}
