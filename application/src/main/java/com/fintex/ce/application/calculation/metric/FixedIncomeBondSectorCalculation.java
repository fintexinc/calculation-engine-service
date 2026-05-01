package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.FixedIncomeSectorResult;
import com.fintex.ce.model.error.Warning;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSecuritiesAllocationType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.application.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.application.util.CalculationUtils.sumProduct;
import static com.fintex.ce.application.util.CollectorUtils.toMap;
import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.application.util.PortfolioUtils.calculateInitialPortfolioWeight;
import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;

public class FixedIncomeBondSectorCalculation {

  private final Map<PortfolioHolding, Map<FixedIncomeSecuritiesAllocationType, BigDecimal>> exposures;
  private final List<PortfolioHolding> holdings;
  private final List<Warning> warnings;
  private final Map<PortfolioHolding, BigDecimal> fixedIncomePlusCash;

  public FixedIncomeBondSectorCalculation(
      final Map<PortfolioHolding, Map<FixedIncomeSecuritiesAllocationType, BigDecimal>> exposures,
      final List<PortfolioHolding> holdings,
      final List<Warning> warnings,
      final Map<PortfolioHolding, BigDecimal> fixedIncomePlusCash) {
    this.exposures = exposures;
    this.holdings = holdings;
    this.warnings = warnings;
    this.fixedIncomePlusCash = fixedIncomePlusCash;
  }

  public FixedIncomeSectorResult calculate() {
    final Map<FixedIncomeSecuritiesAllocationType, BigDecimal> netProducts = calculateFixedIncomeSectorAllocation(
        holdings, exposures,
        fixedIncomePlusCash);
    final Map<FixedIncomeSecuritiesAllocationType, BigDecimal> reScaledValues = toUserScale(reScaleAbs(netProducts));
    return FixedIncomeSectorResult.builder()
        .fixedIncomeSector(reScaledValues)
        .warnings(warnings)
        .build();
  }

  private Map<FixedIncomeSecuritiesAllocationType, BigDecimal> calculateFixedIncomeSectorAllocation(
      final List<PortfolioHolding> holdings,
      final Map<PortfolioHolding, Map<FixedIncomeSecuritiesAllocationType, BigDecimal>> fixedIncome,
      final Map<PortfolioHolding, BigDecimal> fixedIncomePlusCash) {
    final Map<PortfolioHolding, BigDecimal> weights = calculateInitialPortfolioWeight(holdings);
    final Map<FixedIncomeSecuritiesAllocationType, BigDecimal> result = new HashMap<>();
    for (FixedIncomeSecuritiesAllocationType type : FixedIncomeSecuritiesAllocationType.values()) {
      final BigDecimal sumProduct = calculateSumProduct(fixedIncome, fixedIncomePlusCash, weights, type);
      result.put(type, divide(sumProduct, HUNDRED));
    }
    return result;
  }

  private BigDecimal calculateSumProduct(
      final Map<PortfolioHolding, Map<FixedIncomeSecuritiesAllocationType, BigDecimal>> fixedIncomeSectorType,
      final Map<PortfolioHolding, BigDecimal> fixedIncomePlusCash,
      final Map<PortfolioHolding, BigDecimal> weights,
      final FixedIncomeSecuritiesAllocationType type) {
    final Map<PortfolioHolding, BigDecimal> fixedIncomeType = fixedIncomeSectorType.entrySet().stream()
        .filter(e -> e.getValue().containsKey(type))
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().get(type)));
    return sumProduct(fixedIncomeType, fixedIncomePlusCash, weights);
  }

}
