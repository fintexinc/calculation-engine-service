package com.fintex.ce.domain.calculation;

import com.fintex.ce.config.enumeration.calculation.SalesCharge;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.SalesChargeResDtos;
import com.fintex.ce.model.redis.RSalesCharge;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fintex.ce.util.DecimalUtils.divide;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.*;

public class SalesChargeCalculation {

    private final Map<Holding, RSalesCharge> salesCharges;
    private final BigDecimal sumOfAllMarketValues;

    static final SalesChargeResDtos.SalesChargeResDto DEFAULT_SALES_CHARGE_DTO = new SalesChargeResDtos.SalesChargeResDto(ZERO, ZERO, Set.of());
    static final Map<SalesCharge, SalesChargeResDtos.SalesChargeResDto> DEFAULT_MAP = new EnumMap<>(SalesCharge.class);

    static {
        Stream.of(SalesCharge.values()).forEach(type -> DEFAULT_MAP.put(type, DEFAULT_SALES_CHARGE_DTO));
    }

    public SalesChargeCalculation(final Map<Holding, RSalesCharge> salesCharges) {
        this.salesCharges = salesCharges;
        this.sumOfAllMarketValues = getSumOfMarketValues(salesCharges.keySet());
    }

    public SalesChargeResDtos calculate() {
        if (CollectionUtils.isEmpty(salesCharges)) {
            return new SalesChargeResDtos().setSalesCharges(DEFAULT_MAP);
        }
        return calculateSalesCharge();
    }

    /**
     * calculates allocation, value and holdings for each sales charge type.
     * divide sum of mutual funds of particular sales charge type to sum of all mutual funds in portfolio results in 'allocation' of sales charge type
     * sum of mutual funds of particular sales charge type results in 'value' of sales charge type
     *
     * @return sales charges with allocation, value and holdings with respective allocations
     */
    private SalesChargeResDtos calculateSalesCharge() {
        final Map<SalesCharge, SalesChargeResDtos.SalesChargeResDto> result = new EnumMap<>(DEFAULT_MAP);

        final Map<SalesCharge, Set<Holding>> groupedHoldingsBySalesCharge = groupBySalesChargeTypes(salesCharges);
        groupedHoldingsBySalesCharge.forEach((salesCharge, holdingSet) -> {
            final BigDecimal allocation = calculateAllocation(holdingSet);
            final BigDecimal value = getSumOfMarketValues(holdingSet);
            final Set<SalesChargeResDtos.SalesChargeHoldingResDto> salesChargeHoldingResDtos = getSalesChargeHoldingResDtos(holdingSet);

            result.put(salesCharge, new SalesChargeResDtos.SalesChargeResDto(allocation, value, salesChargeHoldingResDtos));
        });

        return new SalesChargeResDtos().setSalesCharges(result);
    }

    private BigDecimal calculateAllocation(final Set<Holding> holdingSet) {
        return toUserScale(divide(getSumOfMarketValues(holdingSet), sumOfAllMarketValues));
    }

    private Set<SalesChargeResDtos.SalesChargeHoldingResDto> getSalesChargeHoldingResDtos(final Set<Holding> holdingSet) {
        return holdingSet.stream()
                .map(holding -> new SalesChargeResDtos.SalesChargeHoldingResDto(holding.generateUserIdentifier(), getMutualFundAllocation(holding)))
                .collect(Collectors.toSet());
    }

    private BigDecimal getMutualFundAllocation(final Holding holding) {
        return toUserScale(divide(holding.getValue(), sumOfAllMarketValues));
    }

    /**
     * groups holdings by sales charge types.
     *
     * @param salesCharges map of holdings as key and sales charge type as value.
     * @return grouped holdings.
     */
    private Map<SalesCharge, Set<Holding>> groupBySalesChargeTypes(final Map<Holding, RSalesCharge> salesCharges) {
        return salesCharges.entrySet().stream().collect(groupingBy(e -> SalesCharge.of(e.getValue().getValue()),
                mapping(Map.Entry::getKey, toSet())));
    }

    /**
     * calculates sum of market values, uses holding values from provided set of holdings.
     */
    private BigDecimal getSumOfMarketValues(final Set<Holding> holdings) {
        return holdings.stream().map(Holding::getValue).reduce(ZERO, BigDecimal::add);
    }
}
