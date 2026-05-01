package com.fintex.ce.model.domain.result;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IntervalResult(LocalDate key, BigDecimal value) {
}