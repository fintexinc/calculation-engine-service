package com.fintex.ce.application.calculation.service.fee;

import com.fintex.ce.application.calculation.service.DefaultTargetCurrencyConverter;
import com.fintex.ce.application.calculation.service.DefaultTargetCurrencyConverter.Conversion;
import com.fintex.ce.application.calculation.service.DefaultTargetCurrencyConverter.CurrencyValue;
import com.fintex.ce.calculation.SingleAttributeCalculationService;
import com.fintex.ce.model.domain.calculation.fee.AverageManagementExpenseCalculation;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.error.Notification;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fintex.ce.application.constant.HoldingTypeGroup.MER_BEARING_TYPES;
import static com.fintex.ce.application.constant.HoldingTypeGroup.ZERO_MER_TYPES;
import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.application.util.SecurityDataValidator.requireDataForEveryHolding;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY_STRICT;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.WHOLE_PORTFOLIO;
import static com.fintex.ce.model.error.ErrorCode.HOLDING_MISSING_CURRENCY_FROM_FDS;
import static com.fintex.ce.model.error.ErrorCode.HOLDING_TYPE_NOT_LEAF;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;
import static java.math.BigDecimal.ZERO;

/**
 * Shared template for portfolio fee calculations. Owns the pipeline that the three concrete metrics — Average MER,
 * Annual / Monthly Fees, and Average Management Fee — agree on: validate holding types, fetch fee data, resolve
 * per-holding fees, FX-convert market values into the default target currency, and aggregate. Subclasses fill in only
 * the per-metric pieces (which fee fields to read, how to aggregate, and which response shape to populate).
 *
 * @param <R>
 *          per-metric result type returned by {@link #perform(AverageMerCommand, Map)}.
 */
public abstract class AbstractFeeCalculationService<R extends BaseCalculationResult>
    implements
      SingleAttributeCalculationService<AverageMerCommand, FeeData, R> {

  protected final DefaultTargetCurrencyConverter defaultTargetCurrencyConverter;

  protected AbstractFeeCalculationService(DefaultTargetCurrencyConverter defaultTargetCurrencyConverter) {
    this.defaultTargetCurrencyConverter = defaultTargetCurrencyConverter;
  }

  /**
   * Maps FeeData to AverageManagementExpenseCalculation. Subclasses implement this to define which fee fields to
   * extract.
   */
  protected abstract AverageManagementExpenseCalculation mapFeeDataToCalculation(PortfolioHolding holding,
      FeeData fees);

  @Override
  public CompositeSecurityAttribute requiredAttribute() {
    return CompositeSecurityAttribute.FEES;
  }

  protected Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> fetchData(
      AverageMerCommand command, Map<PortfolioHolding, FeeData> data) {
    Map<PortfolioHolding, FeeData> rawData = FilterUtils.restrictToHoldings(data, command.getHoldings());
    return groupAndMap(rawData, command.getHoldings());
  }

  /**
   * Groups raw fee data by holding type and maps to calculation entries.
   *
   * <p>
   * Every MER-bearing holding in the request must have a corresponding row in {@code rawData}. If the data source did
   * not return a row for some fund holding (security unknown, or the configured data provider doesn't cover it), throw
   * {@link com.fintex.ce.model.error.ErrorCode#NO_SECURITY_DATA_FOR_HOLDING} immediately — we won't silently treat an
   * unknown fund as 0% fee, because that would under-report the MER and Fees results without the caller noticing.
   *
   * <p>
   * Zero-MER holdings (stocks, cash, GIC, fixed income) are exempt from this check — the source isn't expected to
   * return fee rows for them, and the resolver treats them as 0% by definition.
   */
  protected Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> groupAndMap(
      Map<PortfolioHolding, FeeData> rawData, List<? extends PortfolioHolding> holdings) {

    requireDataForEveryHolding(rawData, holdings, h -> MER_BEARING_TYPES.contains(h.getHoldingType()));

    Stream<Map.Entry<PortfolioHolding, AverageManagementExpenseCalculation>> fetched = rawData.entrySet().stream()
        .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), mapFeeDataToCalculation(e.getKey(), e.getValue())));

    Stream<Map.Entry<PortfolioHolding, AverageManagementExpenseCalculation>> defaults = holdings.stream()
        .filter(holding -> !rawData.containsKey(holding))
        .map(holding -> new AbstractMap.SimpleEntry<>(holding, createZeroMerCalculation(holding)));

    return Stream.concat(fetched, defaults)
        .collect(Collectors.groupingBy(
            e -> e.getKey().getHoldingType(),
            () -> new EnumMap<>(FinancialInstrumentType.class),
            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  private AverageManagementExpenseCalculation createZeroMerCalculation(PortfolioHolding holding) {
    return AverageManagementExpenseCalculation.builder()
        .marketValue(holding.getValue())
        .holdingType(holding.getHoldingType())
        .modifiedFee(ZERO)
        .build();
  }

  protected abstract List<Notification> resolveFees(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> groupOfMers);

  protected abstract R calculateAverageValue(List<FeeAggregationMode> modes,
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations);

  protected abstract void nullOutEmptyFundModes(R response, AverageMerCommand command);

  @Override
  public R perform(AverageMerCommand command, Map<PortfolioHolding, FeeData> data) {
    validateHoldingTypes(command.getHoldings());
    Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations = fetchData(
        command, data);

    List<Notification> warnings = new ArrayList<>(resolveFees(calculations));
    warnings.addAll(applyValueFxConversion(calculations, command));
    var result = calculateAverageValue(
        getSpecifiedIfEmpty(command.getParameterTypes(), FUNDS_ONLY, WHOLE_PORTFOLIO),
        calculations);

    result.setWarnings(warnings);
    nullOutEmptyFundModes(result, command);
    return result;
  }

  /**
   * Converts each holding's {@code marketValue} into the configured default target currency by delegating to
   * {@link DefaultTargetCurrencyConverter} and applying the fee-specific rule that a non-zero fee with a missing source
   * currency is fatal.
   *
   * <p>
   * Why default to converting <i>all</i> holdings (not just MER-bearing): weighted-average MER/Management-Fee in
   * {@link FeeAggregationMode#WHOLE_PORTFOLIO} mode places non-MER-bearing holdings' {@code marketValue} in the
   * denominator, so leaving them in their native currency distorts the weights. Fees overrides this to convert only the
   * MER-bearing subset because zero-fee holdings contribute {@code 0 × marketValue = 0} to its dollar-sum anyway.
   *
   * <p>
   * Subclasses may override to scope FX tighter (Fees does) or wider.
   */
  protected List<Notification> applyValueFxConversion(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations,
      AverageMerCommand command) {
    return convertMarketValuesToDefaultTargetCurrency(flattenCalcs(calculations));
  }

  /**
   * Shared between the parent's default and {@link FeesCalculationServiceImpl}'s narrower override. Hard-fails when a
   * holding whose fee actually contributes to the numerator has no source currency (we won't guess and silently
   * miscount); applies converted values in place; returns the converter's FX-rate-unavailable warnings.
   */
  protected final List<Notification> convertMarketValuesToDefaultTargetCurrency(
      Map<PortfolioHolding, AverageManagementExpenseCalculation> calcByHolding) {
    Map<PortfolioHolding, CurrencyValue> input = new HashMap<>();
    for (Map.Entry<PortfolioHolding, AverageManagementExpenseCalculation> entry : calcByHolding.entrySet()) {
      AverageManagementExpenseCalculation calc = entry.getValue();
      input.put(entry.getKey(), new CurrencyValue(calc.getCurrency(), calc.getMarketValue()));
    }

    Conversion conversion = defaultTargetCurrencyConverter.convert(input);

    for (PortfolioHolding holding : conversion.missingCurrency()) {
      if (feeContributesToNumerator(calcByHolding.get(holding))) {
        throw HOLDING_MISSING_CURRENCY_FROM_FDS.toExceptionForHolding(holding);
      }
    }
    conversion.converted().forEach((holding, value) -> calcByHolding.get(holding).setMarketValue(value));
    return conversion.warnings();
  }

  protected static Map<PortfolioHolding, AverageManagementExpenseCalculation> flattenCalcs(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations) {
    Map<PortfolioHolding, AverageManagementExpenseCalculation> flat = new HashMap<>();
    calculations.values().forEach(flat::putAll);
    return flat;
  }

  private static boolean feeContributesToNumerator(AverageManagementExpenseCalculation calc) {
    if (calc == null) {
      return false;
    }
    BigDecimal fee = calc.getModifiedFee();
    return fee != null && fee.signum() != 0;
  }

  /**
   * Rejects holdings whose holdingType is null, a parent / non-leaf category (e.g. {@code FUND}, {@code STOCK}), or a
   * leaf type the fee calculator doesn't bucket — i.e. anything not in
   * {@link com.fintex.ce.application.constant.HoldingTypeGroup#MER_BEARING_TYPES} ∪
   * {@link com.fintex.ce.application.constant.HoldingTypeGroup#ZERO_MER_TYPES}. Without this guard, an unrecognised
   * leaf type would fall through the resolver with {@code modifiedFee = null} and be silently dropped from both
   * numerator and denominator in {@link FeeAggregationMode#WHOLE_PORTFOLIO}, producing a wrong result instead of a
   * clear error.
   */
  private void validateHoldingTypes(List<? extends PortfolioHolding> holdings) {
    if (holdings == null) {
      return;
    }
    for (PortfolioHolding holding : holdings) {
      FinancialInstrumentType type = holding.getHoldingType();
      if (type == null || (!MER_BEARING_TYPES.contains(type) && !ZERO_MER_TYPES.contains(type))) {
        throw HOLDING_TYPE_NOT_LEAF.toExceptionForHolding(holding, holding.getIdsString(), type);
      }
    }
  }

  /**
   * If the request contains no MER-bearing holdings, the FUNDS_ONLY and FUNDS_ONLY_STRICT modes have no defined answer;
   * null those entries out so the response signals that explicitly instead of returning 0.
   */
  protected void nullOutEmptyFundModes(Map<FeeAggregationMode, BigDecimal> responseMap, AverageMerCommand command) {
    boolean noFundHolding = command.getHoldings().stream()
        .map(PortfolioHolding::getHoldingType)
        .noneMatch(MER_BEARING_TYPES::contains);
    if (!noFundHolding) {
      return;
    }
    for (FeeAggregationMode mode : List.of(FUNDS_ONLY, FUNDS_ONLY_STRICT)) {
      if (responseMap.containsKey(mode)) {
        responseMap.put(mode, null);
      }
    }
  }

  protected void setFeeValues(AverageManagementExpenseCalculation calc, BigDecimal fee) {
    calc.setModifiedFee(fee);
  }

  /**
   * Average over the MER-bearing subset only; weights are normalised within that subset.
   */
  protected BigDecimal getFundsOnlyAverage(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations) {
    return weightedAverage(merBearingCalculations(calculations));
  }

  /**
   * Average over the whole portfolio; non-MER-bearing holdings contribute 0% but their market value is included in the
   * denominator.
   */
  protected BigDecimal getWholePortfolioAverage(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations) {
    List<AverageManagementExpenseCalculation> all = calculations.values().stream()
        .map(Map::values)
        .flatMap(Collection::stream)
        .toList();
    return weightedAverage(all);
  }

  /**
   * Same set as {@link #getFundsOnlyAverage}, but returns null if any included holding is missing its primary fee
   * datapoint, regardless of whether the secondary datapoint was used as a fallback.
   */
  protected BigDecimal getFundsOnlyStrictAverage(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations) {
    if (anyMissingPrimary(calculations)) {
      return null;
    }
    return weightedAverage(merBearingCalculations(calculations));
  }

  private List<AverageManagementExpenseCalculation> merBearingCalculations(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations) {
    return calculations.entrySet().stream()
        .filter(e -> MER_BEARING_TYPES.contains(e.getKey()))
        .map(e -> e.getValue().values())
        .flatMap(Collection::stream)
        .toList();
  }

  private boolean anyMissingPrimary(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations) {
    return calculations.entrySet().stream()
        .filter(e -> MER_BEARING_TYPES.contains(e.getKey()))
        .flatMap(e -> e.getValue().values().stream())
        .anyMatch(this::primaryFeeMissing);
  }

  private boolean primaryFeeMissing(AverageManagementExpenseCalculation calc) {
    // Primary is the Management Expense Ratio across all fund types (matches the unified resolver in MerFeeResolver).
    // FUNDS_ONLY_STRICT returns null whenever any included holding fell back to NER, GER or Management Fee.
    return Objects.isNull(calc.getManagementExpenseRatio());
  }

  /**
   * Σ (marketValue × modifiedFee) over MER-bearing holdings only. Used by the Fees (annual / monthly) metric.
   */
  protected BigDecimal getFundsOnlySum(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations) {
    return feeWeightedMarketValueSum(merBearingCalculations(calculations));
  }

  /**
   * Σ (marketValue × modifiedFee) over the whole portfolio. Non-fund holdings carry modifiedFee = 0 so they contribute
   * 0 dollars; only their currency / inclusion is implicit.
   */
  protected BigDecimal getWholePortfolioSum(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations) {
    List<AverageManagementExpenseCalculation> all = calculations.values().stream()
        .map(Map::values)
        .flatMap(Collection::stream)
        .toList();
    return feeWeightedMarketValueSum(all);
  }

  /**
   * Same set as {@link #getFundsOnlySum}, but returns null if any included holding is missing its primary fee
   * datapoint, regardless of whether the secondary datapoint was used as a fallback.
   */
  protected BigDecimal getFundsOnlyStrictSum(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations) {
    if (anyMissingPrimary(calculations)) {
      return null;
    }
    return feeWeightedMarketValueSum(merBearingCalculations(calculations));
  }

  private BigDecimal feeWeightedMarketValueSum(List<AverageManagementExpenseCalculation> calcs) {
    return toUserScale(calcs.stream()
        .filter(c -> Objects.nonNull(c.getModifiedFee()) && Objects.nonNull(c.getMarketValue()))
        .map(c -> c.getMarketValue().multiply(c.getModifiedFee()))
        .reduce(ZERO, BigDecimal::add));
  }

  /**
   * Pure weighted average over the supplied calculations. Holdings whose modifiedFee is null are excluded from both
   * numerator and denominator (they were never resolved into a usable fee).
   */
  private BigDecimal weightedAverage(List<AverageManagementExpenseCalculation> calcs) {
    BigDecimal totalMarketValue = calcs.stream()
        .filter(c -> Objects.nonNull(c.getModifiedFee()))
        .map(AverageManagementExpenseCalculation::getMarketValue)
        .reduce(ZERO, BigDecimal::add);
    if (totalMarketValue.signum() == 0) {
      return toUserScale(ZERO);
    }
    BigDecimal weighted = calcs.stream()
        .filter(c -> Objects.nonNull(c.getModifiedFee()))
        .map(c -> c.getModifiedFee().multiply(divide(c.getMarketValue(), totalMarketValue)))
        .reduce(ZERO, BigDecimal::add);
    return toUserScale(weighted);
  }

}
