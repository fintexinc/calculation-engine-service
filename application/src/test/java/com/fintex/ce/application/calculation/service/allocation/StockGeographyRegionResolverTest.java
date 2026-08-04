package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.allocation.GeographicRegionType;
import com.fintex.wm.commons.domain.allocation.SecurityRegion;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.financial.Geography;
import com.fintex.wm.commons.error.Notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Direct coverage of the stock region fallback chain. The happy paths are already exercised through the three services
 * that use this resolver; what is only reachable here is the full {@link SecurityRegion} switch and the precedence
 * between business country and region, so this test stays deliberately narrow rather than repeating the service-level
 * scenarios.
 */
class StockGeographyRegionResolverTest extends GeographicExposureFixtures {

  private static final String METRIC_NAME = "Geographic Exposure";

  private final StockGeographyRegionResolver resolver = new StockGeographyRegionResolver();

  @ParameterizedTest
  @CsvSource({
      "USA, US",
      "CANADA, CANADA",
      "EMERGING_MARKETS, OTHER",
      "OTHER, OTHER"
  })
  void shouldMapEverySecurityRegion_whenBusinessCountryIsAbsent(SecurityRegion securityRegion,
      GeographicRegionType expectedRegion) {
    PortfolioHolding stock = usStock("NO-COUNTRY", 100);
    List<Notification> warnings = new ArrayList<>();

    GeographicRegionType region = resolver.resolve(stock, geographyWithRegionOnly(securityRegion, Currency.USD),
        METRIC_NAME, warnings);

    assertThat(region).isEqualTo(expectedRegion);
    assertThat(warnings).isEmpty();
  }

  @Test
  void shouldPreferBusinessCountryOverSecurityRegion_whenBothArePresent() {
    PortfolioHolding stock = usStock("BOTH", 100);
    Geography geography = geographyWithRegionOnly(SecurityRegion.EMERGING_MARKETS, Currency.USD);
    geography.setBusinessCountry(geography(Country.GERMANY, Currency.EUR).getBusinessCountry());
    List<Notification> warnings = new ArrayList<>();

    GeographicRegionType region = resolver.resolve(stock, geography, METRIC_NAME, warnings);

    assertThat(region).isEqualTo(GeographicRegionType.EUROPE);
    assertThat(Country.GERMANY.getGeographyRegion()).isEqualTo(GeographicRegionType.EUROPE);
    assertThat(warnings).isEmpty();
  }

  @Test
  void shouldReturnNullAndWarnAboutMissingSecurity_whenGeographyIsAbsent() {
    PortfolioHolding stock = usStock("UNRESOLVED", 100);
    List<Notification> warnings = new ArrayList<>();

    GeographicRegionType region = resolver.resolve(stock, null, METRIC_NAME, warnings);

    assertThat(region).isNull();
    assertThat(warnings).hasSize(1);
    assertThat(warnings.getFirst().getCode()).isEqualTo(ErrorCode.Codes.SECURITY_NOT_FOUND_FOR_METRIC);
    assertThat(warnings.getFirst().getMessage()).contains(METRIC_NAME);
  }

  @Test
  void shouldReturnNullAndWarnAboutMissingBusinessCountry_whenNeitherCountryNorRegionResolves() {
    PortfolioHolding stock = usStock("EMPTY-GEOGRAPHY", 100);
    List<Notification> warnings = new ArrayList<>();

    GeographicRegionType region = resolver.resolve(stock, geography(null, Currency.USD), METRIC_NAME, warnings);

    assertThat(region).isNull();
    assertThat(warnings).hasSize(1);
    assertThat(warnings.getFirst().getCode()).isEqualTo(ErrorCode.Codes.MISSING_BUSINESS_COUNTRY_CODE);
  }
}
