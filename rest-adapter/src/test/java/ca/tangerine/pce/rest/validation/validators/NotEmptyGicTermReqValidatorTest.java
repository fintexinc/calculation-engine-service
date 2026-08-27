package ca.tangerine.pce.rest.validation.validators;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static ca.tangerine.pce.model.domain.enumeration.CalculationMetric.ASSET_ALLOCATIONS;
import static ca.tangerine.pce.model.domain.enumeration.CalculationMetric.SECTOR_EXPOSURE;
import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.model.domain.holding.GicHolding;
import ca.tangerine.pce.rest.validation.RequestValidator;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;

class NotEmptyGicTermReqValidatorTest extends AbstractGicFieldReqValidatorTest {

  /**
   * A metric that buckets a GIC by its term has to opt into this validator, because
   * {@code GicHolding#getAssetAllocationRegionType()} reads {@code getTerm()} with no null guard: registered, a
   * term-less GIC is a 400 at the boundary; unregistered, the same request reaches the calculation and is an unhandled
   * NPE. The tests above exercise the check itself but say nothing about which metrics it runs for, so the registration
   * is pinned here — {@code SECTOR_EXPOSURE} because it is the newest metric to bucket GICs that way, alongside the
   * asset allocation whose classification rule it follows.
   */
  @Test
  void shouldSupportEveryMetricThatBucketsGicsByTerm() {
    assertThat(createValidator().supportedMetrics()).contains(SECTOR_EXPOSURE, ASSET_ALLOCATIONS);
  }

  @Override
  RequestValidator createValidator() {
    return new NotEmptyGicTermReqValidator();
  }

  @Override
  GicHolding createInvalidGicHolding() {
    return GicHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.GIC)
        .term(null)
        .build();
  }

  @Override
  GicHolding createValidGicHolding() {
    return GicHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.GIC)
        .term(BigDecimal.valueOf(365))
        .build();
  }

  @Override
  String expectedErrorCode() {
    return "GIC_HOLDING_MISSING_TERM";
  }

  @Override
  String expectedMessage() {
    return "The gic holding GIC-CAD-10 is missing term";
  }
}
