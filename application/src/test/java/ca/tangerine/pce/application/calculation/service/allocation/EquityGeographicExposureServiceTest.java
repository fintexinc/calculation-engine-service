package ca.tangerine.pce.application.calculation.service.allocation;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.fundCa;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holding;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holdingWithoutCountry;
import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.model.domain.calculation.allocation.GeographicExposureData;
import ca.tangerine.pce.model.domain.calculation.allocation.HoldingGeographicAllocation;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.exposure.EquityGeographicExposureResult;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.domain.allocation.GeographicRegionType;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.financial.Geography;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

class EquityGeographicExposureServiceTest
    extends
      AbstractGeographicExposureServiceTest<EquityGeographicExposureResult> {

  @Override
  protected AbstractGeographicExposureService<EquityGeographicExposureResult> createService() {
    return new EquityGeographicExposureService(portfolioWeightCalculator, stockRegionResolver);
  }

  @Override
  protected PortfolioHolding typeSpecificRelevantHolding() {
    return holding(new SecurityIdentifier("RY", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.CANADA, 100);
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
    return holdingWithoutCountry(new SecurityIdentifier("BOND", FiIdentifierType.MORNINGSTAR_ID),
        FinancialInstrumentType.FIXED_INCOME, BigDecimal.valueOf(200));
  }

  @Override
  protected String expectedMissingFundAllocationCode() {
    return ErrorCode.Codes.MISSING_EQUITY_GEOGRAPHIC_EXPOSURE;
  }

  @Test
  void shouldResolveStockRegionsViaGeographyData_andAttributeByBusinessCountry() {
    PortfolioHolding usStock = holding(new SecurityIdentifier("AAPL", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.USA, 200);
    PortfolioHolding canadaStock = holding(new SecurityIdentifier("RY", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.CANADA, 200);
    PortfolioHolding fund = fundCa("RBF605", 100);

    GeographicExposureData data = data(Map.of(
        fund, allocation(Map.of(GeographicRegionType.EUROPE, ONE), Currency.CAD)),
        Map.of(
            usStock, geography(Country.USA, Currency.USD),
            canadaStock, geography(Country.CANADA, Currency.CAD)));

    EquityGeographicExposureResult result = service.perform(command(usStock, canadaStock, fund), data);

    Map<GeographicRegionType, BigDecimal> expected = distribution(Map.of(
        GeographicRegionType.US, new BigDecimal("0.4"),
        GeographicRegionType.CANADA, new BigDecimal("0.4"),
        GeographicRegionType.EUROPE, new BigDecimal("0.2")));
    assertExposureEquals(result, expected);
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldMapNonNorthAmericanBusinessCountriesToTheirRegion() {
    PortfolioHolding japanStock = holding(new SecurityIdentifier("4875.T", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.USA, 100);
    PortfolioHolding germanyStock = holding(new SecurityIdentifier("TL0.DE", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.USA, 100);
    PortfolioHolding brazilStock = holding(new SecurityIdentifier("AURA33.SA", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.USA, 100);
    PortfolioHolding southAfricaStock = holding(new SecurityIdentifier("ZAFR.JO", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.USA, 100);

    GeographicExposureData data = data(Map.of(), Map.of(
        japanStock, geography(Country.JAPAN, Currency.JPY),
        germanyStock, geography(Country.GERMANY, Currency.EUR),
        brazilStock, geography(Country.BRAZIL, Currency.USD),
        southAfricaStock, geography(Country.SOUTH_AFRICA, Currency.USD)));

    EquityGeographicExposureResult result = service.perform(
        command(japanStock, germanyStock, brazilStock, southAfricaStock), data);

    Map<GeographicRegionType, BigDecimal> expected = distribution(Map.of(
        Country.JAPAN.getGeographyRegion(), new BigDecimal("0.25"),
        Country.GERMANY.getGeographyRegion(), new BigDecimal("0.25"),
        Country.BRAZIL.getGeographyRegion(), new BigDecimal("0.25"),
        Country.SOUTH_AFRICA.getGeographyRegion(), new BigDecimal("0.25")));
    assertExposureEquals(result, expected);
  }

  @Test
  void shouldEmitMissingBusinessCountryWarning_whenStockGeographyIsMissing() {
    PortfolioHolding fund = fundCa("RBF605", 100);
    PortfolioHolding unmappedStock = holding(new SecurityIdentifier("UNKNOWN", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.USA, 100);

    GeographicExposureData data = data(Map.of(
        fund, allocation(Map.of(GeographicRegionType.US, ONE), Currency.CAD)), Map.of());

    EquityGeographicExposureResult result = service.perform(command(fund, unmappedStock), data);

    assertExposureEquals(result, distribution(Map.of(
        GeographicRegionType.US, new BigDecimal("0.5"),
        GeographicRegionType.UNKNOWN, new BigDecimal("0.5"))));
    assertThat(result.getWarnings()).hasSize(1);
    assertThat(result.getWarnings().getFirst().getCode()).isEqualTo(ErrorCode.Codes.SECURITY_NOT_FOUND_FOR_METRIC);
  }
}
