package com.fintex.ce.model.domain.result;

import java.util.Set;

public record RollingIntervalResult(String period, Set<IntervalResult> values) {
}