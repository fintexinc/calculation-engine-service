package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.holding.CommonHolding;
import com.fintex.ce.model.domain.calculation.holding.CommonTopHoldings;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.util.BigDecimalUtils;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.LanguageCode;
import com.fintex.wm.commons.domain.holding.Holding;
import com.fintex.wm.commons.domain.holding.SecurityHolding;
import com.fintex.wm.commons.domain.holding.TopHoldings;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.IdentifiersDatapoint;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.value.MultilingualString;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
/**
 * Maps SM TopHoldings response to PCE CommonTopHoldings domain model. Produces {@link CommonHolding} directly so the
 * calculation pipeline has a single domain shape; the parent reference and final weight are populated later by
 * {@code CommonHoldingsService} during tree expansion.
 */
@Component
public class TopHoldingsMapper implements SecurityMasterResponseMapper<CommonTopHoldings, TopHoldings> {

  /**
   * Priority order for the primary identifier of each underlying holding. MORNINGSTAR_ID is most reliable across
   * providers and is preferred; the rest are listed in decreasing global uniqueness. This identifier is what the
   * calculation uses for the cycle guard and what the response surfaces to clients. Best-effort fallback to a display
   * triple happens in the calculation when none of these are populated.
   */
  private static final List<FiIdentifierType> IDENTIFIER_PRIORITY = List.of(
      FiIdentifierType.MORNINGSTAR_ID,
      FiIdentifierType.TICKER,
      FiIdentifierType.FUNDSERV,
      FiIdentifierType.ISIN,
      FiIdentifierType.CUSIP);

  @Override
  public CommonTopHoldings map(TopHoldings smsResponse, PortfolioHolding holding) {
    List<CommonHolding> holdings = Optional.ofNullable(smsResponse)
        .map(TopHoldings::getAllocation)
        .orElse(List.of())
        .stream()
        .map(this::toCommonHolding)
        .toList();

    List<DataProvider> providers = Optional.ofNullable(smsResponse)
        .map(TopHoldings::getDataProviders)
        .orElseGet(List::of);

    Currency currency = Optional.ofNullable(smsResponse).map(TopHoldings::getCurrency).orElse(null);

    return CommonTopHoldings.builder()
        .currency(currency)
        .holdings(holdings)
        .providers(providers)
        .build();
  }

  private CommonHolding toCommonHolding(SecurityHolding sh) {
    CommonHolding holding = new CommonHolding();
    holding.setName(extractEnglishName(sh.getName()));
    holding.setCompanyName(sh.getCompanyName());
    holding.setType(sh.getType());
    holding.setValue(sh.getMarketValue());
    // SM returns weighting on a percent (0-100) scale; the calculation expects a unitless ratio (0-1).
    holding.setWeight(BigDecimalUtils.percentageToRatio(sh.getWeighting()));
    holding.setPrimaryIdentifier(extractPrimaryIdentifier(sh.getIdentifiers()));
    holding.setUnderlyingHoldings(mapUnderlyingHoldings(sh.getUnderlyingHoldings()));
    return holding;
  }

  private List<CommonHolding> mapUnderlyingHoldings(List<Holding> underlyingHoldings) {
    if (underlyingHoldings == null || underlyingHoldings.isEmpty()) {
      return Collections.emptyList();
    }
    return underlyingHoldings.stream()
        .filter(SecurityHolding.class::isInstance)
        .map(SecurityHolding.class::cast)
        .map(this::toCommonHolding)
        .toList();
  }

  private SecurityIdentifier extractPrimaryIdentifier(IdentifiersDatapoint identifiers) {
    List<SecurityIdentifier> all = Optional.ofNullable(identifiers)
        .map(IdentifiersDatapoint::getIdentifiers)
        .orElse(List.of());
    return IDENTIFIER_PRIORITY.stream()
        .flatMap(type -> all.stream().filter(id -> type.equals(id.getIdType())).limit(1))
        .findFirst()
        .orElse(null);
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
