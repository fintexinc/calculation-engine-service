package com.fintex.ce.domain.monthlyreturns;

import com.fintex.ce.dto.holding.Holding;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

@EqualsAndHashCode
public class ReturnsCutComponent {

    public Map<Holding, TreeMap<LocalDate, BigDecimal>> cutReturnsByEndDate(final Map<Holding, TreeMap<LocalDate, BigDecimal>> returns,
                                                                            final LocalDate endDate) {
        return filterReturnsDates(returns, (dateOfReturn) -> dateOfReturn.isAfter(endDate));
    }

    public Map<Holding, TreeMap<LocalDate, BigDecimal>> cutReturnsByStartDate(final Map<Holding, TreeMap<LocalDate, BigDecimal>> returns,
                                                                          final LocalDate startDate) {
        return filterReturnsDates(returns, (monthlyReturn) -> monthlyReturn.isBefore(startDate));
    }

    private Map<Holding, TreeMap<LocalDate, BigDecimal>> filterReturnsDates(final Map<Holding, TreeMap<LocalDate, BigDecimal>> returns,
                                                                        final Predicate<LocalDate> returnDateFilter) {
        final HashMap<Holding, TreeMap<LocalDate, BigDecimal>> copyOfReturns = new HashMap<>(returns);
        copyOfReturns.forEach((key, value) -> value.entrySet().removeIf(i -> returnDateFilter.test(i.getKey())));
        return copyOfReturns;
    }
}
