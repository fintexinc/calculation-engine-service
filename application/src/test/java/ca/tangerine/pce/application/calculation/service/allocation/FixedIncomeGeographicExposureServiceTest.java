package ca.tangerine.pce.application.calculation.service.allocation;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.etf;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.etfCa;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.fundCa;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holding;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holdingWithoutCountry;
import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.model.domain.calculation.allocation.GeographicExposureData;
import ca.tangerine.pce.model.domain.calculation.allocation.HoldingGeographicAllocation;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.exposure.FixedIncomeGeographicExposureResult;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.domain.allocation.GeographicRegionType;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.financial.Geography;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

class FixedIncomeGeographicExposureServiceTest
    extends
      AbstractGeographicExposureServiceTest<FixedIncomeGeographicExposureResult> {

  @Override
  protected AbstractGeographicExposureService<FixedIncomeGeographicExposureResult> createService() {
    return new FixedIncomeGeographicExposureService(portfolioWeightCalculator, stockRegionResolver);
  }

  @Override
  protected PortfolioHolding typeSpecificRelevantHolding() {
    return holdingWithoutCountry(new SecurityIdentifier("BOND", FiIdentifierType.MORNINGSTAR_ID),
        FinancialInstrumentType.FIXED_INCOME, BigDecimal.valueOf(100));
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
    return holding(new SecurityIdentifier("AAPL", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.USA, 200);
  }

  @Override
  protected String expectedMissingFundAllocationCode() {
    return ErrorCode.Codes.MISSING_FIXED_INCOME_GEOGRAPHIC_EXPOSURE;
  }

  @Test
  void shouldExcludeBothUsAndCanadaStocks_fromExposureAggregation() {
    PortfolioHolding fund = fundCa("RBF605", 100);
    PortfolioHolding usStock = holding(new SecurityIdentifier("AAPL", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.USA, 200);
    PortfolioHolding canadaStock = holding(new SecurityIdentifier("RY", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.CANADA, 200);

    GeographicExposureData data = data(Map.of(
        fund, allocation(Map.of(GeographicRegionType.US, ONE), Currency.CAD)),
        Map.of(
            usStock, geography(Country.USA, Currency.USD),
            canadaStock, geography(Country.CANADA, Currency.CAD)));

    FixedIncomeGeographicExposureResult result = service.perform(command(fund, usStock, canadaStock), data);

    assertExposureEquals(result, distribution(Map.of(GeographicRegionType.US, ONE)));
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldIncludeFixedIncomeAndEtf_inGeographicAllocationPath() {
    PortfolioHolding bond = holdingWithoutCountry(new SecurityIdentifier("BOND", FiIdentifierType.MORNINGSTAR_ID),
        FinancialInstrumentType.FIXED_INCOME, BigDecimal.valueOf(200));
    PortfolioHolding usEtf = etf("BND", Country.USA, 100);
    PortfolioHolding canadaEtf = etfCa("ZCN", 100);
    PortfolioHolding fund = fundCa("RBF605", 100);

    GeographicExposureData data = data(Map.of(
        bond, allocation(Map.of(GeographicRegionType.EUROPE, ONE), Currency.EUR),
        usEtf, allocation(Map.of(GeographicRegionType.US, ONE), Currency.USD),
        canadaEtf, allocation(Map.of(GeographicRegionType.CANADA, ONE), Currency.CAD),
        fund, allocation(Map.of(GeographicRegionType.CANADA, ONE), Currency.CAD)), Map.of());

    FixedIncomeGeographicExposureResult result = service.perform(command(bond, usEtf, canadaEtf, fund), data);

    Map<GeographicRegionType, BigDecimal> expected = distribution(Map.of(
        GeographicRegionType.EUROPE, new BigDecimal("0.4"),
        GeographicRegionType.US, new BigDecimal("0.2"),
        GeographicRegionType.CANADA, new BigDecimal("0.4")));
    assertExposureEquals(result, expected);
    assertThat(result.getWarnings()).isEmpty();
  }
}
