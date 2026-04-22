package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.holding.CommonTopHoldings;
import com.fintex.ce.model.domain.calculation.holding.CommonTopHoldings.CommonTopHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.enumeration.LanguageCode;
import com.fintex.wm.commons.domain.holding.Holding;
import com.fintex.wm.commons.domain.holding.SecurityHolding;
import com.fintex.wm.commons.domain.holding.TopHoldings;
import com.fintex.wm.commons.domain.id.IdentifiersDatapoint;
import com.fintex.wm.commons.domain.value.MultilingualString;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps SM TopHoldings response to PCE CommonTopHoldings domain model.
 */
@Component
public class TopHoldingsMapper implements SecurityMasterResponseMapper<CommonTopHoldings, TopHoldings> {

  @Override
  public CommonTopHoldings map(TopHoldings smsResponse, PortfolioHolding holding) {
    List<CommonTopHolding> holdings = Optional.ofNullable(smsResponse)
        .map(TopHoldings::getAllocation)
        .orElse(List.of())
        .stream()
        .map(this::toCommonTopHolding)
        .collect(Collectors.toList());

    CommonTopHoldings result = new CommonTopHoldings()
        .setHoldings(holdings)
        .setHoldingId(holding.getSecurityIdentifier().getId());

    Optional.ofNullable(smsResponse)
        .map(TopHoldings::getDataProvider)
        .ifPresent(dp -> result.setProviders(List.of(dp)));

    return result;
  }

  private CommonTopHolding toCommonTopHolding(SecurityHolding sh) {
    CommonTopHolding th = new CommonTopHolding();
    th.setName(extractEnglishName(sh.getName()));
    th.setCompanyName(sh.getCompanyName());
    th.setType(sh.getType());
    th.setValue(sh.getMarketValue());
    th.setWeight(sh.getWeighting());
    th.setIdentifiers(Optional.ofNullable(sh.getIdentifiers())
        .map(IdentifiersDatapoint::getIdentifiers)
        .orElse(List.of()));
    th.setUnderlyingHoldings(mapUnderlyingHoldings(sh.getUnderlyingHoldings()));
    return th;
  }

  private List<CommonTopHolding> mapUnderlyingHoldings(List<Holding> underlyingHoldings) {
    if (underlyingHoldings == null || underlyingHoldings.isEmpty()) {
      return Collections.emptyList();
    }
    return underlyingHoldings.stream()
        .filter(SecurityHolding.class::isInstance)
        .map(SecurityHolding.class::cast)
        .map(this::toCommonTopHolding)
        .collect(Collectors.toList());
  }

  private String extractEnglishName(List<MultilingualString> names) {
    if (names == null || names.isEmpty()) {
      return null;
    }
    return names.stream()
        .filter(n -> LanguageCode.EN.equals(n.getLanguageCode()))
        .map(MultilingualString::getValue)
        .findFirst()
        .orElse(names.get(0).getValue());
  }
}
