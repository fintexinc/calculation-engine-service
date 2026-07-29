package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.wm.commons.domain.enumeration.Country;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;
import lombok.Data;

/**
 * Configuration for holding-level request validation. Currently exposes the set of countries whose securities the
 * calculation engine is able to price and process; holdings from any other country are rejected at the REST boundary.
 */
@Data
@Component
@ConfigurationProperties(prefix = "calculation.validation")
public class HoldingsValidationProperties {

  private Set<Country> supportedSecurityCountries = EnumSet.of(Country.CANADA, Country.USA);
}
