package com.fintex.ce.model.domain.result.fee;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * The money half of a fee comparison, for one horizon. Every number here is an <b>amount of money</b> — never a rate.
 *
 * <p>
 * Named for what it holds rather than for the horizon it belongs to (it was {@code FeeHorizon}): the horizon is the map
 * key, and what this record carries is three amounts. The unit comes from the holder, so {@link #portfolio()} and
 * {@link #benchmark()} need no {@code Spend} suffix to be unambiguous.
 *
 * <p>
 * All three amounts are in the request's {@code targetCurrency} — or the configured reporting currency when it is
 * omitted — and are charged against the same asset base, the one behind the aggregation view this horizon belongs to,
 * so they are directly comparable.
 *
 * <p>
 * {@code savings} is stated in the direction the report reads: positive means moving to the benchmark / recommended
 * fund costs less than staying. A consumer wanting the opposite orientation can subtract the two amounts the other way,
 * which is why both are carried rather than the difference alone.
 */
@Schema(description = "Projected fee amounts for the portfolio and the benchmark over one horizon, plus the saving. "
    + "Amounts only — no rates.")
public record FeeSpendComparison(

    @Schema(description = "Fee the portfolio is projected to pay over this horizon") BigDecimal portfolio,

    @Schema(description = "Fee the benchmark / recommended fund would cost over the same horizon and asset "
        + "base") BigDecimal benchmark,

    @Schema(description = "portfolio − benchmark. Positive means switching saves money; negative means the benchmark "
        + "is dearer. Never clamped to zero.") BigDecimal savings) {
}
