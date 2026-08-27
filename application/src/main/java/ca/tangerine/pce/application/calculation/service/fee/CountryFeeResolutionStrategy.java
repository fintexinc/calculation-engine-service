package ca.tangerine.pce.application.calculation.service.fee;

import java.util.List;

import static ca.tangerine.pce.model.error.ErrorCode.MISSING_FUND_FEE_DATA;

import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.domain.enumeration.Country;

/**
 * Strategy: per-country fee resolution policy. Each implementation declares which {@link FeeSource}s apply to its
 * country, in what order, and which error to raise when none of them is populated. The chain-walking algorithm lives in
 * {@link MerFeeResolver} — strategies provide only data, no behaviour.
 *
 * <p>
 * Adding support for a new country = add a new {@code @Component} implementing this interface. The orchestrator's list
 * of strategies grows automatically; no central map to edit.
 */
public interface CountryFeeResolutionStrategy {

  /** Country this strategy resolves fees for. */
  Country country();

  /** Ordered chain of fee fields to try; first non-null value wins. */
  List<FeeSource> sources();

  /**
   * Error raised when every source in {@link #sources()} is missing for a holding. Defaults to the generic
   * {@link ErrorCode#MISSING_FUND_FEE_DATA}; strategies whose exhaustion has a more specific cause (e.g. a US fund
   * missing both NER and GER) override this to point the caller at the exact gap.
   */
  default ErrorCode exhaustedError() {
    return MISSING_FUND_FEE_DATA;
  }
}
