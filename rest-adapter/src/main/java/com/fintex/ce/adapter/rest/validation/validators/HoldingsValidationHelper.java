package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.MonthlyReturnGeneratableHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.ValidationException;
import com.fintex.ce.util.DateTimeUtils;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.springframework.util.CollectionUtils;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.experimental.UtilityClass;

/**
 * Shared helper that encapsulates holding-level validation rules used by several request validators (portfolio
 * holdings, benchmark holdings, and per-portfolio holdings inside multi-portfolio commands).
 */
@UtilityClass
public class HoldingsValidationHelper {

  private static final String HOLDING_TYPE_FIELD = "holdingType";
  private static final String USER_FORMATTED_HOLDING_TYPE_FIELD = "Holding Type";
  private static final String SECURITY_IDENTIFIER_FIELD = "securityIdentifier";
  private static final String USER_FORMATTED_SECURITY_IDENTIFIER_FIELD = "Security Identifier";
  private static final String SECURITY_IDENTIFIER_ID_FIELD = "securityIdentifier.id";
  private static final String USER_FORMATTED_SECURITY_IDENTIFIER_ID_FIELD = "Security Identifier ID";
  private static final String SECURITY_IDENTIFIER_ID_TYPE_FIELD = "securityIdentifier.idType";
  private static final String USER_FORMATTED_SECURITY_IDENTIFIER_ID_TYPE_FIELD = "Security Identifier ID Type";

  public static void validate(List<PortfolioHolding> holdings) {
    if (CollectionUtils.isEmpty(holdings)) {
      return;
    }
    validateHoldingTypes(holdings);
    validateSecurityIdentifiers(holdings);
    validateGicInvestmentDates(holdings);
    validateCashHoldingCurrencies(holdings);
  }

  public static void validateHoldingValues(List<PortfolioHolding> holdings) {
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
        throw ErrorCode.HOLDING_VALUE_NEGATIVE_OR_NULL.toValidationException();
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
        .ifPresent(ignored -> {
          throw ErrorCode.HOLDING_MISSING_CURRENCY.toValidationException();
        });
  }

  private static ValidationException buildInvestmentDateException(PortfolioHolding holding) {
    String id = Optional.ofNullable(holding).map(PortfolioHolding::getIdsString).orElse("");
    return ErrorCode.GIC_INVESTMENT_DATE_TOO_OLD.toValidationExceptionForId(id, id, DateTimeUtils.QUINCENTENARY);
  }

  private static ValidationException buildValueMissingException(PortfolioHolding holding) {
    SecurityIdentifier secId = holding.getSecurityIdentifier();
    if (secId != null && secId.getId() != null) {
      return ErrorCode.HOLDING_VALUE_NEGATIVE_OR_NULL.toValidationExceptionForId(secId.getId());
    }
    return ErrorCode.HOLDING_VALUE_NEGATIVE_OR_NULL.toValidationException();
  }
}
