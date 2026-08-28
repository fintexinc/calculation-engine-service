package ca.tangerine.pce.webclient.mic.mapper;

import ca.tangerine.pce.model.domain.calculation.allocation.FixedIncomeBondSector;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.allocation.FixedIncomeSectorAllocation;
import ca.tangerine.wm.commons.domain.allocation.FixedIncomeSectorAllocationType;
import ca.tangerine.wm.commons.domain.allocation.FixedIncomeSectorAllocationTypeValue;
import ca.tangerine.wm.commons.domain.allocation.FixedIncomeSectorAllocationWithCurrency;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.currency.CurrencyDatapoint;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps MIC {@link FixedIncomeSectorAllocationWithCurrency} (whose nested {@link FixedIncomeSectorAllocation} is keyed
 * by the canonical {@link FixedIncomeSectorAllocationType} 8-bucket taxonomy) to PCE {@link FixedIncomeBondSector}. MIC
 * already classifies each entry into a typed bucket, so the mapper reads the type directly with no translation.
 */
@Component
public class FixedIncomeSectorAllocationMapper
    implements
      MarketInvestmentCatalogueResponseMapper<FixedIncomeBondSector, FixedIncomeSectorAllocationWithCurrency> {

  @Override
  public FixedIncomeBondSector map(FixedIncomeSectorAllocationWithCurrency micResponse, PortfolioHolding holding) {
    Map<FixedIncomeSectorAllocationType, BigDecimal> allocationMap = Optional.ofNullable(micResponse)
        .map(FixedIncomeSectorAllocationWithCurrency::getFixedIncomeSectorAllocation)
        .map(FixedIncomeSectorAllocation::getAllocations)
        .orElseGet(List::of)
        .stream()
        .filter(entry -> entry.getType() != null && entry.getValue() != null)
        .collect(Collectors.toMap(
            FixedIncomeSectorAllocationTypeValue::getType,
            FixedIncomeSectorAllocationTypeValue::getValue,
            BigDecimal::add,
            () -> new EnumMap<>(FixedIncomeSectorAllocationType.class)));

    final List<DataProvider> providers = Optional.ofNullable(micResponse)
        .map(FixedIncomeSectorAllocationWithCurrency::getFixedIncomeSectorAllocation)
        .map(FixedIncomeSectorAllocation::getDataProviders)
        .orElseGet(List::of);

    return FixedIncomeBondSector.builder()
        .fixedIncomeBondSectors(allocationMap)
        .holdingType(holding.getHoldingType())
        .currency(toCurrency(micResponse))
        .providers(providers)
        .build();
  }

  private Currency toCurrency(FixedIncomeSectorAllocationWithCurrency micResponse) {
    return Optional.ofNullable(micResponse)
        .map(FixedIncomeSectorAllocationWithCurrency::getCurrency)
        .map(CurrencyDatapoint::getValue)
        .orElse(null);
  }
}
