package ca.tangerine.pce.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.UrlHandlerFilter;

import ca.tangerine.pce.rest.controller.PortfolioCalculationController;

/**
 * Makes a trailing slash carry no semantics, as the Tangerine API guidelines require: Spring Boot 3 stopped matching
 * trailing slashes and would answer {@code 404} for {@code /api/v1/portfolio/calculations/}. The slash is trimmed
 * before the dispatcher resolves a handler, so both spellings reach the same one and return the same payload.
 *
 * <p>
 * Only this service's own API paths are normalised. Swagger UI resolves its assets relative to a trailing-slash URL, so
 * rewriting those would break the documentation page.
 */
@Configuration
public class RestPathConfig {

  static final String TRAILING_SLASH_PATTERN = PortfolioCalculationController.BASE_PATH + "/**";

  /**
   * Ordered after {@code RequestLoggingFilter} (HIGHEST_PRECEDENCE + 1) so the access log still records the URI the
   * caller actually sent, rather than the one this filter rewrote it to.
   */
  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE + 2)
  static UrlHandlerFilter trailingSlashFilter() {
    return UrlHandlerFilter
        .trailingSlashHandler(TRAILING_SLASH_PATTERN)
        .wrapRequest()
        .build();
  }
}
