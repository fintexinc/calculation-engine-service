package ca.tangerine.pce.webclient.mic.mapper;

import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.holding.Holdings;
import ca.tangerine.wm.commons.domain.holding.SecurityHolding;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Maps the {@code LIMITED_HOLDINGS} payload — the holdings table decomposed through nested funds and aggregated per
 * instrument — into the shared holdings domain shape. The allocation is already flat, so no underlying holdings come
 * back and the calculation's tree expansion has nothing to descend into.
 */
@Component
public class LimitedHoldingsMapper extends AbstractHoldingsMapper<Holdings> {

  @Override
  protected List<SecurityHolding> allocationOf(Holdings micResponse) {
    return micResponse.getAllocation();
  }

  @Override
  protected Currency currencyOf(Holdings micResponse) {
    return micResponse.getCurrency();
  }
}
