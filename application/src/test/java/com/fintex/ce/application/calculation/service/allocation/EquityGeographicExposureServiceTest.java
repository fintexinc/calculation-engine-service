package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.model.domain.calculation.allocation.HoldingGeographicAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.EquityGeographicExposureResult;
import com.fintex.wm.commons.domain.allocation.GeographicRegionType;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.financial.Geography;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.application.util.TestConstants.DEFAULT_DATA_PROPERTIES;
import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EquityGeographicExposureServiceTest
    extends
      AbstractGeographicExposureServiceTest<EquityGeographicExposureResult> {

  @Override
  protected AbstractGeographicExposureService<EquityGeographicExposureResult> createService() {
    return new EquityGeographicExposureService(geographicFetcher, geographyFetcher, portfolioWeightCalculator,
        DEFAULT_DATA_PROPERTIES);
  }

  @Override
  protected PortfolioHolding typeSpecificRelevantHolding() {
    return canadaStock("RY", 100);
  }

  @Override
  protected Map<PortfolioHolding, HoldingGeographicAllocation> typeSpecificFundAllocations(
      PortfolioHolding typeSpecific) {
    return Map.of();
  }

  @Override
  protected Map<PortfolioHolding, Geography> typeSpecificStockGeographies(PortfolioHolding typeSpecific) {
    return Map.of(typeSpecific, geography(Country.CANADA, Currency.CAD));
  }

  @Override
  protected Map<GeographicRegionType, BigDecimal> expectedForHeterogeneousPortfolio() {
    return distribution(Map.of(
        GeographicRegionType.US, new BigDecimal("0.425"),
        GeographicRegionType.CANADA, new BigDecimal("0.375"),
        GeographicRegionType.EUROPE, new BigDecimal("0.2")));
  }

  @Override
  protected PortfolioHolding excludedSecurityHolding() {
    return fixedIncome("BOND", 200);
  }

  @Override
  protected String expectedMissingFundAllocationCode() {
    return "FDS-016";
  }

  @Test
  void shouldResolveStockRegionsViaGeographyFetcher_andAttributeByBusinessCountry() {
    PortfolioHolding usStock = usStock("AAPL", 200);
    PortfolioHolding canadaStock = canadaStock("RY", 200);
    PortfolioHolding fund = canadaMutualFund("RBF605", 100);

    when(geographicFetcher.fetch(anyList(), anyList())).thenReturn(Map.of(
        fund, allocation(Map.of(GeographicRegionType.EUROPE, ONE), Currency.CAD)));
    when(geographyFetcher.fetch(anyList(), anyList())).thenReturn(Map.of(
        usStock, geography(Country.USA, Currency.USD),
        canadaStock, geography(Country.CANADA, Currency.CAD)));

    EquityGeographicExposureResult result = service.perform(command(usStock, canadaStock, fund));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<PortfolioHolding>> stockCaptor = ArgumentCaptor.forClass(List.class);
    verify(geographyFetcher).fetch(stockCaptor.capture(), anyList());
    assertThat(stockCaptor.getValue()).containsExactlyInAnyOrder(usStock, canadaStock);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<PortfolioHolding>> fundCaptor = ArgumentCaptor.forClass(List.class);
    verify(geographicFetcher).fetch(fundCaptor.capture(), anyList());
    assertThat(fundCaptor.getValue()).containsExactly(fund);

    Map<GeographicRegionType, BigDecimal> expected = distribution(Map.of(
        GeographicRegionType.US, new BigDecimal("0.4"),
        GeographicRegionType.CANADA, new BigDecimal("0.4"),
        GeographicRegionType.EUROPE, new BigDecimal("0.2")));
    assertExposureEquals(result, expected);
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldMapNonNorthAmericanBusinessCountriesToTheirRegion() {
    PortfolioHolding japanStock = usStock("4875.T", 100);
    PortfolioHolding germanyStock = usStock("TL0.DE", 100);
    PortfolioHolding brazilStock = usStock("AURA33.SA", 100);
    PortfolioHolding southAfricaStock = usStock("ZAFR.JO", 100);

    when(geographyFetcher.fetch(anyList(), anyList())).thenReturn(Map.of(
        japanStock, geography(Country.JAPAN, Currency.JPY),
        germanyStock, geography(Country.GERMANY, Currency.EUR),
        brazilStock, geography(Country.BRAZIL, Currency.USD),
        southAfricaStock, geography(Country.SOUTH_AFRICA, Currency.USD)));

    EquityGeographicExposureResult result = service.perform(
        command(japanStock, germanyStock, brazilStock, southAfricaStock));

    Map<GeographicRegionType, BigDecimal> expected = distribution(Map.of(
        Country.JAPAN.getGeographyRegion(), new BigDecimal("0.25"),
        Country.GERMANY.getGeographyRegion(), new BigDecimal("0.25"),
        Country.BRAZIL.getGeographyRegion(), new BigDecimal("0.25"),
        Country.SOUTH_AFRICA.getGeographyRegion(), new BigDecimal("0.25")));
    assertExposureEquals(result, expected);
  }

  @Test
  void shouldEmitMissingBusinessCountryWarning_whenStockGeographyIsMissing() {
    PortfolioHolding fund = canadaMutualFund("RBF605", 100);
    PortfolioHolding unmappedStock = usStock("UNKNOWN", 100);

    when(geographicFetcher.fetch(anyList(), anyList())).thenReturn(Map.of(
        fund, allocation(Map.of(GeographicRegionType.US, ONE), Currency.CAD)));
    when(geographyFetcher.fetch(anyList(), anyList())).thenReturn(Map.of());

    EquityGeographicExposureResult result = service.perform(command(fund, unmappedStock));

    assertExposureEquals(result, distribution(Map.of(GeographicRegionType.US, ONE)));
    assertThat(result.getWarnings()).hasSize(1);
    assertThat(result.getWarnings().get(0).getCode()).isEqualTo("FDS-026");
  }
}
