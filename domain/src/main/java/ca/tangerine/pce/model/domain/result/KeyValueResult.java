package ca.tangerine.pce.model.domain.result;

import java.math.BigDecimal;

public record KeyValueResult<T>(T key, BigDecimal value) {
}