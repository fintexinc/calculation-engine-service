package com.fintex.ce.application.calculation.service.fee;

import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.calculation.SingleAttributeCalculationService;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.calculation.fee.MerComparisonData;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.AverageMerResult;
import com.fintex.ce.model.domain.result.fee.MerComparison;
import com.fintex.ce.model.domain.result.fee.MerComparisonResult;
import com.fintex.ce.model.domain.security.SecurityData;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.model.dto.command.MerComparisonCommand;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;

import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY;
import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;
import static java.math.BigDecimal.ZERO;

/**
 * {@code mer-benchmark-comparison} metric (TMI-543): compares the portfolio's weighted-average MER to the benchmark's,
 * once per requested aggregation view. Both MER numbers are produced by reusing the existing MER pipeline (the
 * {@code mer} calculation service — once for the portfolio, once for the benchmark holdings as a portfolio of their
 * own), so fee resolution, FX handling, and warnings stay consistent with the standalone {@code mer} metric.
 */
@Service
@RequiredArgsConstructor
public class MerBenchmarkComparisonService
    implements
      CalculationService<MerComparisonCommand, MerComparisonData, MerComparisonResult> {

  private final SingleAttributeCalculationService<AverageMerCommand, FeeData, AverageMerResult> merCalculationService;

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.MER_BENCHMARK_COMPARISON;
  }

  @Override
  public List<CompositeSecurityAttribute> requiredAttributes() {
    return List.of(CompositeSecurityAttribute.FEES);
  }

  @Override
  public MerComparisonData prepareData(SecurityData securityData) {
    return MerComparisonData.from(securityData);
  }

  @Override
  public MerComparisonResult perform(MerComparisonCommand command, MerComparisonData data) {
    AverageMerCommand portfolioCommand = AverageMerCommand.of(command, command.getHoldings(),
        command.getParameterTypes());
    AverageMerResult portfolioResult = merCalculationService.perform(portfolioCommand, data.portfolio());
    AverageMerResult benchmarkResult = benchmarkMer(command, data.benchmark());

    BigDecimal benchmarkMer = benchmarkResult.getManagementExpenseRatio().get(FUNDS_ONLY);

    Map<FeeAggregationMode, MerComparison> comparison = new EnumMap<>(FeeAggregationMode.class);
    portfolioResult.getManagementExpenseRatio()
        .forEach((mode, portfolioMer) -> comparison.put(mode,
            compare(portfolioMer, benchmarkMer, portfolioResult.getBaseValue().get(mode))));

    List<Notification> warnings = new ArrayList<>(portfolioResult.getWarnings());
    warnings.addAll(benchmarkResult.getWarnings());

    MerComparisonResult result = new MerComparisonResult();
    result.setComparison(comparison);
    result.setWarnings(warnings);
    return result;
  }

  /**
   * The benchmark's MER — the funds-only weighted average over the benchmark holdings, computed by running the MER
   * pipeline over them as a portfolio of their own. Holding values are the weights, so a benchmark portfolio is
   * averaged exactly the way the client's portfolio is; the single-fund benchmark is just the one-element case, where
   * the weight cancels out and the result is that fund's own MER.
   */
  private AverageMerResult benchmarkMer(MerComparisonCommand command, Map<PortfolioHolding, FeeData> benchmarkFees) {
    List<PortfolioHolding> weighted = applyWeights(command.getBenchmarkHoldings());
    AverageMerCommand benchmarkCommand = AverageMerCommand.of(command, weighted, List.of(FUNDS_ONLY));

    return merCalculationService.perform(benchmarkCommand,
        rekeyFees(command.getBenchmarkHoldings(), weighted, benchmarkFees));
  }

  /**
   * A benchmark is a set of MERs to average, and its holdings' values are the weights. Callers who only care about the
   * ratios can leave the values out: when no benchmark holding carries a usable value they are weighted equally, which
   * for the single-fund benchmark is simply that fund's MER. Values are otherwise taken as sent — a holding left at
   * zero drops out of both sides of the average, as it does for the portfolio.
   */
  private static List<PortfolioHolding> applyWeights(List<PortfolioHolding> benchmarkHoldings) {
    if (benchmarkHoldings.stream().noneMatch(MerBenchmarkComparisonService::hasUsableValue)) {
      return benchmarkHoldings.stream().map(holding -> withValue(holding, BigDecimal.ONE)).toList();
    }
    return benchmarkHoldings.stream()
        .map(holding -> holding.getValue() == null ? withValue(holding, ZERO) : holding)
        .toList();
  }

  private static PortfolioHolding withValue(PortfolioHolding holding, BigDecimal value) {
    return holding.toBuilder().value(value).build();
  }

  /**
   * Re-keys the fetched fee data onto the weight-adjusted holdings, which are different map keys whenever
   * {@link #applyWeights} had to rewrite a value.
   */
  private static Map<PortfolioHolding, FeeData> rekeyFees(List<PortfolioHolding> original,
      List<PortfolioHolding> weighted, Map<PortfolioHolding, FeeData> benchmarkFees) {
    return IntStream.range(0, weighted.size())
        .boxed()
        .filter(i -> benchmarkFees.get(original.get(i)) != null)
        .collect(Collectors.toMap(weighted::get, i -> benchmarkFees.get(original.get(i)), (first, second) -> first));
  }

  private static boolean hasUsableValue(PortfolioHolding holding) {
    return holding.getValue() != null && holding.getValue().signum() != 0;
  }

  /**
   * @param baseValue
   *          the FX-converted market-value denominator behind {@code portfolioMer} for this aggregation view (see
   *          {@link AverageMerResult#getBaseValue()}). Using the mode's own base — funds-only value for
   *          {@code FUNDS_ONLY}, whole-portfolio value for {@code WHOLE_PORTFOLIO} — keeps the dollar impact consistent
   *          with the ratio's asset base and in a single currency.
   */
  private MerComparison compare(BigDecimal portfolioMer, BigDecimal benchmarkMer, BigDecimal baseValue) {
    MerComparison.MerComparisonBuilder builder = MerComparison.builder()
        .portfolioMer(portfolioMer)
        .benchmarkMer(benchmarkMer);
    if (portfolioMer == null || benchmarkMer == null) {
      return builder.equal(false).build();
    }
    builder.equal(portfolioMer.compareTo(benchmarkMer) == 0);
    if (benchmarkMer.signum() != 0) {
      builder.percentDifference(
          toUserScale(divide(portfolioMer.subtract(benchmarkMer), benchmarkMer).multiply(HUNDRED)));
    }
    if (baseValue != null) {
      builder.annualDollarImpact(toUserScale(benchmarkMer.subtract(portfolioMer).multiply(baseValue)));
    }
    return builder.build();
  }
}
