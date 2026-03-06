package com.fintex.ce.application.calculation;

import com.fintex.ce.domain.enumeration.calculation.FixedIncomeSectorType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.result.FixedIncomeSectorResult;
import com.fintex.ce.domain.model.core.Warning;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.domain.constant.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.util.CalculationUtils.sumProduct;
import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.DecimalUtils.divide;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.PortfolioUtils.calculateInitialPortfolioWeight;

public class FixedIncomeBondSectorCalculation {

  private final Map<Holding, Map<FixedIncomeSectorType, BigDecimal>> exposures;
  private final List<Holding> holdings;
  private final List<Warning> warnings;
  private final Map<Holding, BigDecimal> fixedIncomePlusCash;

  public FixedIncomeBondSectorCalculation(final Map<Holding, Map<FixedIncomeSectorType, BigDecimal>> exposures,
      final List<Holding> holdings,
      final List<Warning> warnings,
      final Map<Holding, BigDecimal> fixedIncomePlusCash) {
    this.exposures = exposures;
    this.holdings = holdings;
    this.warnings = warnings;
    this.fixedIncomePlusCash = fixedIncomePlusCash;
  }

  public FixedIncomeSectorResult calculate() {
    final Map<FixedIncomeSectorType, BigDecimal> netProducts = calculateFixedIncomeSectorAllocation(holdings, exposures,
        fixedIncomePlusCash);
    final Map<FixedIncomeSectorType, BigDecimal> reScaledValues = toUserScale(reScaleAbs(netProducts));
    FixedIncomeSectorResult result = new FixedIncomeSectorResult();
    result.setFixedIncomeSector(reScaledValues);
    result.setWarnings(warnings);
    return result;
  }

  private Map<FixedIncomeSectorType, BigDecimal> calculateFixedIncomeSectorAllocation(final List<Holding> holdings,
      final Map<Holding, Map<FixedIncomeSectorType, BigDecimal>> fixedIncome,
      final Map<Holding, BigDecimal> fixedIncomePlusCash) {
    final Map<Holding, BigDecimal> weights = calculateInitialPortfolioWeight(holdings);
    final Map<FixedIncomeSectorType, BigDecimal> result = new HashMap<>();
    for (FixedIncomeSectorType type : FixedIncomeSectorType.values()) {
      final BigDecimal sumProduct = calculateSumProduct(fixedIncome, fixedIncomePlusCash, weights, type);
      result.put(type, divide(sumProduct, HUNDRED));
    }
    return result;
  }

  private BigDecimal calculateSumProduct(
      final Map<Holding, Map<FixedIncomeSectorType, BigDecimal>> fixedIncomeSectorType,
      final Map<Holding, BigDecimal> fixedIncomePlusCash,
      final Map<Holding, BigDecimal> weights,
      final FixedIncomeSectorType type) {
    final Map<Holding, BigDecimal> fixedIncomeType = fixedIncomeSectorType.entrySet().stream()
        .filter(e -> e.getValue().containsKey(type))
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().get(type)));
    return sumProduct(fixedIncomeType, fixedIncomePlusCash, weights);
  }

}
