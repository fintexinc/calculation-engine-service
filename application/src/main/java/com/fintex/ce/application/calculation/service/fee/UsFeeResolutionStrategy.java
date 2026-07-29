package com.fintex.ce.application.calculation.service.fee;

import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.enumeration.Country;

import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.model.error.ErrorCode.MISSING_NER_AND_GER;

/**
 * US fund chain: NER → GER. NER (net) is the after-waiver US operating-expense ratio; GER (gross) is pre-waiver. MER is
 * a Canadian regulatory term; US Morningstar data only populates it for US ETFs (where it duplicates NER) and never for
 * US mutual funds, so it is intentionally excluded from this chain.
 *
 * <p>
 * The Management Fee is <b>not</b> a fallback here: it is a different datapoint from the operating expense ratio, so
 * quoting it as a fee ratio would misrepresent the number. When both NER and GER are missing for a US fund or ETF the
 * chain is exhausted and the request fails with {@link ErrorCode#MISSING_NER_AND_GER} (MER-002) instead of falling back
 * to the Management Fee or returning 0.
 */
@Component
public class UsFeeResolutionStrategy implements CountryFeeResolutionStrategy {

  private static final List<FeeSource> SOURCES = List.of(
      FeeSource.NER,
      FeeSource.GER);

  @Override
  public Country country() {
    return Country.USA;
  }

  @Override
  public List<FeeSource> sources() {
    return SOURCES;
  }

  @Override
  public ErrorCode exhaustedError() {
    return MISSING_NER_AND_GER;
  }
}
