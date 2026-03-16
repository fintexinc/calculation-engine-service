package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.enumeration.ExceptionCode;
import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.MonthlyReturnGeneratableHolding;
import com.fintex.ce.util.DateTimeUtils;
import com.fintex.ce.util.FilterUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class HoldingReqValidation extends ReqValidation {

  private final List<Holding> holdings;

  public HoldingReqValidation(final List<Holding> holdings) {
    this.holdings = holdings;
  }

  @Override
  public void check() {
    validateNoDuplicateHoldings();
    validateGicInvestmentDates();
    validateCashHoldingCurrencies();
  }

  private void validateNoDuplicateHoldings() {
    final var listOfHoldings = getHoldingsExcludingGicHoldings();
    if (new HashSet<>(listOfHoldings).size() != listOfHoldings.size()) {
      throw ExceptionCode.ERR_DH_001.reqValidationError();
    }
  }

  private void validateGicInvestmentDates() {
    for (Holding holding : this.holdings) {
      Optional.of(holding)
          .filter(FilterUtils.GIC_PREDICATE)
          .map(MonthlyReturnGeneratableHolding.class::cast)
          .map(MonthlyReturnGeneratableHolding::getInvestmentDate)
          .filter(DateTimeUtils::isDateOlderQuincentenaryFromNow)
          .ifPresent(ignored -> {
            throw throwException(holding, String.format("Investment date could not be before %s years ago",
                DateTimeUtils.QUINCENTENARY));
          });
    }
  }

  private void validateCashHoldingCurrencies() {
    final List<CashHolding> cashHoldings = this.holdings
        .stream()
        .filter(CashHolding.class::isInstance)
        .map(CashHolding.class::cast)
        .toList();
    if (cashHoldings.size() > 1) {
      cashHoldings.forEach(cashHolding -> {
        if (Objects.isNull(cashHolding.getCurrency())) {
          throw ExceptionCode.ERR_RRC_MC_002.reqValidationError();
        }
      });
    }
  }

  private List<Holding> getHoldingsExcludingGicHoldings() {
    return holdings.stream().filter(h -> !(h instanceof GicHolding)).toList();
  }

  private static ReqValidationException throwException(final Holding h, final String message) {
    final String code = Optional.ofNullable(h).map(Holding::generateUserIdentifier).orElse("");
    return new ReqValidationException(code, message);
  }
}
