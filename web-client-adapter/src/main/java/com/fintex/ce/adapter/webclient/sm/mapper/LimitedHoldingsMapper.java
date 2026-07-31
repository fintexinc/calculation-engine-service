package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.holding.Holdings;
import com.fintex.wm.commons.domain.holding.SecurityHolding;

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
  protected List<SecurityHolding> allocationOf(Holdings smsResponse) {
    return smsResponse.getAllocation();
  }

  @Override
  protected Currency currencyOf(Holdings smsResponse) {
    return smsResponse.getCurrency();
  }
}
