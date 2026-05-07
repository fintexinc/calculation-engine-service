package com.fintex.ce.application.calculation.service.fee;

import com.fintex.wm.commons.domain.enumeration.Country;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * US fund chain: NER → GER → Management Fee. NER (net) is the after-waiver US operating-expense ratio; GER (gross) is
 * pre-waiver. MER is a Canadian regulatory term; US Morningstar data only populates it for ETF_US (where it duplicates
 * NER) and never for US mutual funds, so it is intentionally excluded from this chain.
 */
@Component
public class UsFeeResolutionStrategy implements CountryFeeResolutionStrategy {

  private static final List<FeeSource> SOURCES = List.of(
      FeeSource.NER,
      FeeSource.GER,
      FeeSource.MANAGEMENT_FEE);

  @Override
  public Country country() {
    return Country.USA;
  }

  @Override
  public List<FeeSource> sources() {
    return SOURCES;
  }
}
