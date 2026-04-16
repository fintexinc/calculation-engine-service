package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.ce.model.domain.holding.MonthlyReturnGeneratableHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.ReqValidationException;
import com.fintex.ce.util.DateTimeUtils;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashSet;
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

  public static void validate(List<Holding> holdings) {
    if (CollectionUtils.isEmpty(holdings)) {
      return;
    }
    validateNoDuplicateHoldings(holdings);
    validateGicInvestmentDates(holdings);
    validateCashHoldingCurrencies(holdings);
  }

  public static void validateHoldingValues(List<Holding> holdings) {
    if (CollectionUtils.isEmpty(holdings)) {
      return;
    }
    for (Holding holding : holdings) {
      BigDecimal value = holding.getValue();
      if (value == null) {
        throw buildValueMissingException(holding);
      }
      if (value.compareTo(BigDecimal.ZERO) < 0) {
        throw ErrorCode.ERR_ALL_GTZ_001.reqValidationError();
      }
    }
  }

  private static void validateNoDuplicateHoldings(List<Holding> holdings) {
    List<Holding> nonGicHoldings = holdings.stream()
        .filter(h -> !(h instanceof GicHolding))
        .toList();
    if (new HashSet<>(nonGicHoldings).size() != nonGicHoldings.size()) {
      throw ErrorCode.ERR_DH_001.reqValidationError();
    }
  }

  private static void validateGicInvestmentDates(List<Holding> holdings) {
    for (Holding holding : holdings) {
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

  private static void validateCashHoldingCurrencies(List<Holding> holdings) {
    holdings.stream()
        .filter(CashHolding.class::isInstance)
        .map(CashHolding.class::cast)
        .filter(cashHolding -> Objects.isNull(cashHolding.getCurrency()))
        .findFirst()
        .ifPresent(ignored -> {
          throw ErrorCode.ERR_RRC_MC_002.reqValidationError();
        });
  }

  private static ReqValidationException buildInvestmentDateException(Holding holding) {
    String code = Optional.ofNullable(holding).map(Holding::getIdsString).orElse("");
    String message = String.format("Investment date could not be before %s years ago",
        DateTimeUtils.QUINCENTENARY);
    return new ReqValidationException(code, message);
  }

  private static ReqValidationException buildValueMissingException(Holding holding) {
    SecurityIdentifier secId = holding.getSecurityIdentifier();
    if (secId != null && secId.getId() != null) {
      return ErrorCode.ERR_ALL_GTZ_001.reqValidationErrorWithId(secId.getId());
    }
    return ErrorCode.ERR_ALL_GTZ_001.reqValidationError();
  }
}
