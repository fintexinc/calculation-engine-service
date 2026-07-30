package com.fintex.ce.application.calculation.service.fee;

import com.fintex.ce.application.config.FeeProjectionProperties;
import com.fintex.ce.application.util.FeeProjectionUtils;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.calculation.SingleAttributeCalculationService;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.calculation.fee.MerComparisonData;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.AverageMerResult;
import com.fintex.ce.model.domain.result.fee.FeeComparison;
import com.fintex.ce.model.domain.result.fee.FeeRateComparison;
import com.fintex.ce.model.domain.result.fee.FeeSpendComparison;
import com.fintex.ce.model.domain.result.fee.MerComparisonResult;
import com.fintex.ce.model.domain.security.SecurityData;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.model.dto.command.MerComparisonCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;

import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY;
import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;
import static java.math.BigDecimal.ZERO;

/**
 * {@code mer-benchmark-comparison} metric (TMI-543 / TMI-545): compares the portfolio's weighted-average fee rate to
 * the benchmark's, once per requested aggregation view, and projects what each side costs over the configured horizons
 * so the saving from switching can be read straight off the response. Both rates are produced by reusing the existing
 * MER pipeline (the {@code mer} calculation service — once for the portfolio, once for the benchmark holdings as a
 * portfolio of their own), so fee resolution, FX handling, and warnings stay consistent with the standalone {@code mer}
 * metric, and the dollar figures reuse the asset base that pipeline already computed rather than re-summing holding
 * values.
 */
@Service
@RequiredArgsConstructor
public class MerBenchmarkComparisonService
    implements
      CalculationService<MerComparisonCommand, MerComparisonData, MerComparisonResult> {

  /** The three ways a view can fail to be comparable, as they read in {@code FEE_COMPARISON_NOT_AVAILABLE}. */
  static final String NO_PORTFOLIO_RATE = "the portfolio has no fee rate for this view";
  static final String NO_BENCHMARK_RATE = "the benchmark has no fee rate";
  static final String NO_ASSET_BASE = "the view has no asset base to charge the rates against";

  private final SingleAttributeCalculationService<AverageMerCommand, FeeData, AverageMerResult> merCalculationService;
  private final FeeProjectionProperties projectionProperties;

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

    Set<TimePeriod> periods = projectionProperties.periodsFor(command.getProjectionPeriods());
    Map<FeeAggregationMode, FeeComparison> comparison = new EnumMap<>(FeeAggregationMode.class);
    portfolioResult.getManagementExpenseRatio()
        .forEach((mode, portfolioMer) -> comparison.put(mode,
            compare(mode, portfolioMer, benchmarkMer, portfolioResult.getBaseValue().get(mode), periods)));

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
   *          the FX-converted market-value denominator behind {@code portfolioRate} for this aggregation view (see
   *          {@link AverageMerResult#getBaseValue()}). Using the mode's own base — funds-only value for
   *          {@code FUNDS_ONLY}, whole-portfolio value for {@code WHOLE_PORTFOLIO} — keeps every dollar figure
   *          consistent with the ratio's asset base and in a single currency.
   */
  private FeeComparison compare(FeeAggregationMode mode, BigDecimal portfolioRate, BigDecimal benchmarkRate,
      BigDecimal baseValue, Set<TimePeriod> periods) {
    require(portfolioRate != null, mode, NO_PORTFOLIO_RATE);
    require(benchmarkRate != null, mode, NO_BENCHMARK_RATE);
    require(baseValue != null, mode, NO_ASSET_BASE);

    return FeeComparison.builder()
        .feeRate(new FeeRateComparison(
            portfolioRate,
            benchmarkRate,
            percentDifference(portfolioRate, benchmarkRate),
            portfolioRate.compareTo(benchmarkRate) == 0))
        .spend(project(portfolioRate, benchmarkRate, baseValue, periods))
        .build();
  }

  /**
   * A view that cannot be compared is an error rather than a comparison full of nulls. The caller asked what switching
   * would save; "the portfolio holds no funds in this view" is an answer they have to act on, not a number they can
   * read, and a null-filled body leaves them to work out which of the three reasons applied.
   */
  private static void require(boolean comparable, FeeAggregationMode mode, String reason) {
    if (!comparable) {
      throw ErrorCode.FEE_COMPARISON_NOT_AVAILABLE.toException(mode, reason);
    }
  }

  /** Undefined when the benchmark charges nothing, since the portfolio's rate would then be infinitely larger. */
  private static BigDecimal percentDifference(BigDecimal portfolioRate, BigDecimal benchmarkRate) {
    if (benchmarkRate.signum() == 0) {
      return null;
    }
    return toUserScale(divide(portfolioRate.subtract(benchmarkRate), benchmarkRate).multiply(HUNDRED));
  }

  /**
   * Turns the two rates into what each side costs over every configured horizon. Both are charged against the same
   * {@code baseValue}, so the saving is a difference between two amounts drawn from one pool rather than a comparison
   * of differently-sized portfolios — the whole-portfolio view answers "what if all of this moved into the benchmark
   * fund", the funds-only view answers the same question about the fund portion alone.
   */
  private Map<TimePeriod, FeeSpendComparison> project(BigDecimal portfolioRate, BigDecimal benchmarkRate,
      BigDecimal baseValue, Set<TimePeriod> periods) {
    BigDecimal portfolioAnnual = portfolioRate.multiply(baseValue);
    BigDecimal benchmarkAnnual = benchmarkRate.multiply(baseValue);
    BigDecimal annualSaving = portfolioAnnual.subtract(benchmarkAnnual);
    BigDecimal growthRate = projectionProperties.getAnnualGrowthRate();

    Map<TimePeriod, FeeSpendComparison> byPeriod = new LinkedHashMap<>();
    for (TimePeriod period : periods) {
      byPeriod.put(period, new FeeSpendComparison(
          FeeProjectionUtils.spend(portfolioAnnual, growthRate, period),
          FeeProjectionUtils.spend(benchmarkAnnual, growthRate, period),
          // Projected from the annual difference rather than subtracting the two rounded spends: the latter rounds
          // twice and leaves the saving off the exact figure by up to a unit in the last reported place, so a portfolio
          // charging exactly double the benchmark reported a saving a hair away from the benchmark's own spend.
          FeeProjectionUtils.spend(annualSaving, growthRate, period)));
    }
    return byPeriod;
  }
}
