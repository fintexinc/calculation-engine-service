package ca.tangerine.pce.application.calculation.service.fee;

import ca.tangerine.wm.commons.domain.enumeration.Country;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Canadian fund chain: MER → Management Fee. NER and GER are US regulatory metrics and are intentionally excluded —
 * including them would emit warnings about fields a Canadian fund was never expected to populate.
 */
@Component
public class CanadianFeeResolutionStrategy implements CountryFeeResolutionStrategy {

  private static final List<FeeSource> SOURCES = List.of(FeeSource.MER, FeeSource.MANAGEMENT_FEE);

  @Override
  public Country country() {
    return Country.CANADA;
  }

  @Override
  public List<FeeSource> sources() {
    return SOURCES;
  }
}
