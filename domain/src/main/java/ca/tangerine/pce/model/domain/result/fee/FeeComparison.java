package ca.tangerine.pce.model.domain.result.fee;

import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single aggregation view's fee comparison, in two parts: the rates, and what those rates cost.
 *
 * <p>
 * The two are deliberately separate holders rather than one flat set of fields. Previously this class carried two
 * rates, a percentage, a boolean and a map of amounts side by side as bare {@code BigDecimal}s, and nothing at a call
 * site distinguished {@code 0.021} from {@code 4841.00} — a reader had to remember which fields were ratios and which
 * were money. Now the holder names the unit: everything in {@link FeeRateComparison} is a rate, everything in
 * {@link FeeSpendComparison} is an amount.
 *
 * <p>
 * Named for the fee rather than for the MER: only Canadian funds resolve to a Management Expense Ratio, while US funds
 * resolve through Net Expense Ratio → Gross Expense Ratio → Management Fee and never populate an MER at all. What is
 * compared here is whichever fee rate the country's resolution chain reached.
 *
 * <p>
 * Both parts describe the same pool of money. The rates are weighted over this view's holding set, and the amounts are
 * charged against that same view's asset base — funds-only market value for the funds-only view, whole-portfolio value
 * for the whole-portfolio view (see {@link AverageMerResult#getBaseValue()}). The engine never mixes denominators
 * across views.
 *
 * <p>
 * Both parts are always fully populated. A view that cannot be compared — no fee rate on one of the sides, or no asset
 * base to charge the rates against — is reported as {@code FEE_COMPARISON_NOT_AVAILABLE} rather than as a body of nulls
 * a caller would have to interpret.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Portfolio-vs-benchmark fee comparison for one aggregation view, separated into rates and amounts")
public class FeeComparison {

  @Schema(description = "The fee rates being compared, as ratios")
  private FeeRateComparison feeRate;

  @Schema(description = "Projected fee amounts for both sides and the resulting saving, keyed by period. Holding "
      + "values are FX-converted into the request's targetCurrency — or into calculation.fx.default-target-currency "
      + "(CAD unless overridden) when the request omits it — before weighting. ONE_YR is the annual figure.")
  private Map<TimePeriod, FeeSpendComparison> spend;
}
