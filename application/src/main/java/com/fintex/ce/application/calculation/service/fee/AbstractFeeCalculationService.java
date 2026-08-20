package com.fintex.ce.application.calculation.service.fee;

import com.fintex.ce.application.calculation.service.HoldingCurrencyConverter;
import com.fintex.ce.application.calculation.service.HoldingCurrencyConverter.Conversion;
import com.fintex.ce.application.calculation.service.HoldingCurrencyConverter.CurrencyValue;
import com.fintex.ce.calculation.SingleAttributeCalculationService;
import com.fintex.ce.model.domain.calculation.fee.AverageManagementExpenseCalculation;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.currency.Currency;
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
import static com.fintex.ce.model.error.ErrorCode.HOLDING_MISSING_CURRENCY_FROM_MIC;
import static com.fintex.ce.model.error.ErrorCode.HOLDING_TYPE_NOT_LEAF;
import static com.fintex.ce.model.error.ErrorCode.PORTFOLIO_MISSING_CURRENCY;
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

  /**
   * The aggregation views whose holding set is the MER-bearing subset, and which therefore have no defined answer for a
   * portfolio holding no fund.
   */
  protected static final List<FeeAggregationMode> FUND_ONLY_MODES = List.of(FUNDS_ONLY, FUNDS_ONLY_STRICT);

  protected final HoldingCurrencyConverter currencyConverter;

  protected AbstractFeeCalculationService(HoldingCurrencyConverter currencyConverter) {
    this.currencyConverter = currencyConverter;
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

  /**
   * @param command
   *          the originating request. Passed alongside the already-defaulted {@code modes} so a subclass whose output
   *          depends on request settings beyond the aggregation modes — the projection horizons of {@code fees} — can
   *          read them without re-deriving the mode defaulting.
   */
  protected abstract R calculateAverageValue(AverageMerCommand command, List<FeeAggregationMode> modes,
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations);

  protected abstract void nullOutEmptyFundModes(R response, AverageMerCommand command);

  @Override
  public R perform(AverageMerCommand command, Map<PortfolioHolding, FeeData> data) {
    validateHoldingTypes(command.getHoldings());
    Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations = fetchData(
        command, data);

    List<Notification> warnings = new ArrayList<>(resolveFees(calculations));
    // Raised here rather than inside HoldingCurrencyConverter: the fee commands are the only ones with an
    // optional target currency, so they are the only requests that can omit one. The returns commands require it
    // (PortfolioBenchmarkCommand#currency is @NotNull) and the allocation commands never offered the field, so
    // warning from the converter would fire on requests that had nothing to leave out.
    Currency reportingCurrency = currencyConverter.resolveTargetCurrency(command.getTargetCurrency());
    if (command.getTargetCurrency() == null) {
      warnings.add(PORTFOLIO_MISSING_CURRENCY.asNotification(reportingCurrency));
    }
    warnings.addAll(applyValueFxConversion(calculations, command.getTargetCurrency()));
    var result = calculateAverageValue(command,
        getSpecifiedIfEmpty(command.getParameterTypes(), FUNDS_ONLY, WHOLE_PORTFOLIO),
        calculations);

    result.setWarnings(warnings);
    nullOutEmptyFundModes(result, command);
    return result;
  }

  /**
   * Converts each holding's {@code marketValue} into {@code targetCurrency} by delegating to
   * {@link HoldingCurrencyConverter} and applying the fee-specific rule that a non-zero fee with a missing source
   * currency is fatal. A null target means the caller did not ask for one, and the converter falls back to the
   * configured reporting currency.
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
      Currency targetCurrency) {
    return convertMarketValuesToTargetCurrency(flattenCalcs(calculations), targetCurrency);
  }

  /**
   * Shared between the parent's default and {@link FeesCalculationServiceImpl}'s narrower override. Hard-fails when a
   * holding whose fee actually contributes to the numerator has no source currency (we won't guess and silently
   * miscount); applies converted values in place; returns the converter's FX-rate-unavailable warnings.
   */
  protected final List<Notification> convertMarketValuesToTargetCurrency(
      Map<PortfolioHolding, AverageManagementExpenseCalculation> calcByHolding, Currency targetCurrency) {
    Map<PortfolioHolding, CurrencyValue> input = new HashMap<>();
    for (Map.Entry<PortfolioHolding, AverageManagementExpenseCalculation> entry : calcByHolding.entrySet()) {
      AverageManagementExpenseCalculation calc = entry.getValue();
      input.put(entry.getKey(), new CurrencyValue(calc.getCurrency(), calc.getMarketValue()));
    }

    Conversion conversion = currencyConverter.convert(input, targetCurrency);

    for (PortfolioHolding holding : conversion.missingCurrency()) {
      if (feeContributesToNumerator(calcByHolding.get(holding))) {
        throw HOLDING_MISSING_CURRENCY_FROM_MIC.toExceptionForHolding(holding);
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
   * Rejects holdings whose holdingType is null, a parent / non-leaf category (e.g. {@code FUND}), or a leaf type the
   * fee calculator doesn't bucket — i.e. anything not in
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
        throw HOLDING_TYPE_NOT_LEAF.toExceptionForHolding(holding, type);
      }
    }
  }

  /**
   * If the request contains no MER-bearing holdings, the FUNDS_ONLY and FUNDS_ONLY_STRICT modes have no defined answer;
   * null those entries out so the response signals that explicitly instead of returning 0.
   */
  protected void nullOutEmptyFundModes(Map<FeeAggregationMode, BigDecimal> responseMap, AverageMerCommand command) {
    if (!hasNoFundHolding(command)) {
      return;
    }
    for (FeeAggregationMode mode : FUND_ONLY_MODES) {
      if (responseMap.containsKey(mode)) {
        responseMap.put(mode, null);
      }
    }
  }

  /**
   * True when the request contains no MER-bearing holding. Exposed to subclasses so every response map they own —
   * ratios, dollar sums, projections — is nulled on the same condition instead of each re-deriving it.
   */
  protected static boolean hasNoFundHolding(AverageMerCommand command) {
    return command.getHoldings().stream()
        .map(PortfolioHolding::getHoldingType)
        .noneMatch(MER_BEARING_TYPES::contains);
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
    return weightedAverage(allCalculations(calculations));
  }

  /**
   * FX-converted market-value denominator behind {@link #getFundsOnlyAverage} — the exact asset base its weighted
   * average normalises over. Consumed by {@code mer-benchmark-comparison} to scale a ratio difference into an annual
   * dollar impact for the funds-only view.
   */
  protected BigDecimal getFundsOnlyBase(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations) {
    return includedMarketValue(merBearingCalculations(calculations));
  }

  /**
   * FX-converted market-value denominator behind {@link #getWholePortfolioAverage} — the whole portfolio's converted
   * value. Consumed by {@code mer-benchmark-comparison} for the whole-portfolio view's annual dollar impact.
   */
  protected BigDecimal getWholePortfolioBase(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations) {
    return includedMarketValue(allCalculations(calculations));
  }

  /**
   * Base behind {@link #getFundsOnlyStrictAverage}: same set as {@link #getFundsOnlyBase}, but null when the strict
   * average is null (any included holding missing its primary datapoint), so the base always tracks its ratio.
   */
  protected BigDecimal getFundsOnlyStrictBase(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations) {
    return anyMissingPrimary(calculations) ? null : includedMarketValue(merBearingCalculations(calculations));
  }

  /**
   * Same set as {@link #getFundsOnlyAverage}, but returns null if any included holding is missing its primary fee
   * datapoint, regardless of whether the secondary datapoint was used as a fallback.
   */
  protected BigDecimal getFundsOnlyStrictAverage(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations) {
    return anyMissingPrimary(calculations) ? null : weightedAverage(merBearingCalculations(calculations));
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
    return feeWeightedMarketValueSum(allCalculations(calculations));
  }

  private static List<AverageManagementExpenseCalculation> allCalculations(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations) {
    return calculations.values().stream()
        .map(Map::values)
        .flatMap(Collection::stream)
        .toList();
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
    BigDecimal totalMarketValue = includedMarketValue(calcs);
    if (totalMarketValue.signum() == 0) {
      return toUserScale(ZERO);
    }
    BigDecimal weighted = calcs.stream()
        .filter(c -> Objects.nonNull(c.getModifiedFee()))
        .map(c -> c.getModifiedFee().multiply(divide(c.getMarketValue(), totalMarketValue)))
        .reduce(ZERO, BigDecimal::add);
    return toUserScale(weighted);
  }

  /**
   * Sum of market values over the holdings that actually contribute to a weighted average — i.e. those with a resolved
   * {@code modifiedFee}. This is exactly the denominator {@link #weightedAverage} normalises over, so a mode's base and
   * its ratio always cover the same holding set. Values are already FX-converted to the default target currency by
   * {@link #convertMarketValuesToTargetCurrency} before this runs.
   */
  private BigDecimal includedMarketValue(List<AverageManagementExpenseCalculation> calcs) {
    return calcs.stream()
        .filter(c -> Objects.nonNull(c.getModifiedFee()))
        .map(AverageManagementExpenseCalculation::getMarketValue)
        .reduce(ZERO, BigDecimal::add);
  }

}
