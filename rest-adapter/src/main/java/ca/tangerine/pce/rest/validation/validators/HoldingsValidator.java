package ca.tangerine.pce.rest.validation.validators;

import ca.tangerine.pce.model.domain.holding.CashHolding;
import ca.tangerine.pce.model.domain.holding.MonthlyReturnGeneratableHolding;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.ValidationException;
import ca.tangerine.pce.util.DateTimeUtils;
import ca.tangerine.pce.util.FilterUtils;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.id.EquitySecurityIdentifier;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

import static ca.tangerine.wm.commons.domain.id.FiIdentifierType.TICKER_MIC;

/**
 * Holding-level request validation shared by the portfolio, benchmark and multi-portfolio request validators. Applies
 * structural rules (holding type, country, security identifier), business rules (GIC investment date, cash currency)
 * and holding-value rules, throwing a {@link ValidationException} on the first violation.
 */
@Component
@RequiredArgsConstructor
public class HoldingsValidator {

  private static final String HOLDING_TYPE_FIELD = "holdingType";
  private static final String USER_FORMATTED_HOLDING_TYPE_FIELD = "Holding Type";
  static final String COUNTRY_FIELD = "country";
  static final String USER_FORMATTED_COUNTRY_FIELD = "Country";
  private static final String SECURITY_IDENTIFIER_FIELD = "securityIdentifier";
  private static final String USER_FORMATTED_SECURITY_IDENTIFIER_FIELD = "Security Identifier";
  static final String SECURITY_IDENTIFIER_ID_FIELD = "securityIdentifier.id";
  static final String USER_FORMATTED_SECURITY_IDENTIFIER_ID_FIELD = "Security Identifier ID";
  private static final String SECURITY_IDENTIFIER_ID_TYPE_FIELD = "securityIdentifier.idType";
  private static final String USER_FORMATTED_SECURITY_IDENTIFIER_ID_TYPE_FIELD = "Security Identifier ID Type";
  static final String SECURITY_IDENTIFIER_EXCHANGE_ID_FIELD = "securityIdentifier.exchangeId";
  static final String USER_FORMATTED_SECURITY_IDENTIFIER_EXCHANGE_ID_FIELD = "Security Identifier Exchange ID";

  private final HoldingsValidationProperties properties;

  public void validate(List<PortfolioHolding> holdings) {
    if (CollectionUtils.isEmpty(holdings)) {
      return;
    }
    validateHoldingTypes(holdings);
    validateCountries(holdings);
    validateSecurityIdentifiers(holdings);
    validateGicInvestmentDates(holdings);
    validateCashHoldingCurrencies(holdings);
  }

  public void validateHoldingValues(List<PortfolioHolding> holdings) {
    if (CollectionUtils.isEmpty(holdings)) {
      return;
    }
    BigDecimal sum = BigDecimal.ZERO;
    for (PortfolioHolding holding : holdings) {
      BigDecimal value = holding.getValue();
      if (value == null) {
        throw buildValueMissingException(holding);
      }
      if (value.compareTo(BigDecimal.ZERO) < 0) {
        throw ErrorCode.HOLDING_VALUE_NEGATIVE_OR_NULL.toValidationExceptionForHolding(holding);
      }
      sum = sum.add(value);
    }
    if (sum.compareTo(BigDecimal.ZERO) <= 0) {
      throw ErrorCode.HOLDING_VALUES_SUM_NOT_POSITIVE.toValidationException();
    }
  }

  private static void validateHoldingTypes(List<PortfolioHolding> holdings) {
    for (PortfolioHolding holding : holdings) {
      if (holding == null || holding.getHoldingType() == null) {
        throw ErrorCode.FIELD_NOT_NULL.toValidationExceptionForField(HOLDING_TYPE_FIELD,
            USER_FORMATTED_HOLDING_TYPE_FIELD);
      }
    }
  }

  private void validateCountries(List<PortfolioHolding> holdings) {
    for (PortfolioHolding holding : holdings) {
      if (!requiresCountry(holding)) {
        continue;
      }
      Country country = holding.getCountry();
      if (country == null) {
        throw ErrorCode.FIELD_NOT_NULL.toValidationExceptionForField(COUNTRY_FIELD, USER_FORMATTED_COUNTRY_FIELD);
      }
      if (!properties.getSupportedSecurityCountries().contains(country)) {
        throw ErrorCode.COUNTRY_NOT_SUPPORTED.toValidationExceptionForHolding(holding, country.name())
            .withFieldName(COUNTRY_FIELD);
      }
    }
  }

  private static boolean requiresCountry(PortfolioHolding holding) {
    FinancialInstrumentType type = holding.getHoldingType();
    return type != null
        && !FilterUtils.LOCALLY_SOURCED_TYPES.contains(type)
        && type != FinancialInstrumentType.BENCHMARK_INDEX;
  }

  private static void validateSecurityIdentifiers(List<PortfolioHolding> holdings) {
    for (PortfolioHolding holding : holdings) {
      if (requiresSecurityIdentifier(holding)) {
        validateSecurityIdentifier(holding.getSecurityIdentifier());
      }
    }
  }

  private static boolean requiresSecurityIdentifier(PortfolioHolding holding) {
    return holding.getHoldingType() == null || !FilterUtils.LOCALLY_SOURCED_TYPES.contains(holding.getHoldingType());
  }

  private static void validateSecurityIdentifier(SecurityIdentifier identifier) {
    if (identifier == null) {
      throw ErrorCode.FIELD_NOT_NULL.toValidationExceptionForField(SECURITY_IDENTIFIER_FIELD,
          USER_FORMATTED_SECURITY_IDENTIFIER_FIELD);
    }
    if (identifier.getIdType() == null) {
      throw ErrorCode.FIELD_NOT_NULL.toValidationExceptionForField(SECURITY_IDENTIFIER_ID_TYPE_FIELD,
          USER_FORMATTED_SECURITY_IDENTIFIER_ID_TYPE_FIELD);
    }
    if (StringUtils.isBlank(identifier.getId())) {
      throw ErrorCode.FIELD_NOT_BLANK.toValidationExceptionForField(SECURITY_IDENTIFIER_ID_FIELD,
          USER_FORMATTED_SECURITY_IDENTIFIER_ID_FIELD);
    }
    if (identifier.getIdType() == TICKER_MIC && isExchangeIdMissing(identifier)) {
      throw ErrorCode.FIELD_NOT_BLANK.toValidationExceptionForField(SECURITY_IDENTIFIER_EXCHANGE_ID_FIELD,
          USER_FORMATTED_SECURITY_IDENTIFIER_EXCHANGE_ID_FIELD);
    }
  }

  private static boolean isExchangeIdMissing(SecurityIdentifier identifier) {
    return !(identifier instanceof EquitySecurityIdentifier equity) || StringUtils.isBlank(equity.getExchangeId());
  }

  private static void validateGicInvestmentDates(List<PortfolioHolding> holdings) {
    for (PortfolioHolding holding : holdings) {
      Optional.of(holding)
          .filter(FilterUtils.GIC_PREDICATE)
          .map(MonthlyReturnGeneratableHolding.class::cast)
          .map(MonthlyReturnGeneratableHolding::getInvestmentDate)
          .filter(DateTimeUtils::isDateOlderQuincentenaryFromNow)
          .ifPresent(ignored -> {
            throw buildInvestmentDateException(holding);
          });
    }
  }

  private static void validateCashHoldingCurrencies(List<PortfolioHolding> holdings) {
    holdings.stream()
        .filter(CashHolding.class::isInstance)
        .map(CashHolding.class::cast)
        .filter(cashHolding -> Objects.isNull(cashHolding.getCurrency()))
        .findFirst()
        .ifPresent(cashHolding -> {
          throw ErrorCode.HOLDING_MISSING_CURRENCY.toValidationExceptionForHolding(cashHolding);
        });
  }

  private static ValidationException buildInvestmentDateException(PortfolioHolding holding) {
    String id = Optional.ofNullable(holding).map(PortfolioHolding::getIdsString).orElse("");
    return ErrorCode.GIC_INVESTMENT_DATE_TOO_OLD.toValidationExceptionForId(id, id, DateTimeUtils.QUINCENTENARY);
  }

  private static ValidationException buildValueMissingException(PortfolioHolding holding) {
    return ErrorCode.HOLDING_VALUE_NEGATIVE_OR_NULL.toValidationExceptionForHolding(holding);
  }
}
