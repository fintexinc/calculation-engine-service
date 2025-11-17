package com.fintex.ce.domain.monthlyreturns;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.holding.MonthlyReturnGeneratableHolding;
import com.fintex.ce.model.redis.RMonthlyReturns;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.fintex.ce.util.DateTimeUtils.rangeWithLastDayOfMonth;
import static com.fintex.ce.util.DecimalUtils.divide;
import static com.fintex.ce.util.DecimalUtils.pow;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;

@Component
public class MonthlyReturnsGenerator {

    public Map<Holding, RMonthlyReturns> generateGicMonthlyReturns(List<Holding> holdings) {
        Map<Holding, RMonthlyReturns> result = new HashMap<>();
        List<Holding> filteredHoldings = filterHoldings(holdings, GIC_PREDICATE);
        for (Holding h : filteredHoldings) {
            result.put(h, generatedMonthlyReturns((MonthlyReturnGeneratableHolding) h));
        }
        return result;
    }

    private RMonthlyReturns generatedMonthlyReturns(MonthlyReturnGeneratableHolding holding) {
        TreeMap<LocalDate, BigDecimal> returns = generateReturns(holding);
        return createRMonthlyReturns(returns, holding.getCurrency());
    }

    public TreeMap<LocalDate, BigDecimal> generateReturns(MonthlyReturnGeneratableHolding holding) {
        BigDecimal monthlyReturn = calculateMonthlyReturn(holding.getClientIntRate(), getCompoundingFrequency(holding));

        TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>();
        rangeWithLastDayOfMonth(holding.getInvestmentDate(), LocalDate.now())
                .forEach(localDate -> returns.put(localDate, monthlyReturn));
        return returns;
    }

    private BigDecimal calculateMonthlyReturn(BigDecimal interestRate, BigDecimal compoundingFrequency) {
        BigDecimal oneDividedByTwelve = divide(BigDecimal.ONE, BigDecimal.valueOf(12));
        BigDecimal interestRateDividedByCompoundingFrequency = divide(interestRate, compoundingFrequency);
        BigDecimal yearlyReturn = pow(convert(interestRateDividedByCompoundingFrequency), compoundingFrequency);
        return pow(yearlyReturn, oneDividedByTwelve).subtract(BigDecimal.ONE);
    }

    private BigDecimal convert(BigDecimal interestRate) {
        return divide(interestRate, BigDecimal.valueOf(100)).add(BigDecimal.ONE);
    }

    private BigDecimal getCompoundingFrequency(MonthlyReturnGeneratableHolding gicHolding) {
        return gicHolding.getInterestFreq().getFrequency();
    }

    private RMonthlyReturns createRMonthlyReturns(TreeMap<LocalDate, BigDecimal> returns, Currency currency) {
        RMonthlyReturns rMonthlyReturns = new RMonthlyReturns();
        rMonthlyReturns.setReturns(returns);
        rMonthlyReturns.setCurrency(currency.name());
        rMonthlyReturns.setHoldingType(HoldingType.GIC);
        return rMonthlyReturns;
    }

}
