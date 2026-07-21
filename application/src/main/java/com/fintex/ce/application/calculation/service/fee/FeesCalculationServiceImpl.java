package com.fintex.ce.application.calculation.service.fee;

import com.fintex.ce.application.calculation.service.DefaultTargetCurrencyConverter;
import com.fintex.ce.model.domain.calculation.fee.AverageManagementExpenseCalculation;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.FeesResult;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fintex.ce.application.constant.HoldingTypeGroup.MER_BEARING_TYPES;
import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY_STRICT;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.WHOLE_PORTFOLIO;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;

/**
 * Annual / Monthly fee dollar amounts in the configured default target currency (default CAD). Annual = Σ (marketValue
 * × resolved fee) per aggregation mode using {@link MerFeeResolver}; Monthly = Annual ÷ 12.
 *
 * <p>
 * FX: each MER-bearing holding's {@code marketValue} is converted to the default target currency via
 * {@link DefaultTargetCurrencyConverter} before the sum. Missing currency on a MER-bearing holding → hard error
 * ({@link com.fintex.ce.model.error.ErrorCode#HOLDING_MISSING_CURRENCY_FROM_FDS}). Rate unavailable → emit
 * {@link com.fintex.ce.model.error.ErrorCode#FX_RATES_UNAVAILABLE} and leave the value in its native currency. Zero-fee
 * holdings (stocks, cash, GIC, fixed income) are skipped — they contribute 0 either way.
 */
@Service
public class FeesCalculationServiceImpl extends AbstractFeeCalculationService<FeesResult> {

  private final FeeResolver feeResolver;

  public FeesCalculationServiceImpl(DefaultTargetCurrencyConverter defaultTargetCurrencyConverter,
      MerFeeResolver feeResolver) {
    super(defaultTargetCurrencyConverter);
    this.feeResolver = feeResolver;
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
      AverageMerCommand command) {
    Map<PortfolioHolding, AverageManagementExpenseCalculation> merBearing = flattenCalcs(calculations).entrySet()
        .stream()
        .filter(e -> MER_BEARING_TYPES.contains(e.getValue().getHoldingType()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return convertMarketValuesToDefaultTargetCurrency(merBearing);
  }

  @Override
  protected FeesResult calculateAverageValue(List<FeeAggregationMode> modes,
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
    result.getAnnualFee().forEach((mode, annual) -> result.getMonthlyFee().put(mode, monthlyFrom(annual)));
    return result;
  }

  private static BigDecimal monthlyFrom(BigDecimal annual) {
    return annual == null ? null : divide(annual, TWELVE);
  }

  @Override
  protected void nullOutEmptyFundModes(FeesResult response, AverageMerCommand command) {
    nullOutEmptyFundModes(response.getAnnualFee(), command);
    nullOutEmptyFundModes(response.getMonthlyFee(), command);
  }
}
