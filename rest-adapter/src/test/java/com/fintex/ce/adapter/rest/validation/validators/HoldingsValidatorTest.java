package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.ValidationException;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.EquitySecurityIdentifier;
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

  private PortfolioHolding tickerMicStock(String exchangeId) {
    return equityStock("CNQ", FiIdentifierType.TICKER_MIC, exchangeId);
  }

  private PortfolioHolding equityStock(String id, FiIdentifierType idType, String exchangeId) {
    return new PortfolioHolding(
        BigDecimal.TEN, FinancialInstrumentType.STOCK, Country.CANADA,
        EquitySecurityIdentifier.builder()
            .id(id)
            .idType(idType)
            .exchangeId(exchangeId)
            .build());
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
    PortfolioHolding holding = new PortfolioHolding(
        BigDecimal.TEN, FinancialInstrumentType.MUTUAL_FUND,
        new SecurityIdentifier("ID1", FiIdentifierType.TICKER));

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

    assertThatThrownBy(() -> validator.validate(List.of(tickerMicStock(null))))
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

    assertThatThrownBy(() -> validator.validate(List.of(tickerMicStock("   "))))
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

    assertThatCode(() -> validator.validate(List.of(tickerMicStock("XTSE")))).doesNotThrowAnyException();
  }

  @Test
  void shouldThrow_whenTickerMicIdentifierIsNotEquitySecurityIdentifier() {
    HoldingsValidator validator = validatorWithSupportedCountries(Country.CANADA);
    PortfolioHolding holding = new PortfolioHolding(
        BigDecimal.TEN, FinancialInstrumentType.STOCK, Country.CANADA,
        new SecurityIdentifier("CNQ", FiIdentifierType.TICKER_MIC));

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

    assertThatCode(() -> validator.validate(List.of(equityStock("CNQ", FiIdentifierType.TICKER, null))))
        .doesNotThrowAnyException();
  }

  @Test
  void shouldThrow_whenNonTickerMicEquityIdentifierHasBlankId() {
    HoldingsValidator validator = validatorWithSupportedCountries(Country.CANADA);

    assertThatThrownBy(() -> validator.validate(List.of(equityStock("", FiIdentifierType.TICKER, null))))
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

    assertThatCode(() -> validator.validate(List.of(fund(Country.USA)))).doesNotThrowAnyException();
  }

  @Test
  void shouldThrow_whenSecurityIdentifierIdIsBlank() {
    HoldingsValidator validator = validatorWithSupportedCountries(Country.CANADA, Country.USA);
    PortfolioHolding holding = new PortfolioHolding(
        BigDecimal.TEN, FinancialInstrumentType.MUTUAL_FUND, Country.USA,
        new SecurityIdentifier("", FiIdentifierType.TICKER));

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
