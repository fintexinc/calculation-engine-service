package ca.tangerine.pce.model.domain.result.returns;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Portfolio-versus-benchmark comparison for one return metric observation")
public record ReturnComparison<T>(

    @Schema(description = "Time period, calendar year, or valuation date") T period,

    @Schema(description = "Portfolio metric value") BigDecimal portfolio,

    @Schema(description = "Benchmark metric value") BigDecimal benchmark,

    @Schema(description = "Percent difference of the portfolio value relative to the benchmark value: "
        + "(portfolio - benchmark) / benchmark * 100. Null when either value is unavailable or the benchmark "
        + "value is zero, which would make the difference undefined.") BigDecimal percentDifference) {
}
