package ca.tangerine.pce.application.returns;

import ca.tangerine.pce.model.domain.calculation.returns.HoldingMonthlyReturns;
import ca.tangerine.pce.model.domain.holding.MonthlyReturnGeneratableHolding;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static ca.tangerine.pce.application.util.DecimalUtils.divide;
import static ca.tangerine.pce.application.util.DecimalUtils.pow;
import static ca.tangerine.pce.util.DateTimeUtils.rangeWithLastDayOfMonth;
import static ca.tangerine.pce.util.FilterUtils.GIC_PREDICATE;
import static ca.tangerine.pce.util.FilterUtils.filterHoldings;

@Component
public class MonthlyReturnsGenerator {

  public Map<PortfolioHolding, HoldingMonthlyReturns> generateGicMonthlyReturns(List<PortfolioHolding> holdings) {
    Map<PortfolioHolding, HoldingMonthlyReturns> result = new HashMap<>();
    List<PortfolioHolding> filteredHoldings = filterHoldings(holdings, GIC_PREDICATE);
    for (PortfolioHolding h : filteredHoldings) {
      result.put(h, generatedMonthlyReturns((MonthlyReturnGeneratableHolding) h));
    }
    return result;
  }

  private HoldingMonthlyReturns generatedMonthlyReturns(MonthlyReturnGeneratableHolding holding) {
    TreeMap<LocalDate, BigDecimal> returns = generateReturns(holding);
    return createMonthlyReturns(returns, holding.getCurrency());
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
    // Normalize to percent form so the output is unit-compatible with MIC-supplied monthly returns. Every
    // downstream consumer in this pipeline (FxConversionProcessor's per-month formula, ReturnFactorScale.SCALE_OF_TWO,
    // weighted-average compounding) assumes percent-form input — emitting decimal-form here under-weights GIC
    // contributions by ~100× in compounded results.
    return pow(yearlyReturn, oneDividedByTwelve).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));
  }

  private BigDecimal convert(BigDecimal interestRate) {
    return divide(interestRate, BigDecimal.valueOf(100)).add(BigDecimal.ONE);
  }

  private BigDecimal getCompoundingFrequency(MonthlyReturnGeneratableHolding gicHolding) {
    return gicHolding.getInterestFreq().getFrequency();
  }

  private HoldingMonthlyReturns createMonthlyReturns(TreeMap<LocalDate, BigDecimal> returns, Currency currency) {
    HoldingMonthlyReturns monthlyReturns = new HoldingMonthlyReturns();
    monthlyReturns.setReturns(returns);
    monthlyReturns.setCurrency(currency.name());
    monthlyReturns.setHoldingType(FinancialInstrumentType.GIC);
    return monthlyReturns;
  }

}
