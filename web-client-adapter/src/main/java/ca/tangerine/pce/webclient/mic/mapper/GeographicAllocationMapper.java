package ca.tangerine.pce.webclient.mic.mapper;

import ca.tangerine.pce.model.domain.calculation.allocation.HoldingGeographicAllocation;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.allocation.GeographicAllocation;
import ca.tangerine.wm.commons.domain.allocation.GeographicAllocationValue;
import ca.tangerine.wm.commons.domain.allocation.GeographicAllocationWithCurrency;
import ca.tangerine.wm.commons.domain.allocation.GeographicRegionType;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.currency.CurrencyDatapoint;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps the MIC geographic-allocation response ({@link GeographicAllocationWithCurrency}) into the domain
 * {@link HoldingGeographicAllocation}. Pulls the typed allocations from the nested {@link GeographicAllocation}, the
 * data provider from the wrapper, and the trading currency from the {@link CurrencyDatapoint}.
 */
@Component
public class GeographicAllocationMapper
    implements
      MarketInvestmentCatalogueResponseMapper<HoldingGeographicAllocation, GeographicAllocationWithCurrency> {

  @Override
  public HoldingGeographicAllocation map(GeographicAllocationWithCurrency micResponse, PortfolioHolding holding) {
    return HoldingGeographicAllocation.builder()
        .allocations(toTypedAllocations(micResponse))
        .currency(toCurrency(micResponse))
        .providers(providers(micResponse))
        .build();
  }

  private Map<GeographicRegionType, BigDecimal> toTypedAllocations(GeographicAllocationWithCurrency micResponse) {
    if (micResponse == null || micResponse.getGeographicAllocation() == null
        || CollectionUtils.isEmpty(micResponse.getGeographicAllocation().getAllocations())) {
      return new EnumMap<>(GeographicRegionType.class);
    }
    return micResponse.getGeographicAllocation().getAllocations().stream()
        .filter(value -> value != null && value.getType() != null && value.getValue() != null)
        .collect(Collectors.toMap(
            GeographicAllocationValue::getType,
            GeographicAllocationValue::getValue,
            BigDecimal::add,
            () -> new EnumMap<>(GeographicRegionType.class)));
  }

  private Currency toCurrency(GeographicAllocationWithCurrency micResponse) {
    return Optional.ofNullable(micResponse)
        .map(GeographicAllocationWithCurrency::getCurrency)
        .map(CurrencyDatapoint::getValue)
        .orElse(null);
  }

  private List<DataProvider> providers(GeographicAllocationWithCurrency micResponse) {
    if (micResponse == null) {
      return List.of();
    }
    List<DataProvider> providers = Optional.ofNullable(micResponse.getGeographicAllocation())
        .map(GeographicAllocation::getDataProviders)
        .orElseGet(micResponse::getDataProviders);
    return providers == null ? List.of() : providers;
  }
}
