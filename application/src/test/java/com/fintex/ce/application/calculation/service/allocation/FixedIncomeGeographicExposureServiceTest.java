package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.model.domain.calculation.allocation.HoldingGeographicAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.FixedIncomeGeographicExposureResult;
import com.fintex.wm.commons.domain.allocation.GeographicRegionType;
import com.fintex.wm.commons.domain.currency.Currency;
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

class FixedIncomeGeographicExposureServiceTest
    extends
      AbstractGeographicExposureServiceTest<FixedIncomeGeographicExposureResult> {

  @Override
  protected AbstractGeographicExposureService<FixedIncomeGeographicExposureResult> createService() {
    return new FixedIncomeGeographicExposureService(geographicFetcher, geographyFetcher, portfolioWeightCalculator,
        DEFAULT_DATA_PROPERTIES);
  }

  @Override
  protected PortfolioHolding typeSpecificRelevantHolding() {
    return fixedIncome("BOND", 100);
  }

  @Override
  protected Map<PortfolioHolding, HoldingGeographicAllocation> typeSpecificFundAllocations(
      PortfolioHolding typeSpecific) {
    return Map.of(typeSpecific, allocation(Map.of(GeographicRegionType.OTHER, ONE), Currency.CAD));
  }

  @Override
  protected Map<PortfolioHolding, Geography> typeSpecificStockGeographies(PortfolioHolding typeSpecific) {
    return Map.of();
  }

  @Override
  protected Map<GeographicRegionType, BigDecimal> expectedForHeterogeneousPortfolio() {
    return distribution(Map.of(
        GeographicRegionType.US, new BigDecimal("0.425"),
        GeographicRegionType.CANADA, new BigDecimal("0.125"),
        GeographicRegionType.EUROPE, new BigDecimal("0.2"),
        GeographicRegionType.OTHER, new BigDecimal("0.25")));
  }

  @Override
  protected PortfolioHolding excludedSecurityHolding() {
    return usStock("AAPL", 200);
  }

  @Override
  protected String expectedMissingFundAllocationCode() {
    return "FDS-031";
  }

  @Test
  void shouldExcludeBothUsAndCanadaStocks_fromBothFetchers() {
    PortfolioHolding fund = canadaMutualFund("RBF605", 100);
    PortfolioHolding usStock = usStock("AAPL", 200);
    PortfolioHolding canadaStock = canadaStock("RY", 200);

    when(geographicFetcher.fetch(anyList(), anyList())).thenReturn(Map.of(
        fund, allocation(Map.of(GeographicRegionType.US, ONE), Currency.CAD)));

    FixedIncomeGeographicExposureResult result = service.perform(command(fund, usStock, canadaStock));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<PortfolioHolding>> fundCaptor = ArgumentCaptor.forClass(List.class);
    verify(geographicFetcher).fetch(fundCaptor.capture(), anyList());
    assertThat(fundCaptor.getValue()).containsExactly(fund).doesNotContain(usStock, canadaStock);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<PortfolioHolding>> stockCaptor = ArgumentCaptor.forClass(List.class);
    verify(geographyFetcher).fetch(stockCaptor.capture(), anyList());
    assertThat(stockCaptor.getValue()).isEmpty();

    assertExposureEquals(result, distribution(Map.of(GeographicRegionType.US, ONE)));
  }

  @Test
  void shouldIncludeFixedIncomeAndEtf_inGeographicAllocationFetch() {
    PortfolioHolding bond = fixedIncome("BOND", 200);
    PortfolioHolding usEtf = usEtf("BND", 100);
    PortfolioHolding canadaEtf = canadaEtf("ZCN", 100);
    PortfolioHolding fund = canadaMutualFund("RBF605", 100);

    when(geographicFetcher.fetch(anyList(), anyList())).thenReturn(Map.of(
        bond, allocation(Map.of(GeographicRegionType.EUROPE, ONE), Currency.EUR),
        usEtf, allocation(Map.of(GeographicRegionType.US, ONE), Currency.USD),
        canadaEtf, allocation(Map.of(GeographicRegionType.CANADA, ONE), Currency.CAD),
        fund, allocation(Map.of(GeographicRegionType.CANADA, ONE), Currency.CAD)));

    FixedIncomeGeographicExposureResult result = service.perform(command(bond, usEtf, canadaEtf, fund));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<PortfolioHolding>> fundCaptor = ArgumentCaptor.forClass(List.class);
    verify(geographicFetcher).fetch(fundCaptor.capture(), anyList());
    assertThat(fundCaptor.getValue()).contains(bond, usEtf, canadaEtf, fund);

    Map<GeographicRegionType, BigDecimal> expected = distribution(Map.of(
        GeographicRegionType.EUROPE, new BigDecimal("0.4"),
        GeographicRegionType.US, new BigDecimal("0.2"),
        GeographicRegionType.CANADA, new BigDecimal("0.4")));
    assertExposureEquals(result, expected);
  }
}
