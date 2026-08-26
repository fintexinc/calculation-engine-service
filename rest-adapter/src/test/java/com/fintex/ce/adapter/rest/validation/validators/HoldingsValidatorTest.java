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

import static com.fintex.ce.adapter.rest.validation.validators.HoldingsValidator.COUNTRY_FIELD;
import static com.fintex.ce.adapter.rest.validation.validators.HoldingsValidator.SECURITY_IDENTIFIER_EXCHANGE_ID_FIELD;
import static com.fintex.ce.adapter.rest.validation.validators.HoldingsValidator.SECURITY_IDENTIFIER_ID_FIELD;
import static com.fintex.ce.adapter.rest.validation.validators.HoldingsValidator.USER_FORMATTED_COUNTRY_FIELD;
import static com.fintex.ce.adapter.rest.validation.validators.HoldingsValidator.USER_FORMATTED_SECURITY_IDENTIFIER_EXCHANGE_ID_FIELD;
import static com.fintex.ce.adapter.rest.validation.validators.HoldingsValidator.USER_FORMATTED_SECURITY_IDENTIFIER_ID_FIELD;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.equity;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holding;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holdingWithoutCountry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HoldingsValidatorTest {

  private HoldingsValidator validatorWithSupportedCountries(Country... supported) {
    HoldingsValidationProperties properties = new HoldingsValidationProperties();
    properties.setSupportedSecurityCountries(EnumSet.copyOf(List.of(supported)));
    return new HoldingsValidator(properties);
  }

  @Test
  void shouldNotThrow_whenCountryIsSupported() {
    HoldingsValidator validator = validatorWithSupportedCountries(Country.CANADA, Country.USA);

    assertThatCode(() -> validator.validate(List.of(
        holding("ID1", FiIdentifierType.TICKER, FinancialInstrumentType.MUTUAL_FUND, Country.USA, BigDecimal.TEN))))
        .doesNotThrowAnyException();
  }

  @Test
  void shouldThrow_whenCountryIsNotInConfiguredSupportedSet() {
    HoldingsValidator validator = validatorWithSupportedCountries(Country.CANADA);

    assertThatThrownBy(() -> validator.validate(List.of(
        holding("ID1", FiIdentifierType.TICKER, FinancialInstrumentType.MUTUAL_FUND, Country.USA, BigDecimal.TEN))))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException validationException = (ValidationException) ex;
          assertThat(validationException.getErrorCode()).isEqualTo(ErrorCode.COUNTRY_NOT_SUPPORTED);
          assertThat(validationException.getFieldName()).isEqualTo(COUNTRY_FIELD);
          assertThat(validationException.getMessage()).isEqualTo(
              "The holding MUTUAL_FUND-ID1 has an unsupported country USA");
        });
  }

  @Test
  void shouldThrow_whenCountryIsMissing() {
    HoldingsValidator validator = validatorWithSupportedCountries(Country.CANADA, Country.USA);
    PortfolioHolding holding = holdingWithoutCountry(new SecurityIdentifier("ID1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, BigDecimal.TEN);

    assertThatThrownBy(() -> validator.validate(List.of(holding)))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException validationException = (ValidationException) ex;
          assertThat(validationException.getErrorCode()).isEqualTo(ErrorCode.FIELD_NOT_NULL);
          assertThat(validationException.getFieldName()).isEqualTo(COUNTRY_FIELD);
          assertThat(validationException.getMessage()).isEqualTo(USER_FORMATTED_COUNTRY_FIELD + " must not be null");
        });
  }

  @Test
  void shouldThrow_whenExchangeIdIsMissingForTickerMicIdentifier() {
    HoldingsValidator validator = validatorWithSupportedCountries(Country.CANADA);
    List<PortfolioHolding> holdings = List.of(
        equity("CNQ", null, FinancialInstrumentType.STOCK, Country.CANADA, BigDecimal.TEN));

    assertThatThrownBy(() -> validator.validate(holdings))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException validationException = (ValidationException) ex;
          assertThat(validationException.getErrorCode()).isEqualTo(ErrorCode.FIELD_NOT_BLANK);
          assertThat(validationException.getFieldName()).isEqualTo(SECURITY_IDENTIFIER_EXCHANGE_ID_FIELD);
          assertThat(validationException.getMessage())
              .isEqualTo(USER_FORMATTED_SECURITY_IDENTIFIER_EXCHANGE_ID_FIELD + " must not be blank");
        });
  }

  @Test
  void shouldThrow_whenExchangeIdIsBlankForTickerMicIdentifier() {
    HoldingsValidator validator = validatorWithSupportedCountries(Country.CANADA);
    List<PortfolioHolding> holdings = List.of(
        equity("CNQ", "   ", FinancialInstrumentType.STOCK, Country.CANADA, BigDecimal.TEN));

    assertThatThrownBy(() -> validator.validate(holdings))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException validationException = (ValidationException) ex;
          assertThat(validationException.getErrorCode()).isEqualTo(ErrorCode.FIELD_NOT_BLANK);
          assertThat(validationException.getFieldName()).isEqualTo(SECURITY_IDENTIFIER_EXCHANGE_ID_FIELD);
          assertThat(validationException.getMessage())
              .isEqualTo(USER_FORMATTED_SECURITY_IDENTIFIER_EXCHANGE_ID_FIELD + " must not be blank");
        });
  }

  @Test
  void shouldNotThrow_whenExchangeIdIsPresentForTickerMicIdentifier() {
    HoldingsValidator validator = validatorWithSupportedCountries(Country.CANADA);

    assertThatCode(() -> validator.validate(List.of(
        equity("CNQ", "XTSE", FinancialInstrumentType.STOCK, Country.CANADA, BigDecimal.TEN))))
        .doesNotThrowAnyException();
  }

  @Test
  void shouldThrow_whenTickerMicIdentifierIsNotEquitySecurityIdentifier() {
    HoldingsValidator validator = validatorWithSupportedCountries(Country.CANADA);
    PortfolioHolding holding = holding(new SecurityIdentifier("CNQ", FiIdentifierType.TICKER_MIC),
        FinancialInstrumentType.STOCK, Country.CANADA, BigDecimal.TEN);

    assertThatThrownBy(() -> validator.validate(List.of(holding)))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException validationException = (ValidationException) ex;
          assertThat(validationException.getErrorCode()).isEqualTo(ErrorCode.FIELD_NOT_BLANK);
          assertThat(validationException.getFieldName()).isEqualTo(SECURITY_IDENTIFIER_EXCHANGE_ID_FIELD);
          assertThat(validationException.getMessage())
              .isEqualTo(USER_FORMATTED_SECURITY_IDENTIFIER_EXCHANGE_ID_FIELD + " must not be blank");
        });
  }

  @Test
  void shouldNotThrow_whenExchangeIdIsMissingForNonTickerMicEquityIdentifier() {
    HoldingsValidator validator = validatorWithSupportedCountries(Country.CANADA);

    assertThatCode(() -> validator.validate(List.of(
        holding(new SecurityIdentifier("CNQ", FiIdentifierType.TICKER), FinancialInstrumentType.STOCK,
            Country.CANADA, BigDecimal.TEN))))
        .doesNotThrowAnyException();
  }

  @Test
  void shouldThrow_whenNonTickerMicEquityIdentifierHasBlankId() {
    HoldingsValidator validator = validatorWithSupportedCountries(Country.CANADA);
    List<PortfolioHolding> holdings = List.of(
        holding(new SecurityIdentifier("", FiIdentifierType.TICKER), FinancialInstrumentType.STOCK,
            Country.CANADA, BigDecimal.TEN));

    assertThatThrownBy(() -> validator.validate(holdings))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException validationException = (ValidationException) ex;
          assertThat(validationException.getErrorCode()).isEqualTo(ErrorCode.FIELD_NOT_BLANK);
          assertThat(validationException.getFieldName()).isEqualTo(SECURITY_IDENTIFIER_ID_FIELD);
          assertThat(validationException.getMessage())
              .isEqualTo(USER_FORMATTED_SECURITY_IDENTIFIER_ID_FIELD + " must not be blank");
        });
  }

  @Test
  void shouldNotThrow_whenNonEquityIdentifierIsNotTickerMic() {
    HoldingsValidator validator = validatorWithSupportedCountries(Country.CANADA, Country.USA);

    assertThatCode(() -> validator.validate(List.of(
        holding("ID1", FiIdentifierType.TICKER, FinancialInstrumentType.MUTUAL_FUND, Country.USA, BigDecimal.TEN))))
        .doesNotThrowAnyException();
  }

  @Test
  void shouldThrow_whenSecurityIdentifierIdIsBlank() {
    HoldingsValidator validator = validatorWithSupportedCountries(Country.CANADA, Country.USA);
    PortfolioHolding holding = holding(new SecurityIdentifier("", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.USA, BigDecimal.TEN);

    assertThatThrownBy(() -> validator.validate(List.of(holding)))
        .isInstanceOf(ValidationException.class)
        .satisfies(ex -> {
          ValidationException validationException = (ValidationException) ex;
          assertThat(validationException.getErrorCode()).isEqualTo(ErrorCode.FIELD_NOT_BLANK);
          assertThat(validationException.getFieldName()).isEqualTo(SECURITY_IDENTIFIER_ID_FIELD);
          assertThat(validationException.getMessage())
              .isEqualTo(USER_FORMATTED_SECURITY_IDENTIFIER_ID_FIELD + " must not be blank");
        });
  }
}
