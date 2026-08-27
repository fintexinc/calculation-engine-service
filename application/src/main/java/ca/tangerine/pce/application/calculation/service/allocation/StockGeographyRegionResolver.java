package ca.tangerine.pce.application.calculation.service.allocation;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.domain.allocation.GeographicRegionType;
import ca.tangerine.wm.commons.domain.allocation.RegionDatapoint;
import ca.tangerine.wm.commons.domain.allocation.SecurityRegion;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.financial.Geography;
import ca.tangerine.wm.commons.domain.reference.CountryDatapoint;
import ca.tangerine.wm.commons.error.Notification;

/**
 * Resolves the single geographic region of an individual stock from the {@code GEOGRAPHY} attribute.
 *
 * <p>
 * Individual stocks are a special case for every region breakdown: Market Investment Catalogue publishes a per-bucket
 * geographic allocation only for funds / ETFs / bonds, because a single company belongs to exactly one region. So a
 * stock contributes one 100% bucket rather than a distribution.
 *
 * <p>
 * Extracted from {@link AbstractGeographicExposureService}, where this logic originally lived as private methods, so
 * that the consolidated {@link GeographicExposureService} can reuse it verbatim. Sharing was chosen over duplicating
 * because the fallback chain and its two distinct warnings are the kind of detail that silently drifts apart when
 * copied: the copies would keep working while quietly disagreeing on how an unresolved stock is reported.
 */
@Service
public class StockGeographyRegionResolver {

  /**
   * The stock's region, or {@code null} when it cannot be resolved at all. A warning is appended in that case, and the
   * caller is expected to fall back to {@link GeographicRegionType#UNKNOWN} — resolution failure must not drop the
   * holding, otherwise the remaining regions would silently absorb its weight and the client would see a plausible
   * breakdown with no indication that data was missing.
   *
   * @param metricName
   *          user-friendly metric name, carried into the {@code SECURITY_NOT_FOUND_FOR_METRIC} message so the warning
   *          names the metric the client actually asked for rather than this shared helper
   */
  public GeographicRegionType resolve(PortfolioHolding holding, Geography geography, String metricName,
      List<Notification> warnings) {
    if (geography == null) {
      warnings.add(ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC.toNotificationForHolding(holding, metricName));
      return null;
    }
    GeographicRegionType region = resolveRegion(geography);
    if (region == null) {
      warnings.add(ErrorCode.MISSING_BUSINESS_COUNTRY_CODE.toNotificationForHolding(holding));
    }
    return region;
  }

  /**
   * Business country first, {@link SecurityRegion} only as a fallback. The order matters: business country maps onto
   * the full {@link GeographicRegionType} scale, whereas {@code SecurityRegion} is a coarser four-value enum whose
   * {@code EMERGING_MARKETS} and {@code OTHER} both collapse into {@code OTHER} — preferring it would throw away region
   * detail we already have.
   */
  private GeographicRegionType resolveRegion(Geography geography) {
    GeographicRegionType fromCountry = Optional.ofNullable(geography)
        .map(Geography::getBusinessCountry)
        .map(CountryDatapoint::getValue)
        .map(Country::getGeographyRegion)
        .orElse(null);
    if (fromCountry != null) {
      return fromCountry;
    }
    SecurityRegion fallbackRegion = Optional.ofNullable(geography)
        .map(Geography::getRegion)
        .map(RegionDatapoint::getValue)
        .orElse(null);
    return regionFromSecurityRegion(fallbackRegion);
  }

  private GeographicRegionType regionFromSecurityRegion(SecurityRegion securityRegion) {
    if (securityRegion == null) {
      return null;
    }
    return switch (securityRegion) {
      case USA -> GeographicRegionType.US;
      case CANADA -> GeographicRegionType.CANADA;
      case EMERGING_MARKETS, OTHER -> GeographicRegionType.OTHER;
    };
  }
}
