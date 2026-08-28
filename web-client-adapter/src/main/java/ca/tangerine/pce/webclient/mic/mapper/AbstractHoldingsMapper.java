package ca.tangerine.pce.webclient.mic.mapper;

import ca.tangerine.pce.model.domain.calculation.holding.CommonHolding;
import ca.tangerine.pce.model.domain.calculation.holding.CommonTopHoldings;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.util.BigDecimalUtils;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.datapoint.DatapointMetadata;
import ca.tangerine.wm.commons.domain.enumeration.LanguageCode;
import ca.tangerine.wm.commons.domain.holding.Holding;
import ca.tangerine.wm.commons.domain.holding.SecurityHolding;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.IdentifiersDatapoint;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;
import ca.tangerine.wm.commons.domain.value.MultilingualString;

import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * Maps an MIC holdings payload to {@link CommonTopHoldings}. Every payload variant carries the same three things — an
 * allocation of {@link SecurityHolding}, a currency and the provenance from {@link DatapointMetadata} — so subclasses
 * only expose the first two and inherit the conversion.
 *
 * @param <R>
 *          the Market Investment Catalogue holdings payload type
 */
public abstract class AbstractHoldingsMapper<R extends DatapointMetadata>
    implements
      MarketInvestmentCatalogueResponseMapper<CommonTopHoldings, R> {

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

  protected abstract List<SecurityHolding> allocationOf(R micResponse);

  protected abstract Currency currencyOf(R micResponse);

  @Override
  public CommonTopHoldings map(R micResponse, PortfolioHolding holding) {
    List<CommonHolding> holdings = Optional.ofNullable(micResponse)
        .map(this::allocationOf)
        .orElse(List.of())
        .stream()
        .map(this::toCommonHolding)
        .toList();

    List<DataProvider> providers = Optional.ofNullable(micResponse)
        .map(DatapointMetadata::getDataProviders)
        .orElseGet(List::of);

    Currency currency = Optional.ofNullable(micResponse).map(this::currencyOf).orElse(null);

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
    // The shared SMS model exposes the same typed holding vocabulary used by the calculation domain.
    holding.setType(sh.getType());
    holding.setValue(sh.getMarketValue());
    // MIC returns weighting on a percent (0-100) scale; the calculation expects a unitless ratio (0-1).
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
