package com.fintex.ce.adapter.webclient.mic.mapper;

import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.domain.allocation.EquitySectorDatapoint;
import com.fintex.wm.commons.domain.allocation.EquitySectorWithCurrency;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Maps the scalar {@code EQUITY_SECTOR} attribute — the sector of a security that has exactly one, an individual
 * company — onto the same {@link EquitySector} the per-bucket allocation maps onto, as a single bucket carrying the
 * whole weight.
 *
 * <p>
 * Turning the scalar into a one-bucket distribution here rather than in Market Investment Catalogue is what keeps the
 * metric free of a stock branch: a company's whole weight in its one sector is arithmetically the same statement as a
 * fund's distribution over several, so the breakdown pipeline needs no second code path. Market Investment Catalogue
 * stores the fact scalar, because storing the derived vector beside the sector it comes from would keep two copies of
 * it in step only until the taxonomy grows.
 *
 * <p>
 * A row with no sector yields an empty allocation map rather than an {@code UNKNOWN} bucket, so the consuming metric
 * reports the missing data with the same warning it uses for a fund whose allocation is absent.
 */
@Component
public class EquitySectorMapper
    implements
      MarketInvestmentCatalogueResponseMapper<EquitySector, EquitySectorWithCurrency> {

  @Override
  public EquitySector map(EquitySectorWithCurrency micResponse, PortfolioHolding holding) {
    return EquitySector.builder()
        .allocations(toAllocations(micResponse))
        .currency(toCurrency(micResponse))
        .providers(toProviders(micResponse))
        .build();
  }

  private Map<EquitySectorAllocationType, BigDecimal> toAllocations(EquitySectorWithCurrency micResponse) {
    Map<EquitySectorAllocationType, BigDecimal> allocations = new EnumMap<>(EquitySectorAllocationType.class);
    sector(micResponse).ifPresent(sector -> allocations.put(sector, BigDecimal.ONE));
    return allocations;
  }

  private Optional<EquitySectorAllocationType> sector(EquitySectorWithCurrency micResponse) {
    return Optional.ofNullable(micResponse)
        .map(EquitySectorWithCurrency::getSector)
        .map(EquitySectorDatapoint::getValue);
  }

  private Currency toCurrency(EquitySectorWithCurrency micResponse) {
    return Optional.ofNullable(micResponse)
        .map(EquitySectorWithCurrency::getCurrency)
        .map(CurrencyDatapoint::getValue)
        .orElse(null);
  }

  private List<DataProvider> toProviders(EquitySectorWithCurrency micResponse) {
    return Optional.ofNullable(micResponse)
        .map(EquitySectorWithCurrency::getSector)
        .map(EquitySectorDatapoint::getDataProviders)
        .orElseGet(List::of);
  }
}
