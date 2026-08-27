package ca.tangerine.pce.model.domain.result.fee;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * The rate half of a fee comparison. Every number here is a <b>ratio or a percentage</b> — never an amount of money.
 *
 * <p>
 * Split out from {@link FeeComparison} because a flat bag holding both {@code 0.021} (a rate) and {@code 4841.00} (a
 * dollar amount) as bare {@link BigDecimal}s gives a reader nothing to tell them apart by. The unit now comes from the
 * holder, so {@link #portfolio()} and {@link #benchmark()} need no suffix to be unambiguous.
 *
 * <p>
 * These are called fee <i>rates</i> rather than MERs deliberately: only Canadian funds resolve to a Management Expense
 * Ratio. US funds resolve through Net Expense Ratio → Gross Expense Ratio → Management Fee and never populate an MER at
 * all, so what appears here is whichever datapoint the country's resolution chain reached.
 */
@Schema(description = "Portfolio-vs-benchmark fee rates for one aggregation view. Rates only — no amounts.")
public record FeeRateComparison(

    @Schema(description = "The portfolio's weighted-average fee rate for this view, as a ratio "
        + "(0.021 = 2.1%)") BigDecimal portfolio,

    @Schema(description = "The benchmark's weighted-average fee rate, as a ratio") BigDecimal benchmark,

    @Schema(description = "Percent difference of the portfolio rate relative to the benchmark rate: "
        + "(portfolio - benchmark) / benchmark * 100. Positive means the portfolio costs more. Null when the "
        + "benchmark rate is zero, which would make the difference undefined.") BigDecimal percentDifference,

    @Schema(description = "True only when the two rates are identical") boolean equal) {
}
