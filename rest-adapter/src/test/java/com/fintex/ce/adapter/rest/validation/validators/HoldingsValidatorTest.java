package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.ValidationException;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HoldingsValidatorTest {

  private HoldingsValidator validatorWithSupportedCountries(Country... supported) {
    HoldingsValidationProperties properties = new HoldingsValidationProperties();
    properties.setSupportedSecurityCountries(EnumSet.copyOf(List.of(supported)));
    return new HoldingsValidator(properties);
  }

  private PortfolioHolding fund(Country country) {
    return new PortfolioHolding(
        BigDecimal.TEN, FinancialInstrumentType.MUTUAL_FUND, country,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));
  }

  @Test
  void shouldNotThrow_whenCountryIsSupported() {
    HoldingsValidator validator = validatorWithSupportedCountries(Country.CANADA, Country.USA);

    assertThatCode(() -> validator.validate(List.of(fund(Country.USA)))).doesNotThrowAnyException();
  }

  @Test
  void shouldThrow_whenCountryIsNotInConfiguredSupportedSet() {
    HoldingsValidator validator = validatorWithSupportedCountries(Country.CANADA);

    assertThatThrownBy(() -> validator.validate(List.of(fund(Country.USA))))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.COUNTRY_NOT_SUPPORTED);
          assertThat(rve.getFieldName()).isEqualTo("country");
        });
  }

  @Test
  void shouldThrow_whenCountryIsMissing() {
    HoldingsValidator validator = validatorWithSupportedCountries(Country.CANADA, Country.USA);
    PortfolioHolding holding = new PortfolioHolding(
        BigDecimal.TEN, FinancialInstrumentType.MUTUAL_FUND,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));

    assertThatThrownBy(() -> validator.validate(List.of(holding)))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException rve = (ValidationException) ex;
          assertThat(rve.getErrorCode()).isEqualTo(ErrorCode.FIELD_NOT_NULL);
          assertThat(rve.getFieldName()).isEqualTo("country");
        });
  }
}
