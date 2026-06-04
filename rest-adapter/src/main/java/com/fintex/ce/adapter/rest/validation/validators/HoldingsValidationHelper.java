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

  public static void validate(List<PortfolioHolding> holdings) {
    if (CollectionUtils.isEmpty(holdings)) {
      return;
    }
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
