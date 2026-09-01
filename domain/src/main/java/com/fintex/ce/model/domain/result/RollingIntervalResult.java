package com.fintex.ce.model.domain.result;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record RollingIntervalResult(String period, Map<LocalDate, BigDecimal> values) {
}
