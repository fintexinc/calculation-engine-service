package com.fintex.ce.repository.graphql.query.endpoint.maturityallocation;

import com.fintex.smclient.graphql.*;
import com.fintex.ce.config.enumeration.calculation.MaturityAllocationType;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.model.redis.RMaturityAllocation;
import com.fintex.ce.repository.graphql.query.endpoint.core.FixedIncomeAbstractEndpoint;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import org.joda.time.Years;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_FIXED_INCOME_BY_ADP_NUMBERS;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_DATAPOINT_QUERY_DEFINITION;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.FIXED_INCOME;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MATURITY_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;


public class MaturityAllocationFixedIncomeEndpoint extends FixedIncomeAbstractEndpoint<RMaturityAllocation> {

    public MaturityAllocationFixedIncomeEndpoint() {
        super(GET_FIXED_INCOME_BY_ADP_NUMBERS, List.of(), buildCacheName(MATURITY_ALLOCATION, FIXED_INCOME));
    }

    @Override
    public FixedIncomeQuery requestMapper(FixedIncomeQuery query) {
        return query.maturityDate(STRING_DATAPOINT_QUERY_DEFINITION);
    }

    @Override
    public RMaturityAllocation responseMapper(FixedIncome fixedIncome, FixedIncomeHolding holding) {
        final var rMaturityAllocation = new RMaturityAllocation();
        final var maturityAllocationType = getMaturityAllocationType(fixedIncome);
        if (maturityAllocationType == null) {
            return rMaturityAllocation;
        }
        final var maturityDurationValues = Map.of(maturityAllocationType, BigDecimal.ONE);
        rMaturityAllocation.setMaturityDurationValues(maturityDurationValues);
        return rMaturityAllocation;
    }

    private String getMaturityAllocationType(FixedIncome fixedIncome) {
        if (Objects.isNull(fixedIncome.getMaturityDate())) {
            return null;
        }
        return mapMaturityDurationType(fixedIncome.getMaturityDate());
    }

    private String mapMaturityDurationType(StringDatapoint maturityDateDatapoint) {
        final var maturityDate = LocalDate.parse(maturityDateDatapoint.getValue());
        final var currentDate = LocalDate.now();
        final var yearsBetween = Years.yearsBetween(currentDate, maturityDate).getYears();
        final var daysRemaining = Days.daysBetween(currentDate.plusYears(yearsBetween), maturityDate).getDays();
        if (yearsBetween < 0 || daysRemaining < 0) {
            return null;
        }
        return MaturityAllocationType.of(yearsBetween, daysRemaining).name();
    }

}
