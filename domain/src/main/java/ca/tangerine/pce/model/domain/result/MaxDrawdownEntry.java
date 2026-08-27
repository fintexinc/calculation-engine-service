package ca.tangerine.pce.model.domain.result;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MaxDrawdownEntry(
    String period,
    BigDecimal value,
    LocalDate drawdownStartDate,
    LocalDate drawdownTroughDate,
    Integer recoveryTime) {
}
