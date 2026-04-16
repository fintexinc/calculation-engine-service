package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.ce.model.domain.result.allocation.FixedIncomeSectorResult;
import com.fintex.ce.model.error.Warning;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSecuritiesAllocationType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.util.CalculationUtils.sumProduct;
import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.DecimalUtils.divide;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.PortfolioUtils.calculateInitialPortfolioWeight;

public class FixedIncomeBondSectorCalculation {

  private final Map<Holding, Map<FixedIncomeSecuritiesAllocationType, BigDecimal>> exposures;
  private final List<Holding> holdings;
  private final List<Warning> warnings;
  private final Map<Holding, BigDecimal> fixedIncomePlusCash;

  public FixedIncomeBondSectorCalculation(
      final Map<Holding, Map<FixedIncomeSecuritiesAllocationType, BigDecimal>> exposures,
      final List<Holding> holdings,
      final List<Warning> warnings,
      final Map<Holding, BigDecimal> fixedIncomePlusCash) {
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
    FixedIncomeSectorResult result = new FixedIncomeSectorResult();
    result.setFixedIncomeSector(reScaledValues);
    result.setWarnings(warnings);
    return result;
  }

  private Map<FixedIncomeSecuritiesAllocationType, BigDecimal> calculateFixedIncomeSectorAllocation(
      final List<Holding> holdings,
      final Map<Holding, Map<FixedIncomeSecuritiesAllocationType, BigDecimal>> fixedIncome,
      final Map<Holding, BigDecimal> fixedIncomePlusCash) {
    final Map<Holding, BigDecimal> weights = calculateInitialPortfolioWeight(holdings);
    final Map<FixedIncomeSecuritiesAllocationType, BigDecimal> result = new HashMap<>();
    for (FixedIncomeSecuritiesAllocationType type : FixedIncomeSecuritiesAllocationType.values()) {
      final BigDecimal sumProduct = calculateSumProduct(fixedIncome, fixedIncomePlusCash, weights, type);
      result.put(type, divide(sumProduct, HUNDRED));
    }
    return result;
  }

  private BigDecimal calculateSumProduct(
      final Map<Holding, Map<FixedIncomeSecuritiesAllocationType, BigDecimal>> fixedIncomeSectorType,
      final Map<Holding, BigDecimal> fixedIncomePlusCash,
      final Map<Holding, BigDecimal> weights,
      final FixedIncomeSecuritiesAllocationType type) {
    final Map<Holding, BigDecimal> fixedIncomeType = fixedIncomeSectorType.entrySet().stream()
        .filter(e -> e.getValue().containsKey(type))
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().get(type)));
    return sumProduct(fixedIncomeType, fixedIncomePlusCash, weights);
  }

}
