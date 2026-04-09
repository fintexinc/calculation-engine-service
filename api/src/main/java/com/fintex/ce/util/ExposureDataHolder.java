package com.fintex.ce.util;

import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Immutable result of allocation mapping, containing both the mapped allocations and any warnings produced during the
 * mapping process. Keeps mapping functions pure by returning warnings as part of the result instead of mutating an
 * external list.
 *
 * @param <E>
 *          the enum type used as allocation keys (e.g., MaturityAllocationType, FixedIncomeStyleBoxType)
 */
public record ExposureDataHolder<E>(
    Map<Holding, Map<E, BigDecimal>> allocations,
    List<Warning> warnings) {
}