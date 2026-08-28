package ca.tangerine.pce.application.calculation.service.fee;

import ca.tangerine.pce.model.domain.calculation.fee.AverageManagementExpenseCalculation;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.error.Notification;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

import static ca.tangerine.pce.model.error.ErrorCode.MISSING_GROSS_EXPENSE_RATIO;
import static ca.tangerine.pce.model.error.ErrorCode.MISSING_MANAGEMENT_EXPENSE_RATIO;
import static ca.tangerine.pce.model.error.ErrorCode.MISSING_NET_EXPENSE_RATIO;

/**
 * Per-holding fee data source. Each value pairs an extractor (how to read a fee field from
 * {@link AverageManagementExpenseCalculation}) with the {@link ErrorCode} to emit when the field is missing.
 *
 * <p>
 * Sources are composed into per-country chains by {@link CountryFeeResolutionStrategy} implementations and walked by
 * {@link MerFeeResolver}. {@link #MANAGEMENT_FEE} has no missing-warning code — it is the terminal chain element; if it
 * is also null the resolver throws {@link ErrorCode#MISSING_FUND_FEE_DATA}.
 */
public enum FeeSource {

  MER(AverageManagementExpenseCalculation::getManagementExpenseRatio, MISSING_MANAGEMENT_EXPENSE_RATIO),
  NER(AverageManagementExpenseCalculation::getNetExpenseRatio, MISSING_NET_EXPENSE_RATIO),
  GER(AverageManagementExpenseCalculation::getGrossExpenseRatio, MISSING_GROSS_EXPENSE_RATIO),
  MANAGEMENT_FEE(AverageManagementExpenseCalculation::getActualManagementFee, null);

  private final Function<AverageManagementExpenseCalculation, BigDecimal> extractor;
  private final ErrorCode missingWarning;

  FeeSource(Function<AverageManagementExpenseCalculation, BigDecimal> extractor, ErrorCode missingWarning) {
    this.extractor = extractor;
    this.missingWarning = missingWarning;
  }

  /** The fee value for this source on the given calculation, empty if null. */
  public Optional<BigDecimal> extract(AverageManagementExpenseCalculation calc) {
    return Optional.ofNullable(extractor.apply(calc));
  }

  /** Notification to emit when this source was skipped because its value was null. Empty for terminal sources. */
  public Optional<Notification> warningIfMissing(PortfolioHolding holding) {
    return Optional.ofNullable(missingWarning).map(code -> code.toNotificationForHolding(holding));
  }
}
