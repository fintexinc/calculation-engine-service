package ca.tangerine.pce.application.calculation.service.fee;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ca.tangerine.pce.application.constant.HoldingTypeGroup.MER_BEARING_TYPES;
import static ca.tangerine.pce.application.util.DecimalUtils.divide;
import static ca.tangerine.pce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY;
import static ca.tangerine.pce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY_STRICT;
import static ca.tangerine.pce.model.domain.enumeration.FeeAggregationMode.WHOLE_PORTFOLIO;
import static ca.tangerine.pce.model.util.BigDecimalConstants.TWELVE;

import ca.tangerine.pce.application.calculation.service.HoldingCurrencyConverter;
import ca.tangerine.pce.application.config.FeeProjectionProperties;
import ca.tangerine.pce.application.util.FeeProjectionUtils;
import ca.tangerine.pce.model.domain.calculation.fee.AverageManagementExpenseCalculation;
import ca.tangerine.pce.model.domain.calculation.fee.FeeData;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.enumeration.FeeAggregationMode;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.fee.FeesResult;
import ca.tangerine.pce.model.dto.command.AverageMerCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;
import ca.tangerine.wm.commons.error.Notification;

/**
 * Annual / Monthly / multi-year projected fee dollar amounts in the configured default target currency (default CAD).
 * Annual = Σ (marketValue × resolved fee) per aggregation mode, with the fee resolved through the injected
 * {@link FeeResolver}; Monthly = Annual ÷ 12; the projections extend Annual over the horizons configured in
 * {@link FeeProjectionProperties} (see {@link FeeProjectionUtils} for the formula).
 *
 * <p>
 * FX: each MER-bearing holding's {@code marketValue} is converted to the default target currency via
 * {@link HoldingCurrencyConverter} before the sum. Missing currency on a MER-bearing holding → hard error
 * ({@link ErrorCode#HOLDING_MISSING_CURRENCY_FROM_MIC}). Rate unavailable → emit {@link ErrorCode#FX_RATES_UNAVAILABLE}
 * and leave the value in its native currency. Zero-fee holdings (stocks, cash, GIC, fixed income) are skipped — they
 * contribute 0 either way.
 */
@Service
public class FeesCalculationServiceImpl extends AbstractFeeCalculationService<FeesResult> {

  private final FeeResolver feeResolver;
  private final FeeProjectionProperties projectionProperties;

  public FeesCalculationServiceImpl(HoldingCurrencyConverter currencyConverter,
      FeeResolver feeResolver, FeeProjectionProperties projectionProperties) {
    super(currencyConverter);
    this.feeResolver = feeResolver;
    this.projectionProperties = projectionProperties;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.FEES;
  }

  @Override
  protected AverageManagementExpenseCalculation mapFeeDataToCalculation(PortfolioHolding holding, FeeData fees) {
    return feeResolver.mapFeeDataToCalculation(holding, fees);
  }

  @Override
  protected List<Notification> resolveFees(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> groupOfMers) {
    return feeResolver.resolveFees(groupOfMers);
  }

  /**
   * Narrower than the parent default: only MER-bearing holdings are FX-converted because zero-fee holdings (stocks,
   * cash, GIC, fixed income) contribute {@code 0 × marketValue = 0} to every Fees sum — converting them would be wasted
   * work and would force an FX rate fetch for currencies that don't affect the result.
   */
  @Override
  protected List<Notification> applyValueFxConversion(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations,
      Currency targetCurrency) {
    Map<PortfolioHolding, AverageManagementExpenseCalculation> merBearing = flattenCalcs(calculations).entrySet()
        .stream()
        .filter(e -> MER_BEARING_TYPES.contains(e.getValue().getHoldingType()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return convertMarketValuesToTargetCurrency(merBearing, targetCurrency);
  }

  @Override
  protected FeesResult calculateAverageValue(AverageMerCommand command, List<FeeAggregationMode> modes,
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations) {
    var result = new FeesResult();
    if (modes.contains(FUNDS_ONLY)) {
      result.getAnnualFee().put(FUNDS_ONLY, getFundsOnlySum(calculations));
    }
    if (modes.contains(WHOLE_PORTFOLIO)) {
      result.getAnnualFee().put(WHOLE_PORTFOLIO, getWholePortfolioSum(calculations));
    }
    if (modes.contains(FUNDS_ONLY_STRICT)) {
      result.getAnnualFee().put(FUNDS_ONLY_STRICT, getFundsOnlyStrictSum(calculations));
    }
    Set<TimePeriod> periods = projectionProperties.periodsFor(command.getProjectionPeriods());
    result.getAnnualFee().forEach((mode, annual) -> {
      result.getMonthlyFee().put(mode, monthlyFrom(annual));
      result.getProjectedSpend().put(mode, projectedFrom(annual, periods));
    });
    return result;
  }

  private static BigDecimal monthlyFrom(BigDecimal annual) {
    return annual == null ? null : divide(annual, TWELVE);
  }

  private Map<TimePeriod, BigDecimal> projectedFrom(BigDecimal annual, Set<TimePeriod> periods) {
    return annual == null
        ? null
        : FeeProjectionUtils.byPeriod(annual, projectionProperties.getAnnualGrowthRate(), periods);
  }

  @Override
  protected void nullOutEmptyFundModes(FeesResult response, AverageMerCommand command) {
    nullOutEmptyFundModes(response.getAnnualFee(), command);
    nullOutEmptyFundModes(response.getMonthlyFee(), command);
    if (hasNoFundHolding(command)) {
      FUND_ONLY_MODES.stream()
          .filter(response.getProjectedSpend()::containsKey)
          .forEach(mode -> response.getProjectedSpend().put(mode, null));
    }
  }
}
