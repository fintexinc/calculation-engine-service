package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.holding.CommonHolding;
import com.fintex.ce.model.domain.calculation.holding.CommonTopHoldings;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.util.BigDecimalUtils;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.datapoint.DatapointMetadata;
import com.fintex.wm.commons.domain.enumeration.LanguageCode;
import com.fintex.wm.commons.domain.holding.Holding;
import com.fintex.wm.commons.domain.holding.SecurityHolding;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.IdentifiersDatapoint;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.value.MultilingualString;

import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * Maps an SM holdings payload to {@link CommonTopHoldings}. Every payload variant carries the same three things — an
 * allocation of {@link SecurityHolding}, a currency and the provenance from {@link DatapointMetadata} — so subclasses
 * only expose the first two and inherit the conversion.
 *
 * @param <R>
 *          the Security Master holdings payload type
 */
public abstract class AbstractHoldingsMapper<R extends DatapointMetadata>
    implements
      SecurityMasterResponseMapper<CommonTopHoldings, R> {

  /**
   * Priority order for the primary identifier of each underlying holding, in decreasing global uniqueness —
   * MORNINGSTAR_ID is the most reliable across providers, TICKER the least, since the same symbol is reused across
   * exchanges. This identifier is what the calculation uses for the cycle guard and what the response surfaces to
   * clients; when none are populated the calculation falls back to a display triple.
   */
  private static final List<FiIdentifierType> IDENTIFIER_PRIORITY = List.of(
      FiIdentifierType.MORNINGSTAR_ID,
      FiIdentifierType.FUNDSERV,
      FiIdentifierType.ISIN,
      FiIdentifierType.CUSIP,
      FiIdentifierType.TICKER);

  protected abstract List<? extends SecurityHolding> allocationOf(R smsResponse);

  protected abstract Currency currencyOf(R smsResponse);

  @Override
  public CommonTopHoldings map(R smsResponse, PortfolioHolding holding) {
    List<CommonHolding> holdings = Optional.ofNullable(smsResponse)
        .map(this::allocationOf)
        .orElse(List.of())
        .stream()
        .map(this::toCommonHolding)
        .toList();

    List<DataProvider> providers = Optional.ofNullable(smsResponse)
        .map(DatapointMetadata::getDataProviders)
        .orElseGet(List::of);

    Currency currency = Optional.ofNullable(smsResponse).map(this::currencyOf).orElse(null);

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
    if (CollectionUtils.isEmpty(underlyingHoldings)) {
      return List.of();
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
    if (CollectionUtils.isEmpty(names)) {
      return null;
    }
    return names.stream()
        .filter(n -> LanguageCode.EN.equals(n.getLanguageCode()))
        .map(MultilingualString::getValue)
        .findFirst()
        .orElse(names.get(0).getValue());
  }
}
