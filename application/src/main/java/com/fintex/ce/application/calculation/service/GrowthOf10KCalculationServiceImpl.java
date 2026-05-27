package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpsdAndCpedAbstractService;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.KeyValueResult;
import com.fintex.ce.model.domain.result.returns.Growth10KResult;
import com.fintex.ce.model.dto.command.ReturnCommand;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.util.BigDecimalConstants.TEN_THOUSAND;
import static com.fintex.ce.util.DateTimeUtils.minusOneMonth;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;

@Service
public class GrowthOf10KCalculationServiceImpl
    extends
      WeightedAverageWithCpsdAndCpedAbstractService<ReturnCommand, Growth10KResult> {

  public GrowthOf10KCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpsdAndCpedPipeline portfolioWeightedAverageWithCpsdAndCped) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCpsdAndCped);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.GROWTH_OF_10K;
  }

  @Override
  public Growth10KResult perform(ReturnCommand command) {
    // Weighted-average pipeline applies SCALE_OF_TWO upstream, so the returned series is already in factor form
    // (e.g. 1.05 for a 5% month). compoundGrowth10K therefore uses AS_IS to avoid re-scaling.
    WeightedAverageResult<HoldingMonthlyReturns> weighted = runWeightedAverage(command, ReturnFactorScale.SCALE_OF_TWO);

    TreeMap<LocalDate, BigDecimal> growth = compoundGrowth10K(weighted.weightedAverage(), ReturnFactorScale.AS_IS);
    List<KeyValueResult> points = growth.entrySet().stream()
        .map(e -> (KeyValueResult) new KeyValueResult<>(e.getKey(), e.getValue()))
        .toList();

    return Growth10KResult.builder()
        .growth10k(points)
        .performanceStartDate(performanceStart(growth))
        .performanceEndDate(performanceEnd(growth))
        .warnings(weighted.getErrorsAsWarnings())
        .build();
  }

  /**
   * Compounds a monthly return series into a Growth-of-$10K curve. The seed sits at (first-return-month − 1 month) with
   * $10,000 and each subsequent month is {@code previous × factor(entry)}, where the factor is produced by the
   * caller-supplied {@link ReturnFactorScale}. Empty when the input series is null/empty.
   *
   * <p>
   * Exposed as a static helper because the same curve is consumed by sibling metrics (Max-Drawdown, Mar-Ratio): they
   * pass an already-factor-form weighted-average series with {@link ReturnFactorScale#AS_IS} to avoid re-scaling. The
   * {@code growth-of-10k} endpoint is the canonical owner of this artifact, so the math lives here.
   * </p>
   */
  public static TreeMap<LocalDate, BigDecimal> compoundGrowth10K(NavigableMap<LocalDate, BigDecimal> returns,
      ReturnFactorScale scale) {
    TreeMap<LocalDate, BigDecimal> growth = new TreeMap<>();
    if (CollectionUtils.isEmpty(returns)) {
      return growth;
    }
    LocalDate seedMonth = toLastDayOfMonth(minusOneMonth(returns.firstKey()));
    growth.put(seedMonth, TEN_THOUSAND);
    BigDecimal previous = TEN_THOUSAND;
    for (Map.Entry<LocalDate, BigDecimal> entry : returns.entrySet()) {
      BigDecimal factor = scale.getFormula().apply(entry);
      BigDecimal next = toUserScale(previous.multiply(factor));
      growth.put(entry.getKey(), next);
      previous = next;
    }
    return growth;
  }

  private static LocalDate performanceStart(Map<LocalDate, BigDecimal> growth) {
    return growth.isEmpty() ? null : growth.keySet().iterator().next();
  }

  private static LocalDate performanceEnd(TreeMap<LocalDate, BigDecimal> growth) {
    return growth.isEmpty() ? null : growth.lastKey();
  }
}
