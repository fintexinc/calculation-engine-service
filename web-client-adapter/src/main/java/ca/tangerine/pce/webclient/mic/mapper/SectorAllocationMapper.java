package ca.tangerine.pce.webclient.mic.mapper;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import ca.tangerine.pce.model.domain.calculation.allocation.HoldingSectorAllocation;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.allocation.SectorAllocation;
import ca.tangerine.wm.commons.domain.allocation.SectorAllocationType;
import ca.tangerine.wm.commons.domain.allocation.SectorAllocationValue;
import ca.tangerine.wm.commons.domain.allocation.SectorAllocationWithCurrency;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.currency.CurrencyDatapoint;

@Component
public class SectorAllocationMapper
    implements
      MarketInvestmentCatalogueResponseMapper<HoldingSectorAllocation, SectorAllocationWithCurrency> {

  @Override
  public HoldingSectorAllocation map(SectorAllocationWithCurrency micResponse, PortfolioHolding holding) {
    Map<SectorAllocationType, BigDecimal> allocationMap = Optional.ofNullable(micResponse)
        .map(SectorAllocationWithCurrency::getSectorAllocation)
        .map(SectorAllocation::getAllocations)
        .orElseGet(List::of)
        .stream()
        .filter(entry -> entry.getType() != null && entry.getValue() != null)
        .collect(Collectors.toMap(
            SectorAllocationValue::getType,
            SectorAllocationValue::getValue,
            (existing, replacement) -> existing,
            () -> new EnumMap<>(SectorAllocationType.class)));

    final List<DataProvider> providers = Optional.ofNullable(micResponse)
        .map(SectorAllocationWithCurrency::getSectorAllocation)
        .map(SectorAllocation::getDataProviders)
        .orElseGet(List::of);

    return HoldingSectorAllocation.builder()
        .allocations(allocationMap)
        .currency(toCurrency(micResponse))
        .providers(providers)
        .build();
  }

  private Currency toCurrency(SectorAllocationWithCurrency micResponse) {
    return Optional.ofNullable(micResponse)
        .map(SectorAllocationWithCurrency::getCurrency)
        .map(CurrencyDatapoint::getValue)
        .orElse(null);
  }
}
