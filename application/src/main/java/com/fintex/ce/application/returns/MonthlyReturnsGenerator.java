package com.fintex.ce.application.returns;

import com.fintex.ce.domain.model.HoldingMonthlyReturns;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.MonthlyReturnGeneratableHolding;
import com.fintex.ce.returns.ReturnsGenerator;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;

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
public class MonthlyReturnsGenerator implements ReturnsGenerator {

  @Override
  public Map<Holding, HoldingMonthlyReturns> generateGicMonthlyReturns(List<Holding> holdings) {
    Map<Holding, HoldingMonthlyReturns> result = new HashMap<>();
    List<Holding> filteredHoldings = filterHoldings(holdings, GIC_PREDICATE);
    for (Holding h : filteredHoldings) {
      result.put(h, generatedMonthlyReturns((MonthlyReturnGeneratableHolding) h));
    }
    return result;
  }

  private HoldingMonthlyReturns generatedMonthlyReturns(MonthlyReturnGeneratableHolding holding) {
    TreeMap<LocalDate, BigDecimal> returns = generateReturns(holding);
    return createMonthlyReturns(returns, holding.getCurrency());
  }

  @Override
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

  private HoldingMonthlyReturns createMonthlyReturns(TreeMap<LocalDate, BigDecimal> returns, CurrencyType currency) {
    HoldingMonthlyReturns monthlyReturns = new HoldingMonthlyReturns();
    monthlyReturns.setReturns(returns);
    monthlyReturns.setCurrency(currency.name());
    monthlyReturns.setHoldingType(FinancialInstrumentType.GIC);
    return monthlyReturns;
  }

}
