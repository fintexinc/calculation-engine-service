package ca.tangerine.pce.model.error;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;
import ca.tangerine.wm.commons.error.Notification;
import ca.tangerine.wm.commons.error.Severity;

/**
 * Covers the contract every holding-scoped message depends on: the holding id reaches both the rendered message and the
 * metadata map, and it does so identically whether the code is raised as an exception or emitted as a warning.
 */
class ErrorCodeTest {

  private static final PortfolioHolding HOLDING = PortfolioHolding.builder()
      .value(BigDecimal.TEN)
      .holdingType(FinancialInstrumentType.MUTUAL_FUND)
      .securityIdentifier(new SecurityIdentifier("CIG-001", FiIdentifierType.MORNINGSTAR_ID))
      .build();

  @Test
  void exceptionForHolding_namesTheHoldingInTheMessageAndTheMetadata() {
    var exception = ErrorCode.MISSING_MONTHLY_RETURNS.toExceptionForHolding(HOLDING);

    assertThat(exception.getMessage()).isEqualTo(
        "The holding MUTUAL_FUND-CIG-001 is missing values for monthly returns");
    assertThat(exception.getId()).isEqualTo("MUTUAL_FUND-CIG-001");
    assertThat(exception.getMetadata())
        .containsEntry(ErrorParams.HOLDING_ID, "MUTUAL_FUND-CIG-001")
        .containsEntry(ErrorParams.PARAM_KEY_PREFIX + 1, "MUTUAL_FUND-CIG-001");
  }

  @Test
  void exceptionForHolding_placesCallerArgumentsAfterTheHoldingId() {
    var exception = ErrorCode.HOLDING_TYPE_NOT_LEAF.toExceptionForHolding(HOLDING,
        FinancialInstrumentType.FIXED_INCOME);

    assertThat(exception.getMessage())
        .isEqualTo(
            "The holding MUTUAL_FUND-CIG-001 has unsupported holding type FIXED_INCOME; pick a specific subtype");
    assertThat(exception.getMetadata()).containsEntry(ErrorParams.PARAM_KEY_PREFIX + 2,
        FinancialInstrumentType.FIXED_INCOME);
  }

  @Test
  void validationExceptionForHolding_namesTheHoldingToo() {
    var exception = ErrorCode.HOLDING_VALUE_NEGATIVE_OR_NULL.toValidationExceptionForHolding(HOLDING);

    assertThat(exception.getMessage())
        .isEqualTo("The holding MUTUAL_FUND-CIG-001 must have a value greater than or equal to 0 and must not be null");
    assertThat(exception.getMetadata()).containsEntry(ErrorParams.HOLDING_ID, "MUTUAL_FUND-CIG-001");
  }

  @Test
  void portfolioMissingCurrency_isAWarningThatNamesTheAppliedDefault() {
    Notification notification = ErrorCode.PORTFOLIO_MISSING_CURRENCY.asNotification("CAD");

    assertThat(notification.getCode()).isEqualTo("CUR-001");
    assertThat(notification.getSeverity()).isEqualTo(Severity.WARNING);
    assertThat(notification.getMessage()).isEqualTo(
        "Missing target currency in the request. The configured default currency CAD is applied to the result");
  }

  @Test
  void exceptionAndNotificationRenderTheSameHoldingMessage() {
    // The two factories used to disagree: the notification prepended the id, the exception did not, so an ERROR
    // never named the holding its message was about.
    var exception = ErrorCode.HOLDING_MISSING_CURRENCY_FROM_MIC.toExceptionForHolding(HOLDING);
    Notification notification = ErrorCode.MISSING_ASSET_ALLOCATION.toNotificationForHolding(HOLDING);

    assertThat(exception.getMessage()).contains("MUTUAL_FUND-CIG-001");
    assertThat(notification.getMessage()).contains("MUTUAL_FUND-CIG-001");
    assertThat(notification.getMetadata()).containsEntry(ErrorParams.HOLDING_ID, "MUTUAL_FUND-CIG-001");
  }
}
