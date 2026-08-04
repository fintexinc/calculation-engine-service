package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.model.domain.calculation.allocation.GeographicExposureData;
import com.fintex.ce.model.domain.calculation.allocation.HoldingGeographicAllocation;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.GeographicExposureResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.wm.commons.domain.allocation.GeographicRegionType;
import com.fintex.wm.commons.domain.allocation.RegionDatapoint;
import com.fintex.wm.commons.domain.allocation.SecurityRegion;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.financial.Geography;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.reference.CountryDatapoint;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Holding / geography fixtures and per-region assertions shared by every geographic exposure test.
 *
 * <p>
 * Exists as a superclass rather than a static-import utility because the concrete tests call these builders unqualified
 * and static imports are not inherited: keeping them inheritable lets both the per-sleeve hierarchy
 * ({@link AbstractGeographicExposureServiceTest}) and the consolidated {@link GeographicExposureServiceTest} — which
 * stays outside that hierarchy because it excludes no security type, so the hierarchy's excluded-holding contract does
 * not apply — share one set of fixtures instead of two copies that drift.
 */
abstract class GeographicExposureFixtures {

  protected static final BigDecimal TOLERANCE = new BigDecimal("0.0000000001");

  protected Map<GeographicRegionType, BigDecimal> zeroes() {
    Map<GeographicRegionType, BigDecimal> map = new EnumMap<>(GeographicRegionType.class);
    for (GeographicRegionType region : GeographicRegionType.values()) {
      map.put(region, ZERO);
    }
    return map;
  }

  protected Map<GeographicRegionType, BigDecimal> distribution(Map<GeographicRegionType, BigDecimal> values) {
    Map<GeographicRegionType, BigDecimal> map = zeroes();
    map.putAll(values);
    return map;
  }

  /**
   * Asserts every bucket of the enum, not just the ones the scenario populates: the client donut renders zero regions
   * as "0%" rather than omitting them, so a silently missing or unexpectedly non-zero bucket is a real defect.
   */
  protected void assertExposureEquals(GeographicExposureResult result,
      Map<GeographicRegionType, BigDecimal> expected) {
    Map<GeographicRegionType, BigDecimal> actual = result.getGeographicExposure();
    assertThat(actual).containsOnlyKeys(GeographicRegionType.values());
    expected.forEach((region, expectedValue) -> assertThat(actual.get(region))
        .as("region %s", region)
        .isCloseTo(expectedValue, within(TOLERANCE)));
  }

  protected void assertNullExposure(GeographicExposureResult result) {
    Map<GeographicRegionType, BigDecimal> actual = result.getGeographicExposure();
    assertThat(actual).containsOnlyKeys(GeographicRegionType.values());
    assertThat(actual.values()).allSatisfy(v -> assertThat(v).isNull());
  }

  protected static PortfolioHoldingsCommand command(PortfolioHolding... holdings) {
    PortfolioHoldingsCommand cmd = mock(PortfolioHoldingsCommand.class);
    when(cmd.getHoldings()).thenReturn(List.of(holdings));
    return cmd;
  }

  protected static Geography geography(Country businessCountry, Currency currency) {
    Geography geography = new Geography();
    if (businessCountry != null) {
      geography.setBusinessCountry(new CountryDatapoint(businessCountry));
    }
    if (currency != null) {
      CurrencyDatapoint datapoint = new CurrencyDatapoint();
      datapoint.setValue(currency);
      geography.setCurrency(datapoint);
    }
    return geography;
  }

  /**
   * Geography carrying only the coarse {@link SecurityRegion}, used to exercise the fallback taken when a security has
   * no business country.
   */
  protected static Geography geographyWithRegionOnly(SecurityRegion region, Currency currency) {
    Geography geography = geography(null, currency);
    geography.setRegion(new RegionDatapoint(region));
    return geography;
  }

  protected static PortfolioHolding canadaMutualFund(String id, long value) {
    return PortfolioHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.MUTUAL_FUND).country(Country.CANADA)
        .securityIdentifier(new SecurityIdentifier(id, FiIdentifierType.MORNINGSTAR_ID))
        .build();
  }

  protected static PortfolioHolding usEtf(String ticker, long value) {
    return PortfolioHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.ETF).country(Country.USA)
        .securityIdentifier(new SecurityIdentifier(ticker, FiIdentifierType.TICKER))
        .build();
  }

  protected static PortfolioHolding canadaEtf(String ticker, long value) {
    return PortfolioHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.ETF).country(Country.CANADA)
        .securityIdentifier(new SecurityIdentifier(ticker, FiIdentifierType.TICKER))
        .build();
  }

  protected static PortfolioHolding usStock(String ticker, long value) {
    return PortfolioHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.STOCK).country(Country.USA)
        .securityIdentifier(new SecurityIdentifier(ticker, FiIdentifierType.TICKER))
        .build();
  }

  protected static PortfolioHolding canadaStock(String ticker, long value) {
    return PortfolioHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.STOCK).country(Country.CANADA)
        .securityIdentifier(new SecurityIdentifier(ticker, FiIdentifierType.TICKER))
        .build();
  }

  protected static PortfolioHolding fixedIncome(String id, long value) {
    return PortfolioHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.FIXED_INCOME)
        .securityIdentifier(new SecurityIdentifier(id, FiIdentifierType.MORNINGSTAR_ID))
        .build();
  }

  protected static CashHolding cash(Currency currency, long value) {
    return CashHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.CASH)
        .securityIdentifier(new SecurityIdentifier("CASH-" + currency, FiIdentifierType.MORNINGSTAR_ID))
        .currency(currency)
        .build();
  }

  protected static GicHolding gic(Currency currency, long value) {
    return GicHolding.builder()
        .value(BigDecimal.valueOf(value))
        .holdingType(FinancialInstrumentType.GIC)
        .securityIdentifier(new SecurityIdentifier("GIC-" + currency, FiIdentifierType.MORNINGSTAR_ID))
        .currency(currency)
        .term(BigDecimal.valueOf(365))
        .build();
  }

  protected static GeographicExposureData data(Map<PortfolioHolding, HoldingGeographicAllocation> fundAllocations,
      Map<PortfolioHolding, Geography> stockGeographies) {
    return new GeographicExposureData(fundAllocations, stockGeographies);
  }

  /**
   * Built via the {@code EnumMap(Class)} constructor when the source is empty rather than always using the
   * {@code EnumMap(Map)} copy constructor: the latter throws {@code IllegalArgumentException} on an empty source map,
   * since it derives the key type from the map's contents. The empty case is a scenario under test — a security the
   * data source resolved but for which it returned no region breakdown.
   */
  protected static HoldingGeographicAllocation allocation(Map<GeographicRegionType, BigDecimal> values,
      Currency currency) {
    return HoldingGeographicAllocation.builder()
        .allocations(values.isEmpty() ? new EnumMap<>(GeographicRegionType.class) : new EnumMap<>(values))
        .currency(currency)
        .build();
  }
}
